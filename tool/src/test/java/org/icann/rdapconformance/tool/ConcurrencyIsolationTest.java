package org.icann.rdapconformance.tool;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Test to prove that we have eliminated singleton/ThreadLocal concurrency pollution.
 *
 * <p><strong>Historical Context:</strong></p>
 * <p>Before, the RDAP validator suffered from severe concurrency issues due to singleton patterns and ThreadLocal usage:</p>
 * <ul>
 *   <li><strong>RDAPHttpQueryTypeProcessor</strong> - Static singleton shared config between threads</li>
 *   <li><strong>RDAPValidatorResultsImpl</strong> - Global singleton results contaminated across threads</li>
 *   <li><strong>NetworkInfo</strong> - Static singleton network settings affected all threads</li>
 *   <li><strong>ThreadLocal bridge</strong> - Memory leaks and improper cleanup</li>
 * </ul>
 *
 * <p><strong>The Problem:</strong></p>
 * <p>In a web application, Thread A validating "domain-A.example" would get results
 * contaminated with Thread B's validation of "domain-B.example". Configuration changes
 * in Thread B would affect Thread A's validation mid-flight.</p>
 *
 * <p><strong>The Fix:</strong></p>
 * <p>Each {@link RdapWebValidator} now creates its own {@link org.icann.rdapconformance.validator.QueryContext}
 * with completely isolated:</p>
 * <ul>
 *   <li>Results containers</li>
 *   <li>Configuration state</li>
 *   <li>Network settings</li>
 *   <li>Processor instances</li>
 * </ul>
 *
 * <p><strong>This Test:</strong></p>
 * <p>This test would have FAILED with the old singleton architecture due to cross-thread
 * contamination, but PASSES with the new QueryContext isolation.</p>
 */
public class ConcurrencyIsolationTest {

    /**
     * Tests that concurrent RDAP validations are completely isolated from each other.
     *
     * <p><strong>Before our fixes:</strong> This test would fail because:</p>
     * <ul>
     *   <li>All threads would share RDAPValidatorResultsImpl.getInstance() results</li>
     *   <li>Thread B's config would overwrite Thread A's config in the singleton</li>
     *   <li>NetworkInfo static settings would bleed between threads</li>
     * </ul>
     *
     * <p><strong>After our fixes:</strong> Each validator gets its own QueryContext with
     * completely isolated state.</p>
     */
    @Test
    public void testConcurrentValidationIsolation() throws InterruptedException {
        final int numThreads = 25;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(numThreads);
        final Map<String, ValidationData> threadResults = new ConcurrentHashMap<>();
        final AtomicReference<Exception> testFailure = new AtomicReference<>();


        //  TODO: add real data from real sites
        // Create threads with different configurations
        for (int i = 0; i < numThreads; i++) {
            final String threadId = "thread-" + i;
            final boolean isRegistry = (i % 2 == 0); // Alternate registry/registrar
            final String domainSuffix = isRegistry ? ".registry" : ".registrar";

            Thread thread = new Thread(() -> {
                try {
                    startLatch.await(); // All threads start simultaneously

                    // Each thread creates validator with different config
                    // OLD CODE: This would have shared singleton instances
                    // NEW CODE: Each gets its own QueryContext with isolated state
                    RdapWebValidator validator = new RdapWebValidator(
                        URI.create("https://rdap.example.com/domain/" + threadId + domainSuffix + ".example"),
                        isRegistry,
                        !isRegistry,
                        true
                    );

                    // Perform validation
                    // OLD CODE: Results would be contaminated across threads
                    // NEW CODE: Each thread gets completely isolated results
                    RDAPValidatorResults results = validator.validate();

                    // Capture thread-specific data for verification
                    ValidationData data = new ValidationData(
                        threadId,
                        validator.getUri().toString(),
                        validator.getQueryContext().getConfig().isGtldRegistry(),
                        validator.getQueryContext().getConfig().isGtldRegistrar(),
                        results.getResultCount(),
                        System.identityHashCode(results), // Verify different result instances
                        System.identityHashCode(validator.getQueryContext()) // Verify different contexts
                    );

                    threadResults.put(threadId, data);

                } catch (Exception e) {
                    testFailure.set(e);
                } finally {
                    doneLatch.countDown();
                }
            });

            thread.setName("ValidationThread-" + threadId);
            thread.start();
        }

        // Start all threads simultaneously to maximize concurrency pressure
        startLatch.countDown();

        // Wait for all threads to complete
        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "Test threads did not complete within timeout");

        // Check for any thread failures
        Exception failure = testFailure.get();
        if (failure != null) {
            throw new AssertionError("Thread failed during validation", failure);
        }

        // Verify complete isolation between threads
        assertEquals(threadResults.size(), numThreads, "Not all threads completed successfully");

