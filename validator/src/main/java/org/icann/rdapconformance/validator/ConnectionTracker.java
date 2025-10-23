package org.icann.rdapconformance.validator;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.icann.rdapconformance.validator.CommonUtils.*;

/**
 * Singleton class that tracks and monitors all HTTP connections made during RDAP validation.
 *
 * <p>This class provides comprehensive connection tracking functionality including:</p>
 * <ul>
 *   <li>Recording connection details (URI, IP address, protocol, timing)</li>
 *   <li>Tracking connection status and HTTP response codes</li>
 *   <li>Identifying 404 patterns for resource availability analysis</li>
 *   <li>Generating detailed connection reports for debugging</li>
 *   <li>Supporting both main queries and additional conformance queries</li>
 * </ul>
 *
 * <p>Each connection is assigned a unique tracking ID (UUID) and stored as a
 * {@link ConnectionRecord} containing timing information, network details, and
 * HTTP response metadata.</p>
 *
 * <p>The tracker is thread-safe and uses synchronized collections to support
 * concurrent validation operations.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * ConnectionTracker tracker = ConnectionTracker.getInstance();
 * String trackingId = tracker.startTrackingNewConnection(uri, "GET", true);
 * // ... perform HTTP request ...
 * tracker.updateConnectionWithIpAddress(trackingId, "192.0.2.1");
 * tracker.completeConnectionTracking(trackingId, 200, ConnectionStatus.SUCCESS);
 * </pre>
 *
 * @see ConnectionRecord
 * @see ConnectionStatus
 * @since 1.0.0
 */
