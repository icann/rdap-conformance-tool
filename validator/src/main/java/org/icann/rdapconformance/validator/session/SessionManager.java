package org.icann.rdapconformance.validator.session;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages session lifecycle, tracking, and cleanup for concurrent RDAP validation operations.
 *
 * <p>This class provides comprehensive session management including creation, tracking,
 * access monitoring, and automatic cleanup based on configurable policies. It ensures
 * optimal memory usage and prevents resource leaks in long-running web applications.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>Session creation and validation</li>
 *   <li>Last access time tracking for LRU cleanup</li>
 *   <li>Maximum session limit enforcement</li>
 *   <li>Configurable session timeout policies</li>
 *   <li>Thread-safe concurrent operation</li>
 *   <li>Memory usage monitoring and alerts</li>
 * </ul>
 *
 * <p>The manager supports both automatic cleanup through the SessionCleanupService
 * and manual session management for precise control over session lifecycle.</p>
 *
 * @see SessionCleanupService
 * @since 3.0.0
 */
public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    /**
     * Default session timeout in milliseconds (10 minutes)
     */
    public static final long DEFAULT_SESSION_TIMEOUT_MS = 10 * 60 * 1000;

    /**
     * Default maximum number of concurrent sessions
     */
    public static final int DEFAULT_MAX_SESSIONS = 500;

    /**
     * Singleton instance using volatile for thread-safe lazy initialization
     */
    private static volatile SessionManager instance;

    /**
     * Session metadata storage
     */
    private final ConcurrentHashMap<String, SessionMetadata> sessionMetadata = new ConcurrentHashMap<>();

    /**
     * Current session count for monitoring
     */
    private final AtomicInteger activeSessionCount = new AtomicInteger(0);

    /**
     * Configuration settings
     */
    private volatile long sessionTimeoutMs = DEFAULT_SESSION_TIMEOUT_MS;
    private volatile int maxSessions = DEFAULT_MAX_SESSIONS;

    /**
     * Private constructor for singleton pattern
     */
    private SessionManager() {
    }

    /**
     * Returns the singleton instance using double-checked locking for optimal performance.
     *
     * @return the SessionManager singleton instance
     */
    public static SessionManager getInstance() {
        SessionManager result = instance;
        if (result == null) {
            synchronized (SessionManager.class) {
                result = instance;
                if (result == null) {
                    instance = result = new SessionManager();
                }
            }
        }
        return result;
    }

    /**
     * Registers a new session and tracks its metadata.
     *
     * @param sessionId the session identifier
     * @throws IllegalStateException if maximum session limit is exceeded
     */
    public void registerSession(String sessionId) {
        // Check session limit before creating new session
        if (activeSessionCount.get() >= maxSessions) {
            // Try cleanup first to make room
            int cleanedUp = cleanExpiredSessions();
            logger.info("Session limit reached. Cleaned up {} expired sessions.", cleanedUp);

            if (activeSessionCount.get() >= maxSessions) {
                throw new IllegalStateException(
                    String.format("Maximum session limit (%d) exceeded. Cannot create new session: %s",
                    maxSessions, sessionId));
            }
        }

        SessionMetadata metadata = new SessionMetadata(sessionId, Instant.now());
        SessionMetadata existing = sessionMetadata.put(sessionId, metadata);

        if (existing == null) {
            int newCount = activeSessionCount.incrementAndGet();
            logger.debug("Registered new session: {} (active sessions: {})", sessionId, newCount);
        } else {
            logger.debug("Updated existing session: {}", sessionId);
        }
    }

    /**
     * Updates the last access time for a session.
     *
     * @param sessionId the session identifier
     */
    public void updateSessionAccess(String sessionId) {
        SessionMetadata metadata = sessionMetadata.get(sessionId);
        if (metadata != null) {
            metadata.updateLastAccess();
        }
    }

    /**
     * Checks if a session is active and not expired.
     *
     * @param sessionId the session identifier
     * @return true if the session is active
     */
    public boolean isSessionActive(String sessionId) {
        SessionMetadata metadata = sessionMetadata.get(sessionId);
        if (metadata == null) {
            return false;
        }

        long age = Instant.now().toEpochMilli() - metadata.getLastAccessTime().toEpochMilli();
        return age < sessionTimeoutMs;
    }

    /**
     * Manually removes a session and cleans up its resources.
     *
     * @param sessionId the session identifier
     * @return true if the session was removed
     */
    public boolean removeSession(String sessionId) {
        SessionMetadata removed = sessionMetadata.remove(sessionId);
        if (removed != null) {
            int newCount = activeSessionCount.decrementAndGet();
            logger.debug("Removed session: {} (active sessions: {})", sessionId, newCount);

            // Clean up associated resources
            cleanupSessionResources(sessionId);
            return true;
        }
        return false;
    }

    /**
     * Cleans up expired sessions based on the configured timeout.
     *
     * @return the number of sessions cleaned up
     */
    public int cleanExpiredSessions() {
        Instant cutoff = Instant.now().minusMillis(sessionTimeoutMs);
        int cleanedCount = 0;

        for (SessionMetadata metadata : sessionMetadata.values()) {
            if (metadata.getLastAccessTime().isBefore(cutoff)) {
                if (removeSession(metadata.getSessionId())) {
                    cleanedCount++;
                }
            }
        }

        if (cleanedCount > 0) {
            logger.info("Cleaned up {} expired sessions", cleanedCount);
        }

        return cleanedCount;
    }

    /**
     * Gets the current number of active sessions.
     *
     * @return the active session count
     */
    public int getActiveSessionCount() {
        return activeSessionCount.get();
    }

    /**
     * Sets the session timeout in milliseconds.
     *
     * @param timeoutMs the timeout in milliseconds
     */
    public void setSessionTimeout(long timeoutMs) {
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("Session timeout must be positive");
        }
        this.sessionTimeoutMs = timeoutMs;
        logger.info("Session timeout updated to {} ms", timeoutMs);
    }

    /**
     * Sets the maximum number of concurrent sessions.
     *
     * @param maxSessions the maximum session count
     */
    public void setMaxSessions(int maxSessions) {
        if (maxSessions <= 0) {
            throw new IllegalArgumentException("Max sessions must be positive");
        }
        this.maxSessions = maxSessions;
        logger.info("Maximum sessions updated to {}", maxSessions);
    }

    /**
     * Gets the current session timeout in milliseconds.
     *
     * @return the session timeout
     */
    public long getSessionTimeout() {
        return sessionTimeoutMs;
    }

    /**
     * Gets the maximum number of concurrent sessions.
     *
     * @return the maximum session count
     */
    public int getMaxSessions() {
        return maxSessions;
    }

    /**
     * Resets all session data - FOR TESTING ONLY.
     * This method should never be called in production code.
     */
    public void resetAllSessionsForTesting() {
        sessionMetadata.clear();
        activeSessionCount.set(0);

        // Also reset session-aware components
        org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl.resetAll();
        org.icann.rdapconformance.validator.ConnectionTracker.resetAll();

        logger.debug("Reset all sessions for testing");
    }

    /**
     * Cleans up resources associated with a specific session.
     * This method coordinates cleanup across all session-aware components.
     *
     * @param sessionId the session identifier
     */
    private void cleanupSessionResources(String sessionId) {
        try {
            // Clean up validation results
            org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl.reset(sessionId);

            // Clean up connection tracking
            org.icann.rdapconformance.validator.ConnectionTracker.reset(sessionId);

            logger.debug("Cleaned up resources for session: {}", sessionId);
        } catch (Exception e) {
            logger.error("Error cleaning up resources for session: {}", sessionId, e);
        }
    }

    /**
     * Session metadata for tracking session lifecycle and access patterns.
     */
    private static class SessionMetadata {
        private final String sessionId;
        private final Instant creationTime;
        private volatile Instant lastAccessTime;

        SessionMetadata(String sessionId, Instant creationTime) {
            this.sessionId = sessionId;
            this.creationTime = creationTime;
            this.lastAccessTime = creationTime;
        }

        public String getSessionId() {
            return sessionId;
        }

        public Instant getCreationTime() {
            return creationTime;
        }

        public Instant getLastAccessTime() {
            return lastAccessTime;
        }

        public void updateLastAccess() {
            this.lastAccessTime = Instant.now();
        }
    }
}