        // Verify each thread has unique, isolated state
        for (int i = 0; i < numThreads; i++) {
            String threadId = "thread-" + i;
            ValidationData data = threadResults.get(threadId);
            assertNotNull(data, "Missing results for " + threadId);

            // Verify thread-specific configuration was preserved
            boolean expectedIsRegistry = (i % 2 == 0);
            assertEquals(data.isRegistry, expectedIsRegistry,
                threadId + " registry flag contaminated by other threads");
            assertEquals(data.isRegistrar, !expectedIsRegistry,
                threadId + " registrar flag contaminated by other threads");

            // Verify thread-specific URI was preserved
            String expectedDomainSuffix = expectedIsRegistry ? ".registry" : ".registrar";
            assertTrue(data.uri.contains(threadId + expectedDomainSuffix),
                threadId + " URI contaminated: " + data.uri);
        }

        // Verify all threads got different result instances (not shared singletons)
        // OLD CODE: All threads would have same identityHashCode (singleton)
        // NEW CODE: Each thread has different identityHashCode (isolated instances)
        ValidationData[] dataArray = threadResults.values().toArray(new ValidationData[0]);
        for (int i = 0; i < dataArray.length; i++) {
            for (int j = i + 1; j < dataArray.length; j++) {
                assertNotEquals(dataArray[i].resultsHashCode, dataArray[j].resultsHashCode,
                    "Threads " + dataArray[i].threadId + " and " + dataArray[j].threadId +
                    " share the same results instance (singleton leak!)");
                assertNotEquals(dataArray[i].contextHashCode, dataArray[j].contextHashCode,
                    "Threads " + dataArray[i].threadId + " and " + dataArray[j].threadId +
                    " share the same QueryContext instance (singleton leak!)");
            }
        }

        System.out.println("SUCCESS: All " + numThreads + " concurrent validations were completely isolated");
        System.out.println("No singleton contamination detected - concurrency fix verified!");
    }

    /**
     * Tests that registry vs registrar configuration doesn't bleed between concurrent threads.
     *
     * <p>This specifically tests the RDAPHttpQueryTypeProcessor configuration isolation
     * that was a major source of cross-thread contamination in the old singleton model.</p>
     */
    @Test
    public void testConfigurationIsolation() throws InterruptedException {
        final int numThreads = 10;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(numThreads);
        final Map<String, Boolean> registryResults = new ConcurrentHashMap<>();
        final AtomicReference<Exception> testFailure = new AtomicReference<>();

        for (int i = 0; i < numThreads; i++) {
            final String threadId = "config-thread-" + i;
            final boolean shouldBeRegistry = (i < numThreads / 2); // First half registry, second half registrar

            Thread thread = new Thread(() -> {
                try {
                    startLatch.await();

                    // OLD CODE: Thread B's config would overwrite Thread A's config
                    // NEW CODE: Each validator maintains its own configuration
                    RdapWebValidator validator = new RdapWebValidator(
                        URI.create("https://rdap.example.com/domain/" + threadId + ".example"),
                        shouldBeRegistry,  // Registry flag
                        !shouldBeRegistry, // Registrar flag (opposite)
                        true
                    );

                    // Verify configuration is preserved (not contaminated by other threads)
                    boolean actualIsRegistry = validator.getQueryContext().getConfig().isGtldRegistry();
                    registryResults.put(threadId, actualIsRegistry);

                } catch (Exception e) {
                    testFailure.set(e);
                } finally {
                    doneLatch.countDown();
                }
            });

            thread.start();
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(15, TimeUnit.SECONDS), "Configuration test timed out");

        Exception failure = testFailure.get();
        if (failure != null) {
            throw new AssertionError("Configuration thread failed", failure);
        }

        // Verify each thread maintained its own configuration
        for (int i = 0; i < numThreads; i++) {
            String threadId = "config-thread-" + i;
            boolean expectedIsRegistry = (i < numThreads / 2);
            Boolean actualIsRegistry = registryResults.get(threadId);

            assertNotNull(actualIsRegistry, "Missing configuration result for " + threadId);
            assertEquals(actualIsRegistry.booleanValue(), expectedIsRegistry,
                threadId + " configuration was contaminated by other threads");
        }

        System.out.println("SUCCESS: Configuration isolation verified across " + numThreads + " threads");
    }

    /**
     * Data structure to capture thread-specific validation results for verification.
     */
    private static class ValidationData {
        final String threadId;
        final String uri;
        final boolean isRegistry;
        final boolean isRegistrar;
        final int resultCount;
        final int resultsHashCode;  // To verify different instances
        final int contextHashCode;  // To verify different contexts

        ValidationData(String threadId, String uri, boolean isRegistry, boolean isRegistrar,
                      int resultCount, int resultsHashCode, int contextHashCode) {
            this.threadId = threadId;
            this.uri = uri;
            this.isRegistry = isRegistry;
            this.isRegistrar = isRegistrar;
            this.resultCount = resultCount;
            this.resultsHashCode = resultsHashCode;
            this.contextHashCode = contextHashCode;
        }
    }
}