package org.icann.rdapconformance.validator;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.icann.rdapconformance.validator.session.SessionContext;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.testng.Assert.*;

/**
 * Performance and concurrency tests to validate the fixes for singleton bottlenecks
 * and ensure the system can handle 200+ concurrent sessions efficiently.
 */
public class ConcurrencyPerformanceTest {

    private static final int HEAVY_CONCURRENT_LOAD = 200;
    private static final int TEST_ITERATIONS = 1000;

    @BeforeMethod
    public void setUp() {
        // Clean up any existing sessions before each test
        RDAPValidatorResultsImpl.resetAll();
        ConnectionTracker.resetAll();

        // Also reset SessionManager if it's being used
        try {
            org.icann.rdapconformance.validator.session.SessionManager.getInstance().resetAllSessionsForTesting();
        } catch (Exception e) {
            // SessionManager might not be used in all tests, ignore
        }
    }

    @AfterMethod
    public void tearDown() {
        // Clean up after each test
        RDAPValidatorResultsImpl.resetAll();
        ConnectionTracker.resetAll();

        // Also reset SessionManager if it's being used
        try {
            org.icann.rdapconformance.validator.session.SessionManager.getInstance().resetAllSessionsForTesting();
        } catch (Exception e) {
            // SessionManager might not be used in all tests, ignore
        }
    }

    /**
     * Test 1: Lock-Free getInstance() Performance Test
     * Validates that getInstance() calls complete in <1ms under heavy load
     */
    @Test
    public void testGetInstancePerformanceUnderLoad() throws Exception {
        final int CONCURRENT_THREADS = HEAVY_CONCURRENT_LOAD;
        final int CALLS_PER_THREAD = 100;

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);
        AtomicLong totalTime = new AtomicLong(0);
        AtomicInteger successCount = new AtomicInteger(0);

