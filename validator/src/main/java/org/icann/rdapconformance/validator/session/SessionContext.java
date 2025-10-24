package org.icann.rdapconformance.validator.session;

import java.util.UUID;

/**
 * Manages session context and provides consistent session ID handling across the RDAP validation framework.
 *
 * <p>This class ensures proper session isolation by providing validated session IDs and preventing
 * accidental cross-session contamination. It supports both explicit session management for concurrent
 * web applications and backward compatibility for legacy single-threaded usage.</p>
 *
 * <p>Key responsibilities include:</p>
 * <ul>
 *   <li>Session ID validation and sanitization</li>
 *   <li>Default session management for backward compatibility</li>
 *   <li>Session context propagation throughout the validation workflow</li>
 *   <li>Prevention of null or empty session IDs that could cause session mixing</li>
 * </ul>
 *
 * @since 3.0.0
 */
public class SessionContext {

    /**
     * Default session ID used for backward compatibility with single-threaded usage.
     * This ensures existing code continues to work while providing proper isolation.
     */
    public static final String DEFAULT_SESSION_ID = "default-legacy-session";

    /**
     * Validates and returns a session ID, providing a default for backward compatibility.
     *
     * @param sessionId the session ID to validate (may be null for legacy code)
     * @return a valid session ID, never null or empty
     */
    public static String validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return DEFAULT_SESSION_ID;
        }
        return sessionId.trim();
    }

    /**
     * Validates a session ID for new concurrent usage, requiring explicit session management.
     *
     * @param sessionId the session ID to validate
     * @return the validated session ID
     * @throws IllegalArgumentException if sessionId is null or empty
     */
    public static String requireValidSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty. Use a valid session identifier to ensure proper isolation.");
        }
        return sessionId.trim();
    }

    /**
     * Creates a new unique session ID for use in concurrent validation scenarios.
     *
     * @return a new unique session ID
     */
    public static String createNewSessionId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Checks if a session ID represents the default legacy session.
     *
     * @param sessionId the session ID to check
     * @return true if this is the default legacy session
     */
    public static boolean isDefaultSession(String sessionId) {
        return DEFAULT_SESSION_ID.equals(sessionId);
    }
}