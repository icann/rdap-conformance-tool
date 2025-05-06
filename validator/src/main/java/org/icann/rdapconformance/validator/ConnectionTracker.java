package org.icann.rdapconformance.validator;

import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionTracker {
    private static final ConnectionTracker INSTANCE = new ConnectionTracker();
    private static final Logger logger = LoggerFactory.getLogger(ConnectionTracker.class);
    private final List<ConnectionRecord> connections;
    private ConnectionRecord currentConnection;

    private ConnectionTracker() {
        this.connections = Collections.synchronizedList(new ArrayList<>());
    }

    public static ConnectionTracker getInstance() {
        return INSTANCE;
    }

    /**
     * Start tracking a new connection using the current NetworkInfo state
     * @param uri The URI being requested
     */
    public synchronized void startTrackingNewConnection(URI uri) {
        ConnectionRecord record = new ConnectionRecord(
            uri,
            NetworkInfo.getServerIpAddress(),
            NetworkInfo.getNetworkProtocol(),
            ZERO,  // Status code not yet known
            null,  // Duration not yet known
            null,  // Status not yet known
            NetworkInfo.getHttpMethod(),
            Instant.now()
        );
        record.setStartTime(Instant.now());
        connections.add(record);
        currentConnection = record;
        logger.debug("Started tracking connection: {}", record);
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
            logger.info("Completed current connection: {}", currentConnection);
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
     */
    public synchronized void startTracking(URI uri, String ipAddress, NetworkProtocol protocol, String httpMethod) {
        ConnectionRecord record = new ConnectionRecord(
            uri,
            ipAddress,
            protocol,
            0,
            null,
            null,
            httpMethod,
            Instant.now()
        );
        record.setStartTime(Instant.now());
        connections.add(record);
        currentConnection = record;
        logger.debug("Started tracking connection: {}", record);
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
        for (int i = connections.size() - 1; i >= 0; i--) {
            ConnectionRecord record = connections.get(i);
            if (record.getUri().equals(uri) && record.getIpAddress().equals(ipAddress) &&
                record.getStartTime() != null && record.getDuration() == null) {

                Duration duration = Duration.between(record.getStartTime(), Instant.now());
                record.setStatusCode(statusCode);
                record.setDuration(duration);
                record.setStatus(status);

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
     * Count connections with error status
     * @return The number of connections with non-success status
     */
    public synchronized int getErrorCount() {
        int count = 0;
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
        currentConnection = null;
    }

    /**
     * Safely gets the status code of the current connection as a string.
     * Returns "-" if there is no current connection or if the status code is 0.
     *
     * @return The status code as a string, or "-" if unavailable
     */
    public static String getCurrentStatusCodeAsString() {
        ConnectionTracker tracker = getInstance();
        ConnectionRecord currentConnection = tracker.getLastConnection(); // Given the way it works,  the _last_ connection is the one we want. Current would be set to null.

        if (currentConnection == null || currentConnection.getStatusCode() == 0) {
            return "-";
        }

        return String.valueOf(currentConnection.getStatusCode());
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

    /**
     * Represents a tracked network connection
     */
    public static class ConnectionRecord {
        private final URI uri;
        private final String ipAddress;
        private final NetworkProtocol protocol;
        private int statusCode;
        private Duration duration;
        private ConnectionStatus status;
        private final String httpMethod;
        private final Instant timestamp;
        private Instant startTime;

        public ConnectionRecord(URI uri, String ipAddress, NetworkProtocol protocol,
                                int statusCode, Duration duration,
                                ConnectionStatus status, String httpMethod, Instant timestamp) {
            this.uri = uri;
            this.ipAddress = ipAddress;
            this.protocol = protocol;
            this.statusCode = statusCode;
            this.duration = duration;
            this.status = status;
            this.httpMethod = httpMethod;
            this.timestamp = timestamp;
        }

        public URI getUri() {
            return uri;
        }

        public String getIpAddress() {
            return ipAddress;
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

        @Override
        public String toString() {
            return String.format(
                "[%s] %s to %s (%s) over %s - Status: %d, Duration: %s, Result: %s",
                timestamp,
                httpMethod,
                uri,
                ipAddress,
                protocol,
                statusCode,
                duration != null ? duration.toMillis() + "ms" : "unknown",
                status != null ? status.name() : "in progress"
            );
        }
    }
}