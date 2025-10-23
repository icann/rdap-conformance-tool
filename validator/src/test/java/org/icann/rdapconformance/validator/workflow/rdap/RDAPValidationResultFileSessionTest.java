package org.icann.rdapconformance.validator.workflow.rdap;

import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.annotations.AfterMethod;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class to verify session-based isolation in RDAPValidationResultFile.
 * This test ensures that concurrent sessions don't interfere with each other's data.
 */
public class RDAPValidationResultFileSessionTest {

    @Mock
    private RDAPValidatorResults mockResults1;

    @Mock
    private RDAPValidatorResults mockResults2;

    @Mock
    private RDAPValidatorConfiguration mockConfig1;

    @Mock
    private RDAPValidatorConfiguration mockConfig2;

    @Mock
    private ConfigurationFile mockConfigFile1;

    @Mock
    private ConfigurationFile mockConfigFile2;

    @Mock
    private FileSystem mockFileSystem1;

    @Mock
    private FileSystem mockFileSystem2;

    private RDAPValidationResultFile resultFile;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Setup mock configurations
        when(mockConfig1.getUri()).thenReturn(URI.create("https://example1.com"));
        when(mockConfig2.getUri()).thenReturn(URI.create("https://example2.com"));

        when(mockConfigFile1.getDefinitionIdentifier()).thenReturn("test-profile-1");
        when(mockConfigFile2.getDefinitionIdentifier()).thenReturn("test-profile-2");