public class ConnectionTracker {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionTracker.class);

    // Session-keyed storage for concurrent validation requests
    private static final ConcurrentHashMap<String, ConnectionTracker> sessionInstances = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, List<ConnectionRecord>> sessionConnections = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Map<String, ConnectionRecord>> sessionConnectionsByTrackingId = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ConnectionRecord> sessionCurrentConnection = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ConnectionRecord> sessionLastMainConnection = new ConcurrentHashMap<>();

    // Instance holds its session ID for accessing the correct session data
    private final String sessionId;

    private ConnectionTracker(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Returns the session ID for this ConnectionTracker instance.
     *
     * @return the session identifier
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Returns the singleton instance for a specific session.
     *
     * @param sessionId the session identifier
     * @return the singleton ConnectionTracker instance for this session
     */
    public static synchronized ConnectionTracker getInstance(String sessionId) {
        return sessionInstances.computeIfAbsent(sessionId, k -> {
            // Initialize session data when creating new instance
            sessionConnections.put(k, Collections.synchronizedList(new ArrayList<>()));
            sessionConnectionsByTrackingId.put(k, Collections.synchronizedMap(new HashMap<>()));
            sessionCurrentConnection.remove(k); // Ensure no current connection initially
            sessionLastMainConnection.remove(k); // Ensure no last main connection initially
            return new ConnectionTracker(k);
        });
    }

    /**
     * Returns the singleton instance (deprecated - uses default session).
     *
     * @deprecated Use getInstance(String sessionId) instead
     * @return the singleton ConnectionTracker instance for default session
     */
    @Deprecated
    public static ConnectionTracker getInstance() {
        return getInstance("default");
    }

    /**
     * Resets the singleton instance for a specific session.
     *
     * @param sessionId the session to reset
     */
    public static void reset(String sessionId) {
        sessionInstances.remove(sessionId);
        sessionConnections.remove(sessionId);
        sessionConnectionsByTrackingId.remove(sessionId);
        sessionCurrentConnection.remove(sessionId);
        sessionLastMainConnection.remove(sessionId);
    }

    /**
     * Resets all sessions (primarily for testing).
     */
    public static void resetAll() {
        sessionInstances.clear();
        sessionConnections.clear();
        sessionConnectionsByTrackingId.clear();
        sessionCurrentConnection.clear();
        sessionLastMainConnection.clear();
    }

    /**
     * Reset the connection tracker (deprecated - resets default session).
     *
     * @deprecated Use reset(String sessionId) or resetAll() instead
     */
    @Deprecated
    public synchronized void reset() {
        reset("default");
    }

    /**
     * Generates a unique tracking ID
     *
     * @return A new unique tracking ID
     */
    private String generateTrackingId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Find a connection by its tracking ID
     *
     * @param trackingId The tracking ID to look up
     * @return The connection record, or null if not found
     */
    public synchronized ConnectionRecord getConnectionByTrackingId(String trackingId) {
        Map<String, ConnectionRecord> connectionsByTrackingId = sessionConnectionsByTrackingId.get(sessionId);
        return connectionsByTrackingId != null ? connectionsByTrackingId.get(trackingId) : null;
    }

    /**
     * Get the status code of a connection by its tracking ID
     *
     * @param trackingId The tracking ID to look up
     * @return The status code, or 0 if the connection is not found
     */
    public synchronized int getStatusCodeByTrackingId(String trackingId) {
        Map<String, ConnectionRecord> connectionsByTrackingId = sessionConnectionsByTrackingId.get(sessionId);
        if (connectionsByTrackingId != null) {
            ConnectionRecord record = connectionsByTrackingId.get(trackingId);
            return record != null ? record.getStatusCode() : ZERO;
        }
        return ZERO;
    }


    /**
     * Start tracking a new connection using the current NetworkInfo state
     * @param uri The URI being requested
     * @param isMainConnection Whether this is a main connection
     * @return The tracking ID of the new connection
     */
    public synchronized String startTrackingNewConnection(URI uri, String httpMethod, boolean isMainConnection) {
        String trackingId = generateTrackingId();
        ConnectionRecord record = new ConnectionRecord(
                uri,
                "UNKNOWN", // was NetworkInfo.getServerIpAddress(),
                NetworkInfo.getNetworkProtocol(),
                ZERO,  // Status code not yet known
                null,  // Duration not yet known
                null,  // Status not yet known
                httpMethod,
                Instant.now(),
                trackingId,
                isMainConnection
        );
        record.setStartTime(Instant.now());

        // Get session-based collections
        List<ConnectionRecord> connections = sessionConnections.get(sessionId);
        Map<String, ConnectionRecord> connectionsByTrackingId = sessionConnectionsByTrackingId.get(sessionId);

        if (connections != null) {
            connections.add(record);
        }
        if (connectionsByTrackingId != null) {
            connectionsByTrackingId.put(trackingId, record);
        }
        sessionCurrentConnection.put(sessionId, record);

        if (isMainConnection) {
            sessionLastMainConnection.put(sessionId, record);
            logger.debug("Started tracking main connection: {} for session {}", trackingId, sessionId);
        } else {
            logger.debug("Started tracking connection: {} for session {}", trackingId, sessionId);
        }

        return trackingId;
    }

    /**
     * Update the current connection with a new status
     * @param status The connection status to set
     */
    public synchronized void updateCurrentConnection(ConnectionStatus status) {
        ConnectionRecord currentConnection = sessionCurrentConnection.get(sessionId);
        if (currentConnection != null) {
            currentConnection.setStatus(status);
            logger.debug("Updated current connection with status: {} for session {}", status, sessionId);
        } else {
            logger.warn("Attempted to update current connection, but no current connection exists for session {}", sessionId);
        }
    }

    /**
     * Update the current connection with a new IP address
     * @param remoteAddress The new remote IP address to set
     */
    public synchronized void updateIPAddressOnCurrentConnection(String remoteAddress) {
        ConnectionRecord currentConnection = sessionCurrentConnection.get(sessionId);
        if (currentConnection != null) {
            currentConnection.setIpAddress(remoteAddress);
            logger.debug("Updated current connection with ipAddress: {} for session {}", remoteAddress, sessionId);
        } else {
            logger.warn("Attempted to update current connection, but no current connection exists for session {}", sessionId);
        }
    }

    /**
     * Update a connection's IP address by tracking ID
     * @param trackingId The tracking ID of the connection
     * @param remoteAddress The new remote IP address to set
     * @return true if the connection was found and updated, false otherwise
     */
    public synchronized boolean updateIPAddressById(String trackingId, String remoteAddress) {
        Map<String, ConnectionRecord> connectionsByTrackingId = sessionConnectionsByTrackingId.get(sessionId);
        if (connectionsByTrackingId != null) {
            ConnectionRecord record = connectionsByTrackingId.get(trackingId);
            if (record != null) {
                record.setIpAddress(remoteAddress);
                logger.debug("Updated connection {} with ipAddress: {} for session {}", trackingId, remoteAddress, sessionId);
                return true;
            }
        }
        logger.warn("Attempted to update connection {}, but no connection exists with that tracking ID for session {}", trackingId, sessionId);
        return false;
    }

    /**
     * Complete tracking the current connection with status code and status
     * @param statusCode The HTTP status code
     * @param status The connection status
     */
    public synchronized void completeCurrentConnection(int statusCode, ConnectionStatus status) {
        ConnectionRecord currentConnection = sessionCurrentConnection.get(sessionId);
        if (currentConnection != null) {
            Duration duration = Duration.between(currentConnection.getStartTime(), Instant.now());
            currentConnection.setStatusCode(statusCode);
            currentConnection.setDuration(duration);
            currentConnection.setStatus(status);

            // If this was a main connection that's being completed, keep track of it
            if (currentConnection.isMainConnection()) {
                sessionLastMainConnection.put(sessionId, currentConnection);
            }

            logger.debug("Completed current connection with tracking id: {} for session {}", currentConnection.getTrackingId(), sessionId);
            sessionCurrentConnection.remove(sessionId);
        } else {
            logger.warn("Attempted to complete current connection, but no current connection exists for session {}", sessionId);
        }
    }

    /**
     * Start tracking a connection with explicit parameters
     * @param uri The URI being requested
     * @param ipAddress The server IP address
     * @param protocol The network protocol used
     * @param httpMethod The HTTP method used
     * @param isMainConnection Whether this is a main connection
     * @return The tracking ID of the new connection
     */
    public synchronized String startTracking(URI uri, String ipAddress, NetworkProtocol protocol,
                                             String httpMethod, boolean isMainConnection) {
        String trackingId = generateTrackingId();
        ConnectionRecord record = new ConnectionRecord(
                uri,
                ipAddress,
                protocol,
                ZERO,
                null,
                null,
                httpMethod,
                Instant.now(),
                trackingId,
                isMainConnection
        );
        record.setStartTime(Instant.now());

        // Get session-based collections
        List<ConnectionRecord> connections = sessionConnections.get(sessionId);
        Map<String, ConnectionRecord> connectionsByTrackingId = sessionConnectionsByTrackingId.get(sessionId);

        if (connections != null) {
            connections.add(record);
        }
        if (connectionsByTrackingId != null) {
            connectionsByTrackingId.put(trackingId, record);
        }
        sessionCurrentConnection.put(sessionId, record);

        if (isMainConnection) {
            sessionLastMainConnection.put(sessionId, record);
            logger.debug("Started tracking main connection: {} for session {}", trackingId, sessionId);
        } else {
            logger.debug("Started tracking connection: {} for session {}", trackingId, sessionId);
        }

        return trackingId;
    }

    /**
     * Start tracking a connection with explicit parameters (default non-main)
     * @param uri The URI being requested
     * @param ipAddress The server IP address
     * @param protocol The network protocol used
     * @param httpMethod The HTTP method used
     * @return The tracking ID of the new connection
     */
    public synchronized String startTracking(URI uri, String ipAddress, NetworkProtocol protocol, String httpMethod) {
        return startTracking(uri, ipAddress, protocol, httpMethod, false);
    }

    /**
     * Complete tracking a connection by finding it in the connections list
     * @param uri The URI being requested
     * @param ipAddress The server IP address
     * @param statusCode The HTTP status code
     * @param status The connection status
     */
    public synchronized void completeTracking(URI uri, String ipAddress,
                                              int statusCode, ConnectionStatus status) {
        List<ConnectionRecord> connections = sessionConnections.get(sessionId);
        ConnectionRecord currentConnection = sessionCurrentConnection.get(sessionId);

        if (connections != null) {
            for (int i = connections.size() - ONE; i >= ZERO; i--) {
                ConnectionRecord record = connections.get(i);
                if (record.getUri().equals(uri) && record.getIpAddress().equals(ipAddress) &&
                        record.getStartTime() != null && record.getDuration() == null) {

                    Duration duration = Duration.between(record.getStartTime(), Instant.now());
                    record.setStatusCode(statusCode);
                    record.setDuration(duration);
                    record.setStatus(status);

                    // If this was a main connection that's being completed, keep track of it
                    if (record.isMainConnection()) {
                        sessionLastMainConnection.put(sessionId, record);
                    }

                    // If this was the current connection, clear it
                    if (currentConnection == record) {
                        sessionCurrentConnection.remove(sessionId);
                    }

                    logger.debug("Completed tracking connection: {} for session {}", record, sessionId);
                    return;
                }
            }
        }
    }

    /**
     * Complete tracking a connection by its tracking ID
     * @param trackingId The tracking ID of the connection
     * @param statusCode The HTTP status code
     * @param status The connection status
     * @return true if the connection was found and completed, false otherwise
     */
    public synchronized boolean completeTrackingById(String trackingId, int statusCode, ConnectionStatus status) {
        Map<String, ConnectionRecord> connectionsByTrackingId = sessionConnectionsByTrackingId.get(sessionId);
        ConnectionRecord currentConnection = sessionCurrentConnection.get(sessionId);

        if (connectionsByTrackingId != null) {
            ConnectionRecord record = connectionsByTrackingId.get(trackingId);
            if (record != null && record.getDuration() == null) {
                Duration duration = Duration.between(record.getStartTime(), Instant.now());
                record.setStatusCode(statusCode);
                record.setDuration(duration);
                record.setStatus(status);

                // If this was a main connection that's being completed, keep track of it
                if (record.isMainConnection()) {
                    sessionLastMainConnection.put(sessionId, record);
                }

                // If this was the current connection, clear it
                if (currentConnection == record) {
                    sessionCurrentConnection.remove(sessionId);
                }

                logger.debug("Completed tracking connection by ID: {} for session {}", record, sessionId);
                return true;
            }
        }
        return false;
    }

    /**
     * Get the current connection
     * @return The current connection record
     */
    public synchronized ConnectionRecord getCurrentConnection() {
        return sessionCurrentConnection.get(sessionId);
    }

    /**
     * Get all connections
     * @return A copy of the connections list
     */
    public synchronized List<ConnectionRecord> getConnections() {
        List<ConnectionRecord> connections = sessionConnections.get(sessionId);
        return connections != null ? new ArrayList<>(connections) : new ArrayList<>();
    }

    /**
     * Get the most recent connection
     * @return The last connection record
     */
    public synchronized ConnectionRecord getLastConnection() {
        List<ConnectionRecord> connections = sessionConnections.get(sessionId);
        if (connections == null || connections.isEmpty()) {
            return null;
        }
        return connections.get(connections.size() - 1);
    }

    /**
     * Get the most recent main connection
     * @return The last main connection record
     */
    public synchronized ConnectionRecord getLastMainConnection() {
        return sessionLastMainConnection.get(sessionId);
    }

    /**
     * Count connections with error status
     * @return The number of connections with non-success status
     */
    public synchronized int getErrorCount() {
        int count = ZERO;
        List<ConnectionRecord> connections = sessionConnections.get(sessionId);
        if (connections != null) {
            for (ConnectionRecord record : connections) {
                if (record.getStatus() != null && record.getStatus() != ConnectionStatus.SUCCESS) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Count connections with success status
     * @return The number of connections with success status
     */
    public synchronized int getSuccessCount() {
        int count = 0;
        List<ConnectionRecord> connections = sessionConnections.get(sessionId);
        if (connections != null) {
            for (ConnectionRecord record : connections) {
                if (record.getStatus() == ConnectionStatus.SUCCESS) {
                    count++;
                }
            }
        }
        return count;
    }

    // Note: reset() method moved to static resetAll() and reset(sessionId) methods above

    /**
     * Get the current status code of the last connection
     * @return The status code, or null if not available
     */
    public static Integer getCurrentStatusCode() {
        ConnectionTracker tracker = getInstance();
        if(tracker.getCurrentConnection() != null) {
            return tracker.getCurrentConnection().getStatusCode();
        }
        // else
        ConnectionRecord currentConnection = tracker.getLastConnection(); // Given the way it works, the _last_ connection is the one we want. Current would be set to null.

        if (currentConnection == null || currentConnection.getStatusCode() == ZERO || currentConnection.getStatus() == null) {
            return null;
        }

        return currentConnection.getStatusCode();
    }

    /**
     * Get the status code of the last main connection
     * @return The status code, or null if not available
     */
    public static Integer getMainStatusCode() {
        ConnectionRecord mainConnection = getInstance().getLastMainConnection();

        if (mainConnection == null || mainConnection.getStatusCode() == ZERO || mainConnection.getStatus() == null) {
            return ZERO; // force to zero
        }

        // otherwise it's good and return it
        return mainConnection.getStatusCode();
    }


    /**
     * Returns a detailed string representation of all tracked connections.
     *
     * <p>The output includes connection counts and individual connection details
     * with timing, status, and network information for debugging purposes.</p>
     *
     * @return formatted string containing connection tracking summary
     */
    @Override
    public synchronized String toString() {
        List<ConnectionRecord> connections = sessionConnections.get(sessionId);
        Map<String, ConnectionRecord> connectionsByTrackingId = sessionConnectionsByTrackingId.get(sessionId);

        if (connections == null || connections.isEmpty()) {
            return "No connections tracked for session " + sessionId;
        }

        StringBuilder sb = new StringBuilder("Connection Tracking Report (Session: " + sessionId + "):\n");

        // Track which connections have been displayed as redirect follows
        Set<String> displayedAsFollows = new HashSet<>();

        for (ConnectionRecord record : connections) {
            // Skip connections that are redirect follows - they'll be displayed under their parent
            if (record.isRedirectFollow() && displayedAsFollows.contains(record.getTrackingId())) {
                continue;
            }

            // Display the main connection
            sb.append(record.toStringWithoutRedirectStatus()).append(getRedirectStatusForDisplay(record)).append("\n");

            // If this connection redirected, show the follow-up indented
            if (record.getRedirectedToId() != null && connectionsByTrackingId != null) {
                ConnectionRecord followUp = connectionsByTrackingId.get(record.getRedirectedToId());
                if (followUp != null) {
                    sb.append("  └─► ").append(followUp.toStringWithoutRedirectStatus()).append(" [REDIRECT_FOLLOW]").append("\n");
                    displayedAsFollows.add(followUp.getTrackingId());
                }
            }
        }

        sb.append("Summary: ")
                .append(connections.size()).append(" connections, ")
                .append(getSuccessCount()).append(" successful, ")
                .append(getErrorCount()).append(" errors.");

        return sb.toString();
    }
    
    private String getRedirectStatusForDisplay(ConnectionRecord record) {
        if (record.getRedirectedToId() != null) {
            return " [REDIRECTED]";
        } else if (record.isRedirectFollow()) {
            return " [REDIRECT_FOLLOW]";
        }
        return "";
    }

    /**
     * Updates the server IP address for a connection identified by tracking ID.
     *
     * @param trackingId the unique tracking ID of the connection
     * @param hostAddress the server's IP address to set
     */
    public synchronized void updateServerIpOnConnection(String trackingId, String hostAddress) {
        Map<String, ConnectionRecord> connectionsByTrackingId = sessionConnectionsByTrackingId.get(sessionId);
        if (connectionsByTrackingId != null) {
            ConnectionRecord record = connectionsByTrackingId.get(trackingId);
            if (record != null) {
                logger.debug("Updating server IP address {} for tracking ID: {} in session {}", hostAddress, trackingId, sessionId);
                record.setIpAddress(hostAddress);
            } else {
                logger.debug("No connection found for tracking ID: {} in session {}", trackingId, sessionId);
            }
        }
    }

    /**
     * Determines if all HEAD and main queries returned 404 Not Found status codes.
     *
     * <p>This method is used to identify cases where a resource may be legitimately
     * unavailable, which may require different validation handling or warning generation.</p>
     *
     * @param config the validator configuration containing query settings
     * @return true if all relevant queries returned 404 status, false otherwise
     */
    public synchronized boolean isResourceNotFoundNoteWarning(RDAPValidatorConfiguration config) {
        boolean foundRelevant = false;
        List<ConnectionRecord> connections = sessionConnections.get(sessionId);
        if (connections != null) {
            for (ConnectionRecord record : connections) {
            if (record.isMainConnection() || HEAD.equalsIgnoreCase(record.getHttpMethod())) {
                foundRelevant = true;
                if (record.getStatusCode() != HTTP_NOT_FOUND) {
                    return false;
                }

                // It's ok to return true here so we can log the message in verbose mode
                // If a profile is used, and both HEAD and GET result in 404, then this should be a warning
                if((HEAD.equalsIgnoreCase(record.getHttpMethod()) || GET.equalsIgnoreCase(record.getHttpMethod()))
                        && (config.useRdapProfileFeb2024() || config.useRdapProfileFeb2019())
                        && (config.isGtldRegistrar() || config.isGtldRegistry())) {
                    CommonUtils.addErrorToResultsFile(record.getStatusCode(), -13020, config.getUri().toString(), "This URL returned an HTTP 404 status code that was validly formed. If the provided URL "
                            + "does not reference a registered resource, then this warning may be ignored. If the provided URL does reference a registered resource, then this should be considered an error.");
                }
                // to get the error code we are looking for ->
                // if no profile is selected and a GET results in 404 and no other errors occur, this should show up as a warning
                //  then we put in the error code -13020
                else if(GET.equalsIgnoreCase(record.getHttpMethod())) {
                    CommonUtils.addErrorToResultsFile(record.getStatusCode(), -13020, config.getUri().toString(), "This URL returned an HTTP 404 status code that was validly formed. If the provided URL "
                            + "does not reference a registered resource, then this warning may be ignored. If the provided URL does reference a registered resource, then this should be considered an error.");
                }
            }
        }
        }
        return foundRelevant;
    }

    /**
     * Represents a tracked network connection
     */
    public static class ConnectionRecord {
        private final URI uri;
        private String ipAddress;
        private final NetworkProtocol protocol;
        private int statusCode;
        private Duration duration;
        private ConnectionStatus status;
        private final String httpMethod;
        private final Instant timestamp;
        private Instant startTime;
        private final String trackingId;
        private final boolean mainConnection;
        
        // Redirect tracking fields
        private String parentTrackingId;     // ID of the request that caused this redirect
        private String redirectedToId;      // ID of the request this redirected to
        private boolean isRedirectFollow;   // True if this request was following a redirect

        public ConnectionRecord(URI uri, String ipAddress, NetworkProtocol protocol,
                                int statusCode, Duration duration,
                                ConnectionStatus status, String httpMethod, Instant timestamp,
                                String trackingId, boolean mainConnection) {
            this.uri = uri;
            this.ipAddress = ipAddress;
            this.protocol = protocol;
            this.statusCode = statusCode;
            this.duration = duration;
            this.status = status;
            this.httpMethod = httpMethod;
            this.timestamp = timestamp;
            this.trackingId = trackingId;
            this.mainConnection = mainConnection;
        }

        public ConnectionRecord(URI uri, String ipAddress, NetworkProtocol protocol,
                                int statusCode, Duration duration,
                                ConnectionStatus status, String httpMethod, Instant timestamp,
                                String trackingId) {
            this(uri, ipAddress, protocol, statusCode, duration, status, httpMethod, timestamp, trackingId, false);
        }

        public String getTrackingId() {
            return trackingId;
        }

        public URI getUri() {
            return uri;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public NetworkProtocol getProtocol() {
            return protocol;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public Duration getDuration() {
            return duration;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }

        public ConnectionStatus getStatus() {
            return status;
        }

        public void setStatus(ConnectionStatus status) {
            this.status = status;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public Instant getStartTime() {
            return startTime;
        }

        public void setStartTime(Instant startTime) {
            this.startTime = startTime;
        }

        public boolean isMainConnection() {
            return mainConnection;
        }

        // Redirect tracking getters and setters
        public String getParentTrackingId() {
            return parentTrackingId;
        }

        public void setParentTrackingId(String parentTrackingId) {
            this.parentTrackingId = parentTrackingId;
        }

        public String getRedirectedToId() {
            return redirectedToId;
        }

        public void setRedirectedToId(String redirectedToId) {
            this.redirectedToId = redirectedToId;
        }

        public boolean isRedirectFollow() {
            return isRedirectFollow;
        }

        public void setRedirectFollow(boolean redirectFollow) {
            isRedirectFollow = redirectFollow;
        }

        public String toStringWithoutRedirectStatus() {
            return String.format(
                    "[%s] %s %s to %s (%s) over %s with ID %s - Status: %d, Duration: %s, Result: %s",
                    timestamp,
                    mainConnection ? "[MAIN]" : "",
                    httpMethod,
                    uri,
                    ipAddress,
                    protocol,
                    trackingId,
                    statusCode,
                    duration != null ? duration.toMillis() + "ms" : "unknown",
                    status != null ? status.name() : "in progress"
            );
        }

        @Override
        public String toString() {
            // Build redirect status indicators
            String redirectStatus = "";
            if (redirectedToId != null) {
                redirectStatus = " [REDIRECTED]";
            } else if (isRedirectFollow) {
                redirectStatus = " [REDIRECT_FOLLOW]";
            }
            
            return toStringWithoutRedirectStatus() + redirectStatus;
        }
    }
}