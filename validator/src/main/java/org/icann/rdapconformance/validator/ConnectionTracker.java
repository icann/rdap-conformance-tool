package org.icann.rdapconformance.validator;

import static org.icann.rdapconformance.validator.CommonUtils.HEAD;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_NOT_FOUND;
import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResultFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionTracker {
    private static final ConnectionTracker INSTANCE = new ConnectionTracker();
    private static final Logger logger = LoggerFactory.getLogger(ConnectionTracker.class);
    private final List<ConnectionRecord> connections;
    private final Map<String, ConnectionRecord> connectionsByTrackingId;
    private ConnectionRecord currentConnection;
    private ConnectionRecord lastMainConnection;

    private ConnectionTracker() {
        this.connections = Collections.synchronizedList(new ArrayList<>());
        this.connectionsByTrackingId = Collections.synchronizedMap(new HashMap<>());
        this.lastMainConnection = null;
    }

    public static ConnectionTracker getInstance() {
        return INSTANCE;
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
     * Update the current connection with a new status
     * @param status The connection status to set
     */
    public synchronized void updateCurrentConnection(ConnectionStatus status) {
        if (currentConnection != null) {
            currentConnection.setStatus(status);
            logger.info("Updated current connection with status: {}", status);
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
            logger.info("Updated current connection with ipAddress: {}", remoteAddress);
        } else {
            logger.warn("Attempted to update current connection, but no current connection exists");
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

            logger.info("Completed current connection with tracking id: {}", currentConnection.trackingId);
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

                logger.info("Completed tracking connection: {}", record);
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

            logger.info("Completed tracking connection by ID: {}", record);
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


    @Override
    public synchronized String toString() {
        if (connections.isEmpty()) {
            return "No connections tracked";
        }

        StringBuilder sb = new StringBuilder("Connection Tracking Report:\n");

        for (ConnectionRecord record : connections) {
            sb.append(record.toString()).append("\n");
        }

        sb.append("Summary: ")
          .append(connections.size()).append(" connections, ")
          .append(getSuccessCount()).append(" successful, ")
          .append(getErrorCount()).append(" errors.");

        return sb.toString();
    }

    public synchronized void updateServerIpOnConnection(String trackingId, String hostAddress) {
        ConnectionRecord record = connectionsByTrackingId.get(trackingId);
        if (record != null) {
            logger.info("Updating server IP address {} for tracking ID: {}", hostAddress, trackingId);
            record.setIpAddress(hostAddress);
        } else {
            logger.info("No connection found for tracking ID: {}", trackingId);
        }
    }

public synchronized boolean isResourceNotFoundNoteWarning(RDAPValidatorConfiguration config) {
    boolean foundRelevant = false;
    for (ConnectionRecord record : connections) {
        if (record.isMainConnection() || HEAD.equalsIgnoreCase(record.getHttpMethod())) {
            foundRelevant = true;
            if (record.getStatusCode() != HTTP_NOT_FOUND) {
                return false;
            }
            // It's ok to return true here so we can log the message in verbose mode, However....
            // to get the error code we are looking for -> we also need to check the  config that a Gtld profile was selected AND there are no other errors;
            //  then and only then we put in the error code -13020
            if(config.useRdapProfileFeb2024() && RDAPValidationResultFile.getInstance().getErrorCount() < ONE  && (config.isGtldRegistrar() || config.isGtldRegistry())) {
                CommonUtils.addErrorToResultsFile(record.getStatusCode(), -13020, config.getUri().toString(), "This URL returned an HTTP 404 status code that was validly formed. If the provided URL "
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

        @Override
        public String toString() {
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
    }
}