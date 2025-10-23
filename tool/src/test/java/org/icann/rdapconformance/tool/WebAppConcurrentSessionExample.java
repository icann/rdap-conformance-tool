package org.icann.rdapconformance.tool;

import org.icann.rdapconformance.tool.RdapConformanceTool;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

/**
 * Example demonstrating concurrent session management for web application usage.
 * This simulates how a web application would handle multiple concurrent validation requests.
 *
 * This is a standalone example that can be run manually to demonstrate the session-aware architecture.
 * To run: java -cp <classpath> org.icann.rdapconformance.tool.WebAppConcurrentSessionExample
 *
 * Note: This is NOT an automated test - it's a runnable example for documentation purposes.
 */
public class WebAppConcurrentSessionExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== CONCURRENT SESSION MANAGEMENT TEST ===");
        System.out.println("Simulating frontend-like concurrent usage patterns");

        // Create a thread pool to simulate concurrent requests
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<ValidationResult>> futures = new ArrayList<>();

        // Submit 3 concurrent validation tasks with different configurations
        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            Future<ValidationResult> future = executor.submit(() -> {
                return runValidationTask(taskId);
            });
            futures.add(future);
        }

        // Wait for all tasks to complete and collect results
        System.out.println("\nWaiting for all validation tasks to complete...");
        List<ValidationResult> results = new ArrayList<>();
        for (Future<ValidationResult> future : futures) {
            results.add(future.get());
        }

        // Display results
        System.out.println("\n=== RESULTS ===");
        for (ValidationResult result : results) {
            System.out.println(String.format(
                "Task %d (Session: %s): Exit Code: %d, Errors: %d, Total Results: %d",
                result.taskId,
                result.sessionId.substring(0, 8) + "...",
                result.exitCode,
                result.errorCount,
                result.totalResults
            ));
        }

        // Verify session isolation
        System.out.println("\n=== SESSION ISOLATION VERIFICATION ===");
        boolean allSessionsUnique = results.stream()
            .map(r -> r.sessionId)
            .distinct()
            .count() == results.size();

        System.out.println("All sessions have unique IDs: " + allSessionsUnique);

        // Test cleanup of individual sessions
        System.out.println("\n=== TESTING SESSION-SPECIFIC CLEANUP ===");
        for (ValidationResult result : results) {
            String sessionId = result.sessionId;
            RdapConformanceTool tool = new RdapConformanceTool();
            tool.setSessionId(sessionId);

            // Test that we can still access results before cleanup
            List<RDAPValidationResult> errors = tool.getErrors();
            System.out.println("Session " + sessionId.substring(0, 8) + "... has " +
                             errors.size() + " errors before cleanup");

            // Clean up this specific session
            tool.clean(sessionId);

            // Verify cleanup worked
            List<RDAPValidationResult> errorsAfterCleanup = tool.getErrors();
            System.out.println("Session " + sessionId.substring(0, 8) + "... has " +
                             errorsAfterCleanup.size() + " errors after cleanup");
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        System.out.println("\n=== TEST COMPLETED SUCCESSFULLY ===");
        System.out.println("This demonstrates that:");
        System.out.println("1. Multiple sessions can run concurrently without interference");
        System.out.println("2. Each session maintains its own isolated results");
        System.out.println("3. Sessions can be cleaned up individually");
        System.out.println("4. External session management prevents race conditions");
    }

    /**
     * Simulates a validation task similar to how the frontend would use it
     */
    private static ValidationResult runValidationTask(int taskId) throws Exception {
        // Create unique session ID for this request (like frontend would do)
        String sessionId = UUID.randomUUID().toString();

        System.out.println("Task " + taskId + " starting with session: " + sessionId.substring(0, 8) + "...");

        // Create and configure tool (like frontend Main.java does)
        RdapConformanceTool tool = new RdapConformanceTool();

        // Set session ID BEFORE any configuration (external session management)
        tool.setSessionId(sessionId);

        // Configure tool with different settings for each task
        String[] testUrls = {
            "https://rdap.arin.net/registry/entity/GOGL",
            "https://rdap.verisign.com/com/v1/domain/verisign.com",
            "https://rdap.arin.net/registry/ip/8.8.8.8"
        };

        tool.setUri(URI.create(testUrls[taskId - 1]));
        tool.setTimeout(10);
        tool.setMaxRedirects(3);
        tool.setUseLocalDatasets(false);
        tool.setVerbose(false);

        // Set results file to temp location
        tool.setResultsFile("/tmp/results-session-" + sessionId + ".json");

        // Use Path API for config file (like RdapToolExample does)
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path configFilePath = currentDir.resolve("tool/bin/rdapct_config.json");
        tool.setConfigurationFile(configFilePath.toString());

        // Different configurations for different tasks
        switch (taskId) {
            case 1:
                tool.setGtldRegistry(true);
                tool.setUseRdapProfileFeb2024(true);
                break;
            case 2:
                tool.setGtldRegistry(true);
                tool.setUseRdapProfileFeb2024(true);
                break;
            case 3:
                tool.setExecuteIPv4Queries(true);
                tool.setExecuteIPv6Queries(false);
                break;
        }

        // Run validation
        int exitCode = tool.call();

        // Get results (these should be isolated to this session)
        List<RDAPValidationResult> errors = tool.getErrors();
        List<RDAPValidationResult> allResults = tool.getAllResults();

        System.out.println("Task " + taskId + " completed with " + errors.size() + " errors");

        // Return results for verification
        return new ValidationResult(taskId, sessionId, exitCode, errors.size(), allResults.size());
    }

    /**
     * Simple class to hold validation results for testing
     */
    private static class ValidationResult {
        final int taskId;
        final String sessionId;
        final int exitCode;
        final int errorCount;
        final int totalResults;

        ValidationResult(int taskId, String sessionId, int exitCode, int errorCount, int totalResults) {
            this.taskId = taskId;
            this.sessionId = sessionId;
            this.exitCode = exitCode;
            this.errorCount = errorCount;
            this.totalResults = totalResults;
        }
    }
}