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
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.icann.rdapconformance.validator.CommonUtils.*;

/**
 * Connection tracker that monitors all HTTP connections made during RDAP validation.
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
 * <p>Now integrated into the QueryContext architecture for thread-safe operation in
 * concurrent validation environments. Each QueryContext has its own ConnectionTracker
 * instance to ensure complete isolation between validation sessions.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * ConnectionTracker tracker = queryContext.getConnectionTracker();
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
    private final List<ConnectionRecord> connections;
    private final Map<String, ConnectionRecord> connectionsByTrackingId;
    private ConnectionRecord currentConnection;
    private ConnectionRecord lastMainConnection;

    // Public constructor for QueryContext usage
    public ConnectionTracker() {
        this.connections = Collections.synchronizedList(new ArrayList<>());
        this.connectionsByTrackingId = Collections.synchronizedMap(new HashMap<>());
        this.lastMainConnection = null;
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
        return connectionsByTrackingId.get(trackingId);
    }

    /**
     * Get the status code of a connection by its tracking ID
     *
     * @param trackingId The tracking ID to look up
     * @return The status code, or 0 if the connection is not found
     */
    public synchronized int getStatusCodeByTrackingId(String trackingId) {
        ConnectionRecord record = connectionsByTrackingId.get(trackingId);
        return record != null ? record.getStatusCode() : 0;
    }



    /**
     * Start tracking a new connection with explicit protocol (QueryContext-aware)
     * @param uri The URI being requested
     * @param httpMethod The HTTP method being used
     * @param isMainConnection Whether this is a main connection
     * @param protocol The network protocol being used
     * @return The tracking ID of the new connection
     */
    public synchronized String startTrackingNewConnection(URI uri, String httpMethod, boolean isMainConnection, NetworkProtocol protocol) {
        String trackingId = generateTrackingId();
        ConnectionRecord record = new ConnectionRecord(
                uri,
                "UNKNOWN", // IP address will be set later
                protocol,
                ZERO,  // Status code not yet known
                null,  // Duration not yet known
                null,  // Status not yet known
                httpMethod,
                Instant.now(),
                trackingId,
                isMainConnection
        );
        record.setStartTime(Instant.now());
        connections.add(record);
        connectionsByTrackingId.put(trackingId, record);
        currentConnection = record;

        if (isMainConnection) {
            lastMainConnection = record;
            logger.debug("Started tracking main connection: {} using protocol: {}", trackingId, protocol);
        } else {
            logger.debug("Started tracking connection: {} using protocol: {}", trackingId, protocol);
        }

        return trackingId;
    }

    /**
     * Update the current connection with a new status
     * @param status The connection status to set
     */
    public synchronized void updateCurrentConnection(ConnectionStatus status) {
        if (currentConnection != null) {
            currentConnection.setStatus(status);
            logger.debug("Updated current connection with status: {}", status);
        } else {
            logger.warn("Attempted to update current connection, but no current connection exists");
        }
    }

    /**
     * Update the current connection with a new IP address
     * @param remoteAddress The new remote IP address to set
     */
    public synchronized void updateIPAddressOnCurrentConnection(String remoteAddress) {
        if (currentConnection != null) {
            currentConnection.setIpAddress(remoteAddress);
            logger.debug("Updated current connection with ipAddress: {}", remoteAddress);
        } else {
            logger.warn("Attempted to update current connection, but no current connection exists");
        }
    }

    /**
     * Update a connection's IP address by tracking ID
     * @param trackingId The tracking ID of the connection
     * @param remoteAddress The new remote IP address to set
     * @return true if the connection was found and updated, false otherwise
     */
    public synchronized boolean updateIPAddressById(String trackingId, String remoteAddress) {
        ConnectionRecord record = connectionsByTrackingId.get(trackingId);
        if (record != null) {
            record.setIpAddress(remoteAddress);
            logger.debug("Updated connection {} with ipAddress: {}", trackingId, remoteAddress);
            return true;
        } else {
            logger.warn("Attempted to update connection {}, but no connection exists with that tracking ID", trackingId);
            return false;
        }
    }

    /**
     * Complete tracking the current connection with status code and status
     * @param statusCode The HTTP status code
     * @param status The connection status
     */
    public synchronized void completeCurrentConnection(int statusCode, ConnectionStatus status) {
        if (currentConnection != null) {
            Duration duration = Duration.between(currentConnection.getStartTime(), Instant.now());
            currentConnection.setStatusCode(statusCode);
            currentConnection.setDuration(duration);
            currentConnection.setStatus(status);

            // If this was a main connection that's being completed, keep track of it
            if (currentConnection.isMainConnection()) {
                lastMainConnection = currentConnection;
            }

            logger.debug("Completed current connection with tracking id: {}", currentConnection.trackingId);
            currentConnection = null;
        } else {
            logger.warn("Attempted to complete current connection, but no current connection exists");
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
        connections.add(record);
        connectionsByTrackingId.put(trackingId, record);
        currentConnection = record;

        if (isMainConnection) {
            lastMainConnection = record;
            logger.debug("Started tracking main connection: {}", trackingId);
        } else {
            logger.debug("Started tracking connection: {}", trackingId);
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
                    lastMainConnection = record;
                }

                // If this was the current connection, clear it
                if (currentConnection == record) {
                    currentConnection = null;
                }

                logger.debug("Completed tracking connection: {}", record);
                return;
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
        ConnectionRecord record = connectionsByTrackingId.get(trackingId);
        if (record != null && record.getDuration() == null) {
            Duration duration = Duration.between(record.getStartTime(), Instant.now());
            record.setStatusCode(statusCode);
            record.setDuration(duration);
            record.setStatus(status);

            // If this was a main connection that's being completed, keep track of it
            if (record.isMainConnection()) {
                lastMainConnection = record;
            }

            // If this was the current connection, clear it
            if (currentConnection == record) {
                currentConnection = null;
            }

            logger.debug("Completed tracking connection by ID: {}", record);
            return true;
        }
        return false;
    }

    /**
     * Get the current connection
     * @return The current connection record
     */
    public synchronized ConnectionRecord getCurrentConnection() {
        return currentConnection;
    }

    /**
     * Get all connections
     * @return A copy of the connections list
     */
    public synchronized List<ConnectionRecord> getConnections() {
        return new ArrayList<>(connections);
    }

    /**
     * Get the most recent connection
     * @return The last connection record
     */
    public synchronized ConnectionRecord getLastConnection() {
        if (connections.isEmpty()) {
            return null;
        }
        return connections.get(connections.size() - 1);
    }

    /**
     * Get the most recent main connection
     * @return The last main connection record
     */
    public synchronized ConnectionRecord getLastMainConnection() {
        return lastMainConnection;
    }

    /**
     * Count connections with error status
     * @return The number of connections with non-success status
     */
    public synchronized int getErrorCount() {
        int count = ZERO;
        for (ConnectionRecord record : connections) {
            if (record.getStatus() != null && record.getStatus() != ConnectionStatus.SUCCESS) {
                count++;
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
        for (ConnectionRecord record : connections) {
            if (record.getStatus() == ConnectionStatus.SUCCESS) {
                count++;
            }
        }
        return count;
    }

    /**
     * Reset the connection tracker
     */
    public synchronized void reset() {
        connections.clear();
        connectionsByTrackingId.clear();
        currentConnection = null;
        lastMainConnection = null;
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
        if (connections.isEmpty()) {
            return "No connections tracked";
        }

        StringBuilder sb = new StringBuilder("Connection Tracking Report:\n");

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
            if (record.getRedirectedToId() != null) {
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
        ConnectionRecord record = connectionsByTrackingId.get(trackingId);
        if (record != null) {
            logger.debug("Updating server IP address {} for tracking ID: {}", hostAddress, trackingId);
            record.setIpAddress(hostAddress);
        } else {
            logger.debug("No connection found for tracking ID: {}", trackingId);
        }
    }

    /**
     * Checks if all HEAD and main (GET) queries returned 404 Not Found status codes.
     *
     * <p>This is a pure query method with no side effects - it only checks the connection
     * records and returns a boolean. Use this method when you need to check 404 status
     * without adding any warnings to results.</p>
     *
     * @return true if there are relevant queries and all returned 404 status, false otherwise
     */
    public synchronized boolean areAllRelevantQueriesNotFound() {
        boolean foundRelevant = false;
        for (ConnectionRecord record : connections) {
            if (record.isMainConnection() || HEAD.equalsIgnoreCase(record.getHttpMethod())) {
                foundRelevant = true;
                if (record.getStatusCode() != HTTP_NOT_FOUND) {
                    return false;
                }
            }
        }
        return foundRelevant;
    }

    /**
     * Determines if all HEAD and main queries returned 404 Not Found status codes.
     *
     * <p>This method is used to identify cases where a resource may be legitimately
     * unavailable, which may require different validation handling or warning generation.</p>
     *
     * @param queryContext the QueryContext for thread-safe error reporting
     * @param config the validator configuration containing query settings
     * @return true if all relevant queries returned 404 status, false otherwise
     * @deprecated Use {@link #areAllRelevantQueriesNotFound()} for the pure check and
     *             {@link CommonUtils#handleResourceNotFoundWarning} for the full handling.
     */
    @Deprecated
    public synchronized boolean isResourceNotFoundNoteWarning(QueryContext queryContext, RDAPValidatorConfiguration config) {
        boolean foundRelevant = false;
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
                    queryContext.addError(record.getStatusCode(), -13020, config.getUri().toString(), "This URL returned an HTTP 404 status code that was validly formed. If the provided URL "
                            + "does not reference a registered resource, then this warning may be ignored. If the provided URL does reference a registered resource, then this should be considered an error.");
                }
                // to get the error code we are looking for ->
                // if no profile is selected and a GET results in 404 and no other errors occur, this should show up as a warning
                //  then we put in the error code -13020
                else if(GET.equalsIgnoreCase(record.getHttpMethod())) {
                    queryContext.addError(record.getStatusCode(), -13020, config.getUri().toString(), "This URL returned an HTTP 404 status code that was validly formed. If the provided URL "
                            + "does not reference a registered resource, then this warning may be ignored. If the provided URL does reference a registered resource, then this should be considered an error.");
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