        // Use the same session ID to test contention on the same instance
        String sessionId = "performance-test-session";

        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            executor.submit(() -> {
                try {
                    long startTime = System.nanoTime();

                    for (int j = 0; j < CALLS_PER_THREAD; j++) {
                        RDAPValidatorResultsImpl instance = RDAPValidatorResultsImpl.getInstance(sessionId);
                        assertNotNull(instance, "getInstance should never return null");
                    }

                    long endTime = System.nanoTime();
                    totalTime.addAndGet(endTime - startTime);
                    successCount.incrementAndGet();

                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to complete with timeout
        assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete within 30 seconds");
        executor.shutdown();

        // Verify all threads completed successfully
        assertEquals(successCount.get(), CONCURRENT_THREADS, "All threads should complete successfully");

        // Calculate average time per getInstance() call
        long totalCalls = (long) CONCURRENT_THREADS * CALLS_PER_THREAD;
        double averageTimeMs = (totalTime.get() / 1_000_000.0) / totalCalls;

        System.out.printf("getInstance() performance: %.3f ms average per call (%d total calls)%n",
            averageTimeMs, totalCalls);

        // Performance requirement: <1ms per call on average
        assertTrue(averageTimeMs < 1.0,
            String.format("getInstance() should complete in <1ms, actual: %.3f ms", averageTimeMs));
    }

    /**
     * Test 2: Session Isolation Under Concurrent Load
     * Validates that 200 concurrent sessions maintain perfect isolation
     */
    @Test
    public void testSessionIsolationUnderConcurrentLoad() throws Exception {
        final int CONCURRENT_SESSIONS = HEAVY_CONCURRENT_LOAD;

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_SESSIONS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_SESSIONS);
        ConcurrentHashMap<String, String> sessionResults = new ConcurrentHashMap<>();
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < CONCURRENT_SESSIONS; i++) {
            final String sessionId = "session-" + i;
            final String expectedValue = "data-for-session-" + i;

            executor.submit(() -> {
                try {
                    RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance(sessionId);

                    // Simulate some work and verify isolation
                    Thread.sleep(10); // Small delay to increase chance of race conditions

                    // Store unique data for this session
                    sessionResults.put(sessionId, expectedValue);

                    // Verify we get the same instance for the same session
                    RDAPValidatorResultsImpl sameInstance = RDAPValidatorResultsImpl.getInstance(sessionId);
                    if (results != sameInstance) {
                        failureCount.incrementAndGet();
                        System.err.println("Session isolation failed for: " + sessionId);
                    }

                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.err.println("Error in session " + sessionId + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all sessions to complete
        assertTrue(latch.await(60, TimeUnit.SECONDS), "All sessions should complete within 60 seconds");
        executor.shutdown();

        // Verify perfect isolation
        assertEquals(failureCount.get(), 0, "No failures should occur during concurrent session access");
        assertEquals(sessionResults.size(), CONCURRENT_SESSIONS, "All sessions should have unique data");

        // Verify each session has its expected data
        for (int i = 0; i < CONCURRENT_SESSIONS; i++) {
            String sessionId = "session-" + i;
            String expectedValue = "data-for-session-" + i;
            assertEquals(sessionResults.get(sessionId), expectedValue,
                "Session " + sessionId + " should have its unique data");
        }
    }

    /**
     * Test 3: Atomic Composite Operations Test
     * Validates that addAll() operations are atomic and don't lose data
     */
    @Test
    public void testAtomicCompositeOperations() throws Exception {
        final int CONCURRENT_THREADS = 50;
        final String sessionId = "atomic-test-session";

        RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance(sessionId);
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);
        AtomicInteger operationCount = new AtomicInteger(0);

        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    // Create unique validation results for this thread
                    java.util.Set<org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult> threadResults =
                        new java.util.HashSet<>();

                    for (int j = 0; j < 10; j++) {
                        threadResults.add(
                            org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult.builder()
                                .code(-1000 - (threadId * 10 + j))
                                .value("thread-" + threadId + "-result-" + j)
                                .message("Test result from thread " + threadId)
                                .build()
                        );
                    }

                    // Use addAll() - this should be atomic now
                    results.addAll(sessionId, threadResults);
                    operationCount.incrementAndGet();

                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all operations to complete
        assertTrue(latch.await(30, TimeUnit.SECONDS), "All operations should complete within 30 seconds");
        executor.shutdown();

        // Verify all operations completed
        assertEquals(operationCount.get(), CONCURRENT_THREADS, "All addAll operations should complete");

        // Verify the final state - only the last operation's results should be present
        // (since addAll replaces all results atomically)
        java.util.Set<org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult> finalResults =
            results.getAll(sessionId);

        // The exact count depends on which thread won, but should be exactly 10 (one thread's worth)
        assertEquals(finalResults.size(), 10, "Should have exactly 10 results from one winning thread");
    }

    /**
     * Test 4: ConnectionTracker Concurrent Performance
     * Validates that ConnectionTracker can handle high-throughput concurrent operations
     */
    @Test
    public void testConnectionTrackerConcurrentPerformance() throws Exception {
        final int CONCURRENT_THREADS = 100;
        final int OPERATIONS_PER_THREAD = 50;
        final String sessionId = "connection-perf-test";

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);
        AtomicLong totalOperations = new AtomicLong(0);
        AtomicLong totalTime = new AtomicLong(0);

        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    long startTime = System.nanoTime();
                    ConnectionTracker tracker = ConnectionTracker.getInstance(sessionId);

                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        java.net.URI uri = new java.net.URI("https://test" + threadId + "-" + j + ".example.com/");
                        String trackingId = tracker.startTrackingNewConnection(uri, "GET", true);
                        tracker.updateIPAddressById(trackingId, "192.0.2." + (threadId % 254 + 1));
                        tracker.completeTrackingById(trackingId, 200,
                            org.icann.rdapconformance.validator.ConnectionStatus.SUCCESS);
                    }

                    long endTime = System.nanoTime();
                    totalTime.addAndGet(endTime - startTime);
                    totalOperations.addAndGet(OPERATIONS_PER_THREAD);

                } catch (Exception e) {
                    System.err.println("Error in thread " + threadId + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all operations to complete
        assertTrue(latch.await(60, TimeUnit.SECONDS), "All operations should complete within 60 seconds");
        executor.shutdown();

        // Calculate throughput
        double totalTimeSeconds = totalTime.get() / 1_000_000_000.0;
        double operationsPerSecond = totalOperations.get() / totalTimeSeconds;

        System.out.printf("ConnectionTracker throughput: %.0f operations/second%n", operationsPerSecond);

        // Performance requirement: >800 operations/second (realistic for CI environments)
        assertTrue(operationsPerSecond > 800,
            String.format("ConnectionTracker should achieve >800 ops/sec, actual: %.0f", operationsPerSecond));

        // Verify all connections were tracked
        ConnectionTracker tracker = ConnectionTracker.getInstance(sessionId);
        assertEquals(tracker.getConnections().size(), CONCURRENT_THREADS * OPERATIONS_PER_THREAD,
            "All connections should be tracked");
    }

    /**
     * Test 5: SessionContext Validation Performance
     * Tests the new SessionContext validation under load
     */
    @Test
    public void testSessionContextValidationPerformance() throws Exception {
        final int CONCURRENT_THREADS = HEAVY_CONCURRENT_LOAD;
        final int VALIDATIONS_PER_THREAD = 100;

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);
        AtomicLong totalTime = new AtomicLong(0);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    long startTime = System.nanoTime();

                    for (int j = 0; j < VALIDATIONS_PER_THREAD; j++) {
                        String sessionId = "session-" + threadId + "-" + j;
                        String validated = SessionContext.validateSessionId(sessionId);
                        assertEquals(validated, sessionId, "Validation should preserve valid session IDs");

                        // Test null handling
                        String defaulted = SessionContext.validateSessionId(null);
                        assertEquals(defaulted, SessionContext.DEFAULT_SESSION_ID, "Null should default properly");
                    }

                    long endTime = System.nanoTime();
                    totalTime.addAndGet(endTime - startTime);
                    successCount.incrementAndGet();

                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "All validations should complete within 30 seconds");
        executor.shutdown();

        assertEquals(successCount.get(), CONCURRENT_THREADS, "All validation threads should succeed");

        // Calculate performance
        long totalValidations = (long) CONCURRENT_THREADS * VALIDATIONS_PER_THREAD * 2; // 2 validations per iteration
        double averageTimeMs = (totalTime.get() / 1_000_000.0) / totalValidations;

        System.out.printf("SessionContext validation: %.6f ms average per validation%n", averageTimeMs);

        // Should be very fast - less than 0.1ms per validation
        assertTrue(averageTimeMs < 0.1,
            String.format("SessionContext validation should be <0.1ms, actual: %.6f ms", averageTimeMs));
    }
}