        // Reset singleton state before each test
        RDAPValidationResultFile.reset();
        resultFile = RDAPValidationResultFile.getInstance();
    }

    @AfterMethod
    public void tearDown() {
        RDAPValidationResultFile.reset();
    }

    @Test
    public void testSessionIsolation() {
        String session1 = "session1";
        String session2 = "session2";

        // Initialize two different sessions
        resultFile.initialize(session1, mockResults1, mockConfig1, mockConfigFile1, mockFileSystem1);
        resultFile.initialize(session2, mockResults2, mockConfig2, mockConfigFile2, mockFileSystem2);

        // Build results for session1 only
        when(mockResults1.getGroupOk()).thenReturn(java.util.Collections.singleton("test-group-ok"));
        when(mockResults1.getGroupErrorWarning()).thenReturn(java.util.Collections.emptySet());
        when(mockConfigFile1.getDefinitionIgnore()).thenReturn(java.util.Collections.emptyList());
        when(mockConfigFile1.getDefinitionNotes()).thenReturn(java.util.Collections.singletonList("Test notes 1"));

        boolean built1 = resultFile.build(session1);

        // Session1 should have a results path, session2 should not
        assertThat(resultFile.getResultsPath(session1)).isNotNull();
        assertThat(resultFile.getResultsPath(session2)).isNull();

        // Clear session1 only
        RDAPValidationResultFile.clearSession(session1);

        // Session1 should now be cleared, session2 should still exist
        assertThat(resultFile.getResultsPath(session1)).isNull();
        // Note: session2 hasn't been built yet, so it won't have a results path anyway
        // But we can verify the session data still exists by trying to build it

        when(mockResults2.getGroupOk()).thenReturn(java.util.Collections.emptySet());
        when(mockResults2.getGroupErrorWarning()).thenReturn(java.util.Collections.singleton("test-error-warning"));
        when(mockConfigFile2.getDefinitionIgnore()).thenReturn(java.util.Collections.emptyList());
        when(mockConfigFile2.getDefinitionNotes()).thenReturn(java.util.Collections.singletonList("Test notes 2"));

        boolean built2 = resultFile.build(session2);
        assertThat(resultFile.getResultsPath(session2)).isNotNull();
    }

    @Test
    public void testConcurrentSessionIsolation() throws InterruptedException {
        final int NUM_THREADS = 5;
        final int ITERATIONS = 10;

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(NUM_THREADS);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadId = i;

            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready

                    for (int iteration = 0; iteration < ITERATIONS; iteration++) {
                        String sessionId = "thread-" + threadId + "-iter-" + iteration;

                        // Create mock objects for this session
                        RDAPValidatorResults results = mock(RDAPValidatorResults.class);
                        RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
                        ConfigurationFile configFile = mock(ConfigurationFile.class);
                        FileSystem fileSystem = mock(FileSystem.class);

                        when(config.getUri()).thenReturn(URI.create("https://example" + threadId + ".com"));
                        when(configFile.getDefinitionIdentifier()).thenReturn("profile-" + threadId);
                        when(results.getGroupOk()).thenReturn(threadId % 2 == 0 ?
                            java.util.Collections.singleton("group-ok-" + threadId) :
                            java.util.Collections.emptySet());
                        when(results.getGroupErrorWarning()).thenReturn(threadId % 2 == 1 ?
                            java.util.Collections.singleton("group-error-" + threadId) :
                            java.util.Collections.emptySet());
                        when(configFile.getDefinitionIgnore()).thenReturn(java.util.Collections.emptyList());
                        when(configFile.getDefinitionNotes()).thenReturn(java.util.Collections.singletonList("Notes " + threadId));

                        // Initialize session
                        resultFile.initialize(sessionId, results, config, configFile, fileSystem);

                        // Build results
                        boolean built = resultFile.build(sessionId);
                        if (built) {
                            successCount.incrementAndGet();

                            // Verify session data
                            String resultPath = resultFile.getResultsPath(sessionId);
                            assertThat(resultPath).as("Results path should not be null for session " + sessionId).isNotNull();
                        } else {
                            errorCount.incrementAndGet();
                        }

                        // Clear session when done
                        RDAPValidationResultFile.clearSession(sessionId);
                    }

                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    completeLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Wait for completion
        assertThat(completeLatch.await(30, TimeUnit.SECONDS)).as("All threads should complete within 30 seconds").isTrue();

        executor.shutdown();

        // Verify results
        int totalOperations = NUM_THREADS * ITERATIONS;
        System.out.println("Concurrent test completed: " + successCount.get() + " successes, " +
                          errorCount.get() + " errors out of " + totalOperations + " total operations");

        // All operations should succeed (no concurrency issues)
        assertThat(errorCount.get()).as("No errors should occur due to concurrency issues").isEqualTo(0);
        assertThat(successCount.get()).as("At least some operations should succeed").isGreaterThan(0);
    }

    @Test
    public void testSessionNotFound() {
        String nonExistentSession = "non-existent";

        // Trying to access non-existent session should return null/empty gracefully
        assertThat(resultFile.getResultsPath(nonExistentSession)).isNull();
        assertThat(resultFile.getAllResults(nonExistentSession)).isEmpty();
        assertThat(resultFile.getErrors(nonExistentSession)).isEmpty();
        assertThat(resultFile.getErrorCount(nonExistentSession)).isEqualTo(0);

        // Operations on non-existent session should not crash
        assertThatCode(() -> {
            resultFile.removeErrors(nonExistentSession);
            resultFile.removeResultGroups(nonExistentSession);
            resultFile.debugPrintResultBreakdown(nonExistentSession);
        }).doesNotThrowAnyException();
    }

    @Test
    public void testSessionCleanup() {
        String sessionId = "cleanup-test";

        // Initialize session
        resultFile.initialize(sessionId, mockResults1, mockConfig1, mockConfigFile1, mockFileSystem1);

        // Verify session exists (by attempting to get results)
        assertThat(resultFile.getAllResults(sessionId)).isNotNull(); // Should return empty list, not null

        // Clear specific session
        RDAPValidationResultFile.clearSession(sessionId);

        // Verify session is cleared
        assertThat(resultFile.getResultsPath(sessionId)).isNull();
        assertThat(resultFile.getAllResults(sessionId)).isEmpty();

        // Initialize another session to verify clearSession doesn't affect other sessions
        String otherSession = "other-session";
        resultFile.initialize(otherSession, mockResults2, mockConfig2, mockConfigFile2, mockFileSystem2);

        // Clear all sessions
        RDAPValidationResultFile.clearAllSessions();

        // Verify both sessions are cleared
        assertThat(resultFile.getResultsPath(sessionId)).isNull();
        assertThat(resultFile.getResultsPath(otherSession)).isNull();
    }

    @Test
    public void testBackwardCompatibility() {
        // Test that deprecated methods still work with "default" session
        resultFile.initialize(mockResults1, mockConfig1, mockConfigFile1, mockFileSystem1);

        // Deprecated methods should work with default session
        assertThat(resultFile.getAllResults()).isNotNull(); // Uses default session
        assertThat(resultFile.getErrorCount()).isEqualTo(0); // Uses default session

        // Should be equivalent to explicit default session calls
        assertThat(resultFile.getAllResults()).isEqualTo(resultFile.getAllResults("default"));
        assertThat(resultFile.getErrorCount()).isEqualTo(resultFile.getErrorCount("default"));
    }
}