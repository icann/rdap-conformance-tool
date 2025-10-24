package org.icann.rdapconformance.validator.session;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;

/**
 * Comprehensive tests for session lifecycle management, cleanup, and memory leak prevention.
 */
public class SessionLifecycleTest {

    private SessionManager sessionManager;
    private SessionCleanupService cleanupService;

    @BeforeMethod
    public void setUp() {
        sessionManager = SessionManager.getInstance();
        cleanupService = SessionCleanupService.getInstance();

        // Reset to clean state
        if (cleanupService.isRunning()) {
            cleanupService.stop();
        }

        // Reset all session state for proper test isolation
        sessionManager.resetAllSessionsForTesting();

        // Reset configuration to defaults
        sessionManager.setSessionTimeout(SessionManager.DEFAULT_SESSION_TIMEOUT_MS);
        sessionManager.setMaxSessions(SessionManager.DEFAULT_MAX_SESSIONS);
        cleanupService.setCleanupInterval(SessionCleanupService.DEFAULT_CLEANUP_INTERVAL_MINUTES);
    }

    @AfterMethod
    public void tearDown() {
        if (cleanupService.isRunning()) {
            cleanupService.stop();
        }

        // Clean up any sessions created during the test
        sessionManager.resetAllSessionsForTesting();
    }

    /**
     * Test 1: Session Registration and Tracking
     */
    @Test
    public void testSessionRegistrationAndTracking() {
        int initialCount = sessionManager.getActiveSessionCount();

        // Register multiple sessions
        String[] sessionIds = {"session1", "session2", "session3"};
        for (String sessionId : sessionIds) {
            sessionManager.registerSession(sessionId);
            assertTrue(sessionManager.isSessionActive(sessionId), "Session should be active after registration");
        }

        assertEquals(sessionManager.getActiveSessionCount(), initialCount + sessionIds.length,
            "Session count should increase by the number of registered sessions");

        // Test duplicate registration (should not increase count)
        sessionManager.registerSession("session1");
        assertEquals(sessionManager.getActiveSessionCount(), initialCount + sessionIds.length,
            "Duplicate registration should not increase count");
    }

    /**
     * Test 2: Session Timeout and Expiration
     */
    @Test
    public void testSessionTimeoutAndExpiration() throws InterruptedException {
        // Set a very short timeout for testing
        long originalTimeout = sessionManager.getSessionTimeout();
        sessionManager.setSessionTimeout(100); // 100ms

        try {
            String sessionId = "timeout-test-session";
            sessionManager.registerSession(sessionId);

            // Initially active
            assertTrue(sessionManager.isSessionActive(sessionId), "Session should be active initially");

            // Wait for timeout
            Thread.sleep(150);

            // Should be expired now
            assertFalse(sessionManager.isSessionActive(sessionId), "Session should be expired after timeout");

            // Cleanup should remove it
            int cleanedUp = sessionManager.cleanExpiredSessions();
            assertEquals(cleanedUp, 1, "Should clean up 1 expired session");

        } finally {
            sessionManager.setSessionTimeout(originalTimeout);
        }
    }

    /**
     * Test 3: Maximum Session Limit Enforcement
     */
    @Test
    public void testMaxSessionLimitEnforcement() {
        int originalMaxSessions = sessionManager.getMaxSessions();
        sessionManager.setMaxSessions(5); // Small limit for testing

        try {
            // Register up to the limit
            for (int i = 0; i < 5; i++) {
                sessionManager.registerSession("session" + i);
            }

            // Attempting to register beyond the limit should fail
            assertThrows("Should throw exception when exceeding session limit", IllegalStateException.class, () -> {
                sessionManager.registerSession("session-overflow");
            });

        } finally {
            sessionManager.setMaxSessions(originalMaxSessions);
        }
    }

