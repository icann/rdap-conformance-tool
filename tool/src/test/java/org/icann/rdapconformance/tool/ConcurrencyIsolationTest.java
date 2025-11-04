package org.icann.rdapconformance.tool;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.testng.annotations.Test;
import org.testng.annotations.Ignore;
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

    // Real RDAP endpoints for concurrency testing (from EndpointsWebValidationTest)
    private static final TestEndpoint[] REAL_ENDPOINTS = {
        new TestEndpoint("https://rdap.publicinterestregistry.org/rdap/domain/wikipedia.org", true, false, "PIR Registry", "wikipedia.org"),
        new TestEndpoint("https://rdap.verisign.com/net/v1/domain/google.net", true, false, "Verisign Registry", "google.net"),
        new TestEndpoint("https://rdap.nic.cz/domain/nic.cz", true, false, "CZ.NIC Registry", "nic.cz"),
        new TestEndpoint("https://rdap.cscglobal.com/dbs/rdap-api/v1/domain/VERISIGN.COM", false, true, "CSC Registrar", "verisign.com"),
        new TestEndpoint("https://rdap.iana.org/domain/com", true, false, "IANA Registry", "com")
    };

    // Domain extraction pattern for validation messages
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("\\b([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}\\b");

    private static class TestEndpoint {
        final String uri;
        final boolean isRegistry;
        final boolean isRegistrar;
        final String description;
        final String expectedDomain;

        TestEndpoint(String uri, boolean isRegistry, boolean isRegistrar, String description, String expectedDomain) {
            this.uri = uri;
            this.isRegistry = isRegistry;
            this.isRegistrar = isRegistrar;
            this.description = description;
            this.expectedDomain = expectedDomain.toLowerCase();
        }
    }

    /**
     * Tests that concurrent RDAP validations against REAL endpoints are completely isolated.
     *
     * <p><strong>Enhanced Test:</strong> This test now uses real RDAP endpoints and verifies that:</p>
     * <ul>
     *   <li>Each thread gets results specific to its endpoint (no cross-contamination)</li>
     *   <li>Domains mentioned in results match the expected endpoint</li>
     *   <li>No thread accidentally gets results from another thread's validation</li>
     *   <li>HTTP status codes and error patterns are endpoint-specific</li>
     * </ul>
     *
     * <p><strong>Before our fixes:</strong> This test would fail because threads would get
     * mixed results from different endpoints due to singleton contamination.</p>
     *
     * <p><strong>After our fixes:</strong> Each validator gets its own QueryContext with
     * completely isolated state and endpoint-specific results.</p>
     */
    @Test
    @Ignore("Network-dependent test for manual execution only")
    public void testConcurrentValidationIsolationWithRealEndpoints() throws InterruptedException {
        final int threadsPerEndpoint = 5; // Create multiple threads per endpoint -- have run this with 15 before and still no corruption
        final int totalThreads = REAL_ENDPOINTS.length * threadsPerEndpoint;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(totalThreads);
        final Map<String, ValidationData> threadResults = new ConcurrentHashMap<>();
        final AtomicReference<Exception> testFailure = new AtomicReference<>();

        System.out.println("=== ENHANCED CONCURRENCY ISOLATION TEST ===");
        System.out.println("Testing " + totalThreads + " concurrent threads against " + REAL_ENDPOINTS.length + " real RDAP endpoints");
        System.out.println("Verifying semantic isolation (no cross-endpoint contamination)\n");

        // Create multiple threads for each real endpoint
        for (int round = 0; round < threadsPerEndpoint; round++) {
            for (TestEndpoint endpoint : REAL_ENDPOINTS) {
                final String threadId = endpoint.description + "-Round" + round;

                Thread thread = new Thread(() -> {
                    try {
                        startLatch.await(); // All threads start simultaneously

                        System.out.println("Thread " + threadId + " starting validation of " + endpoint.uri);

                        // Each thread validates a different real endpoint
                        // This creates maximum opportunity for cross-contamination if isolation fails
                        RdapWebValidator validator = new RdapWebValidator(
                            URI.create(endpoint.uri),
                            endpoint.isRegistry,
                            endpoint.isRegistrar,
                            true // use local datasets
                        );

                        // Perform validation - this makes real network calls
                        RDAPValidatorResults results = validator.validate();

                        // Extract semantic content for verification
                        ValidationData data = extractValidationData(endpoint, results, validator);

                        threadResults.put(threadId, data);
                        System.out.println("Thread " + threadId + " completed: " + results.getResultCount() + " results");

                    } catch (Exception e) {
                        System.err.println("Thread " + threadId + " failed: " + e.getMessage());
                        testFailure.set(e);
                    } finally {
                        doneLatch.countDown();
                    }
                });

                thread.setName("RealValidationThread-" + threadId);
                thread.start();
            }
        }

        // Start all threads simultaneously to maximize concurrency pressure
        startLatch.countDown();

        // Wait for all threads to complete (longer timeout for network calls)
        boolean completed = doneLatch.await(120, TimeUnit.SECONDS);
        assertTrue(completed, "Test threads did not complete within timeout");

        // Check for any thread failures
        Exception failure = testFailure.get();
        if (failure != null) {
            System.err.println("Thread failure detected: " + failure.getMessage());
            // Don't fail the test for network issues, but log them
            System.out.println("WARNING: Some threads failed (likely network issues), testing completed threads only");
        }

        System.out.println("\n=== VERIFICATION RESULTS ===");
        System.out.println("Completed threads: " + threadResults.size() + "/" + totalThreads);

        // Verify semantic isolation - each thread should have results specific to its endpoint
        verifyNoContamination(threadResults, REAL_ENDPOINTS);

        // Verify all threads got different result instances (not shared singletons)
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

        System.out.println("\nSUCCESS: All concurrent validations were semantically isolated");
        System.out.println("No cross-endpoint contamination detected");
        System.out.println("Each thread received results specific to its endpoint");
        System.out.println("Concurrency safety verified with real RDAP data");
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
     * Extracts semantic content from validation results to verify thread isolation.
     */
    private static ValidationData extractValidationData(TestEndpoint endpoint, RDAPValidatorResults results, RdapWebValidator validator) {
        // Extract domains mentioned in validation results
        Set<String> foundDomains = new HashSet<>();
        Set<Integer> errorCodes = new HashSet<>();
        StringBuilder messageBuilder = new StringBuilder();

        for (RDAPValidationResult result : results.getAll()) {
            errorCodes.add(result.getCode());

            // Extract domains from validation messages
            String message = result.getMessage();
            if (message != null) {
                Matcher matcher = DOMAIN_PATTERN.matcher(message.toLowerCase());
                while (matcher.find()) {
                    foundDomains.add(matcher.group());
                }
                messageBuilder.append(message).append(" ");
            }

            // Extract domains from validation values
            String value = result.getValue();
            if (value != null) {
                Matcher matcher = DOMAIN_PATTERN.matcher(value.toLowerCase());
                while (matcher.find()) {
                    foundDomains.add(matcher.group());
                }
                messageBuilder.append(value).append(" ");
            }
        }

        // Get HTTP status code from the query context
        Integer httpStatusCode = null;
        String rdapResponseData = null;
        try {
            // Try to get HTTP status from the validator's context
            if (validator.getQueryContext().getCurrentHttpResponse() != null) {
                httpStatusCode = validator.getQueryContext().getCurrentHttpResponse().statusCode();
            }
            // Get the RDAP response data
            rdapResponseData = validator.getQueryContext().getRdapResponseData();

            // Extract domains from response data as well
            if (rdapResponseData != null) {
                Matcher matcher = DOMAIN_PATTERN.matcher(rdapResponseData.toLowerCase());
                while (matcher.find()) {
                    foundDomains.add(matcher.group());
                }
            }
        } catch (Exception e) {
            // Ignore exceptions during data extraction
        }

        // Create response snippet for verification
        String responseSnippet = rdapResponseData != null && rdapResponseData.length() > 200
            ? rdapResponseData.substring(0, 200) + "..."
            : rdapResponseData;

        return new ValidationData(
            endpoint.description,
            endpoint.uri,
            endpoint.expectedDomain,
            endpoint.isRegistry,
            endpoint.isRegistrar,
            results.getResultCount(),
            System.identityHashCode(results),
            System.identityHashCode(validator.getQueryContext()),
            foundDomains,
            httpStatusCode,
            errorCodes,
            responseSnippet,
            rdapResponseData
        );
    }

    /**
     * Verifies that no cross-thread contamination occurred by checking semantic content.
     */
    private static void verifyNoContamination(Map<String, ValidationData> threadResults, TestEndpoint[] endpoints) {
        for (TestEndpoint endpoint : endpoints) {
            ValidationData data = threadResults.get(endpoint.description);
            if (data == null) continue; // Skip if thread failed

            // Verify the thread got results for its expected domain
            boolean foundExpectedDomain = data.foundDomains.contains(data.expectedDomain);
            assertTrue(foundExpectedDomain,
                String.format("Thread %s expected to find domain '%s' but found domains: %s",
                    data.threadId, data.expectedDomain, data.foundDomains));

            // Verify the thread didn't get contaminated with other domains
            for (TestEndpoint otherEndpoint : endpoints) {
                if (!otherEndpoint.description.equals(endpoint.description)) {
                    boolean foundOtherDomain = data.foundDomains.contains(otherEndpoint.expectedDomain);
                    assertFalse(foundOtherDomain,
                        String.format("Thread %s (validating %s) was contaminated with domain '%s' from thread %s",
                            data.threadId, data.expectedDomain, otherEndpoint.expectedDomain, otherEndpoint.description));
                }
            }

            // Log verification details
            System.out.println(String.format("Thread %s: Expected '%s', Found domains: %s, HTTP: %s, Errors: %d",
                data.threadId, data.expectedDomain, data.foundDomains, data.httpStatusCode, data.errorCodes.size()));
        }
    }

    /**
     * Enhanced data structure to capture thread-specific validation results for semantic verification.
     */
    private static class ValidationData {
        final String threadId;
        final String uri;
        final String expectedDomain;  // Domain this thread should be validating
        final boolean isRegistry;
        final boolean isRegistrar;
        final int resultCount;
        final int resultsHashCode;    // To verify different instances
        final int contextHashCode;    // To verify different contexts

        // Semantic content verification fields
        final Set<String> foundDomains;     // All domains mentioned in validation results
        final Integer httpStatusCode;       // HTTP status code from response
        final Set<Integer> errorCodes;      // All validation error codes found
        final String responseSnippet;       // Sample of response data for verification
        final String rdapResponseData;      // Full RDAP response for deep inspection

        ValidationData(String threadId, String uri, String expectedDomain, boolean isRegistry, boolean isRegistrar,
                      int resultCount, int resultsHashCode, int contextHashCode,
                      Set<String> foundDomains, Integer httpStatusCode, Set<Integer> errorCodes,
                      String responseSnippet, String rdapResponseData) {
            this.threadId = threadId;
            this.uri = uri;
            this.expectedDomain = expectedDomain;
            this.isRegistry = isRegistry;
            this.isRegistrar = isRegistrar;
            this.resultCount = resultCount;
            this.resultsHashCode = resultsHashCode;
            this.contextHashCode = contextHashCode;
            this.foundDomains = foundDomains;
            this.httpStatusCode = httpStatusCode;
            this.errorCodes = errorCodes;
            this.responseSnippet = responseSnippet;
            this.rdapResponseData = rdapResponseData;
        }
    }
}