    /**
     * Test 4: Concurrent Session Registration and Access
     */
    @Test
    public void testConcurrentSessionRegistrationAndAccess() throws Exception {
        final int CONCURRENT_SESSIONS = 100;
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_SESSIONS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_SESSIONS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < CONCURRENT_SESSIONS; i++) {
            final String sessionId = "concurrent-session-" + i;
            executor.submit(() -> {
                try {
                    sessionManager.registerSession(sessionId);
                    sessionManager.updateSessionAccess(sessionId);

                    if (sessionManager.isSessionActive(sessionId)) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }

                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.err.println("Error with session " + sessionId + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "All operations should complete within 30 seconds");
        executor.shutdown();

        assertEquals(failureCount.get(), 0, "No failures should occur during concurrent registration");
        assertEquals(successCount.get(), CONCURRENT_SESSIONS, "All sessions should be registered successfully");
    }

    /**
     * Test 5: Automatic Cleanup Service
     */
    @Test
    public void testAutomaticCleanupService() throws InterruptedException {
        // Set short intervals for testing
        cleanupService.setCleanupInterval(1); // 1 minute
        sessionManager.setSessionTimeout(500); // 500ms

        // Register some sessions
        for (int i = 0; i < 5; i++) {
            sessionManager.registerSession("cleanup-test-" + i);
        }

        int initialCount = sessionManager.getActiveSessionCount();
        assertTrue(initialCount >= 5, "Should have at least 5 sessions");

        // Start cleanup service
        cleanupService.start();
        assertTrue(cleanupService.isRunning(), "Cleanup service should be running");

        // Wait for sessions to expire
        Thread.sleep(600);

        // Manually trigger cleanup for immediate testing
        int cleanedUp = cleanupService.cleanupNow();
        assertTrue(cleanedUp >= 5, "Should clean up at least 5 expired sessions");

        cleanupService.stop();
        assertFalse(cleanupService.isRunning(), "Cleanup service should be stopped");
    }

    /**
     * Test 6: Memory Leak Prevention
     */
    @Test
    public void testMemoryLeakPrevention() throws InterruptedException {
        // Create many sessions quickly
        final int SESSION_COUNT = 1000;
        long originalTimeout = sessionManager.getSessionTimeout();
        int originalMaxSessions = sessionManager.getMaxSessions();

        sessionManager.setSessionTimeout(100); // Very short timeout
        sessionManager.setMaxSessions(1500); // Temporarily increase limit for this test

        try {
            for (int i = 0; i < SESSION_COUNT; i++) {
                sessionManager.registerSession("memory-test-" + i);
            }

            int initialCount = sessionManager.getActiveSessionCount();
            assertTrue(initialCount >= SESSION_COUNT, "Should have created many sessions");

            // Wait for expiration
            Thread.sleep(200);

            // Clean up
            int cleanedUp = sessionManager.cleanExpiredSessions();
            int finalCount = sessionManager.getActiveSessionCount();

            System.out.printf("Memory leak test: %d sessions created, %d cleaned up, %d remaining%n",
                SESSION_COUNT, cleanedUp, finalCount);

            assertTrue(cleanedUp >= SESSION_COUNT * 0.9, "Should clean up most expired sessions");
            assertTrue(finalCount < initialCount, "Session count should decrease after cleanup");

        } finally {
            sessionManager.setSessionTimeout(originalTimeout);
            sessionManager.setMaxSessions(originalMaxSessions);
        }
    }

    /**
     * Test 7: Session Context Integration
     */
    @Test
    public void testSessionContextIntegration() {
        // Test valid session ID
        String validSessionId = "valid-session-123";
        String validated = SessionContext.validateSessionId(validSessionId);
        assertEquals(validated, validSessionId, "Valid session ID should pass through unchanged");

        // Test null session ID
        String defaulted = SessionContext.validateSessionId(null);
        assertEquals(defaulted, SessionContext.DEFAULT_SESSION_ID, "Null should default to legacy session");

        // Test empty session ID
        String emptyDefaulted = SessionContext.validateSessionId("");
        assertEquals(emptyDefaulted, SessionContext.DEFAULT_SESSION_ID, "Empty should default to legacy session");

        // Test whitespace-only session ID
        String whitespaceDefaulted = SessionContext.validateSessionId("   ");
        assertEquals(whitespaceDefaulted, SessionContext.DEFAULT_SESSION_ID, "Whitespace should default to legacy session");

        // Test default session detection
        assertTrue(SessionContext.isDefaultSession(SessionContext.DEFAULT_SESSION_ID),
            "Should correctly identify default session");
        assertFalse(SessionContext.isDefaultSession("custom-session"),
            "Should correctly identify non-default session");

        // Test new session ID creation
        String newSessionId1 = SessionContext.createNewSessionId();
        String newSessionId2 = SessionContext.createNewSessionId();
        assertNotNull(newSessionId1, "New session ID should not be null");
        assertNotNull(newSessionId2, "New session ID should not be null");
        assertNotEquals(newSessionId1, newSessionId2, "Each new session ID should be unique");
    }

    /**
     * Test 8: Cleanup Service Error Resilience
     */
    @Test
    public void testCleanupServiceErrorResilience() {
        cleanupService.start();

        // The cleanup service should continue running even if errors occur
        // This is more of a resilience test - the service should not crash
        assertTrue(cleanupService.isRunning(), "Service should start successfully");

        // Simulate some load during cleanup
        for (int i = 0; i < 10; i++) {
            sessionManager.registerSession("resilience-test-" + i);
        }

        // Trigger cleanup manually to test error handling
        int cleaned = cleanupService.cleanupNow();
        assertTrue(cleaned >= 0, "Cleanup should return a non-negative count");

        // Service should still be running
        assertTrue(cleanupService.isRunning(), "Service should continue running after cleanup");

        cleanupService.stop();
        assertFalse(cleanupService.isRunning(), "Service should stop cleanly");
    }

    /**
     * Test 9: Configuration Changes During Operation
     */
    @Test
    public void testConfigurationChangesDuringOperation() {
        // Test timeout changes
        long originalTimeout = sessionManager.getSessionTimeout();
        sessionManager.setSessionTimeout(5000);
        assertEquals(sessionManager.getSessionTimeout(), 5000, "Timeout should be updated");

        // Test max sessions changes
        int originalMaxSessions = sessionManager.getMaxSessions();
        sessionManager.setMaxSessions(100);
        assertEquals(sessionManager.getMaxSessions(), 100, "Max sessions should be updated");

        // Test cleanup interval changes
        int originalInterval = cleanupService.getCleanupInterval();
        cleanupService.setCleanupInterval(10);
        assertEquals(cleanupService.getCleanupInterval(), 10, "Cleanup interval should be updated");

        // Restore original values
        sessionManager.setSessionTimeout(originalTimeout);
        sessionManager.setMaxSessions(originalMaxSessions);
        cleanupService.setCleanupInterval(originalInterval);
    }

    /**
     * Test 10: Session Removal and Resource Cleanup
     */
    @Test
    public void testSessionRemovalAndResourceCleanup() {
        String sessionId = "removal-test-session";
        sessionManager.registerSession(sessionId);

        assertTrue(sessionManager.isSessionActive(sessionId), "Session should be active after registration");

        // Manual removal
        boolean removed = sessionManager.removeSession(sessionId);
        assertTrue(removed, "Session should be removed successfully");

        assertFalse(sessionManager.isSessionActive(sessionId), "Session should not be active after removal");

        // Attempting to remove again should return false
        boolean removedAgain = sessionManager.removeSession(sessionId);
        assertFalse(removedAgain, "Second removal attempt should return false");
    }
}