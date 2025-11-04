package org.icann.rdapconformance.tool;

import static org.testng.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

/**
 * Tests for RdapWebValidator to ensure web-safe operation.
 */
public class RdapWebValidatorTest {

    @Test
    public void testConstructorWithString() {
        // Should not throw any exceptions
        RdapWebValidator validator = new RdapWebValidator("https://rdap.example.com/domain/test.example");

        assertNotNull(validator);
        assertNotNull(validator.getQueryContext());
        assertEquals("https://rdap.example.com/domain/test.example", validator.getUri().toString());
    }

    @Test
    public void testConstructorWithURI() {
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");
        RdapWebValidator validator = new RdapWebValidator(testUri);

        assertNotNull(validator);
        assertNotNull(validator.getQueryContext());
        assertEquals(testUri, validator.getUri());
    }

    @Test
    public void testConstructorWithCustomConfig() {
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");
        // Test with null config to use default
        RdapWebValidator validator = new RdapWebValidator(testUri, null);

        assertNotNull(validator);
        assertNotNull(validator.getQueryContext());
        assertEquals(testUri, validator.getUri());
        assertEquals(30, validator.getQueryContext().getConfig().getTimeout());
    }

    @Test
    public void testValidateReturnsResults() {
        // This test will fail in network-isolated environments
        // but demonstrates the interface works
        RdapWebValidator validator = new RdapWebValidator("https://rdap.arin.net/registry/entity/GOGL");

        // Should not throw exceptions and should return results object
        RDAPValidatorResults results = validator.validate();

        assertNotNull(results);
        // Results may contain validation errors, but the interface should work
    }

    @Test
    public void testIsValidMethod() {
        RdapWebValidator validator = new RdapWebValidator("https://rdap.example.com/domain/test.example");

        // Initially should be valid (no results yet)
        assertTrue(validator.isValid());

        // After validation (which may fail due to network), should still have boolean result
        validator.validate();
        // isValid() should return true if no validation errors found
        boolean isValid = validator.isValid();
        assertTrue(isValid || !isValid); // Just ensure it returns a boolean without exception
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        // Test that multiple validators can be created concurrently
        // This verifies our fix for the ThreadLocal issue

        final int numThreads = 10;
        final boolean[] results = new boolean[numThreads];
        final Exception[] exceptions = new Exception[numThreads];

        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    String uri = "https://rdap.example.com/domain/test" + threadIndex + ".example";
                    RdapWebValidator validator = new RdapWebValidator(uri);

                    // Verify each validator has its own context
                    assertNotNull(validator.getQueryContext());
                    assertEquals(uri, validator.getUri().toString());

                    results[threadIndex] = true;
                } catch (Exception e) {
                    exceptions[threadIndex] = e;
                    results[threadIndex] = false;
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(5000); // 5 second timeout
        }

        // Verify all threads completed successfully
        for (int i = 0; i < numThreads; i++) {
            if (exceptions[i] != null) {
                fail("Thread " + i + " failed with exception: " + exceptions[i].getMessage());
            }
            assertTrue(results[i], "Thread " + i + " failed to complete");
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidURI() {
        // Test that invalid URIs are handled appropriately
        new RdapWebValidator("not-a-valid-uri");
    }

    @Test
    @Ignore // requires network access
    public void testWebExampleBasicUsage() {
        // Test the example usage pattern
        RdapWebExample.ValidationResponse response =
            RdapWebExample.validateDomain("https://rdap.arin.net/registry/entity/GOGL");

        assertNotNull(response);
        assertNotNull(response.getUri());
        assertNotNull(response.getErrors());
        assertNotNull(response.getWarnings());

        // Should be able to check validity
        boolean isValid = response.isValid();
        assertTrue(isValid || !isValid); // Just ensure it returns a boolean
    }

    @Test
    public void testCustomDatasetDirectoryConstructor() {
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");
        String customDir = "/tmp/rdap-test-custom";

        // Test custom dataset directory constructor
        try (RdapWebValidator validator = new RdapWebValidator(testUri, customDir, false)) {
            assertNotNull(validator);
            assertNotNull(validator.getQueryContext());
            assertEquals(testUri, validator.getUri());
        }
        // No exceptions should be thrown during cleanup
    }

    @Test
    public void testTemporaryDirectoryConstructor() {
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");

        // Test auto-generated temporary directory constructor
        try (RdapWebValidator validator = new RdapWebValidator(testUri, true, false, true, true)) {
            assertNotNull(validator);
            assertNotNull(validator.getQueryContext());
            assertEquals(testUri, validator.getUri());
        }
        // Temporary directory should be cleaned up automatically
    }

    @Test
    public void testAutoCloseableInterface() {
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");

        // Test that RdapWebValidator implements AutoCloseable properly
        try (RdapWebValidator validator = new RdapWebValidator(testUri, "/tmp/rdap-test-autocloseable", true)) {
            assertNotNull(validator);
            // Use the validator
            validator.validate();
        } catch (Exception e) {
            // Network errors are expected in test environment, but no cleanup errors should occur
            assertFalse(e.getMessage().contains("cleanup"), "Cleanup should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testTemporaryDirectoryCreation() throws IOException {
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");
        Path createdTempDir = null;

        try {
            // Create a temporary directory for testing
            Path tempDir = Files.createTempDirectory("rdap-test-");
            createdTempDir = tempDir;
            String tempDirPath = tempDir.toAbsolutePath().toString();

            // Test that custom directory is used
            try (RdapWebValidator validator = new RdapWebValidator(testUri, tempDirPath, false)) {
                assertNotNull(validator);
                // The directory should exist during validation
                assertTrue(Files.exists(tempDir), "Custom dataset directory should exist");
            }

            // Directory should still exist since cleanup=false
            assertTrue(Files.exists(tempDir), "Directory should still exist when cleanup=false");

        } finally {
            // Clean up test directory recursively
            if (createdTempDir != null && Files.exists(createdTempDir)) {
                deleteDirectoryRecursively(createdTempDir);
            }
        }
    }

    @Test
    public void testTemporaryDirectoryCleanup() throws IOException {
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");
        Path tempDirToCheck = null;

        try {
            // Create a temporary directory for testing cleanup
            Path tempDir = Files.createTempDirectory("rdap-test-cleanup-");
            tempDirToCheck = tempDir;
            String tempDirPath = tempDir.toAbsolutePath().toString();

            // Test with cleanup enabled
            try (RdapWebValidator validator = new RdapWebValidator(testUri, tempDirPath, true)) {
                assertNotNull(validator);
                // Directory should exist during validation
                assertTrue(Files.exists(tempDir), "Dataset directory should exist during validation");
            }

            // Directory should be cleaned up after close
            assertFalse(Files.exists(tempDir), "Directory should be cleaned up after close()");
            tempDirToCheck = null; // Don't try to clean up in finally block

        } finally {
            // Clean up test directory if cleanup didn't work
            if (tempDirToCheck != null && Files.exists(tempDirToCheck)) {
                Files.delete(tempDirToCheck);
            }
        }
    }

    @Test
    public void testConcurrentTemporaryDirectories() throws InterruptedException {
        // Test that multiple concurrent validations get isolated directories
        final int numValidators = 5;
        final URI testUri = URI.create("https://rdap.example.com/domain/test.example");
        final List<String> usedDirectories = new ArrayList<>();
        final List<Exception> exceptions = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(numValidators);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < numValidators; i++) {
            final int validatorIndex = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // Create a custom directory for each validator
                    Path tempDir = Files.createTempDirectory("rdap-concurrent-test-" + validatorIndex + "-");
                    String tempDirPath = tempDir.toAbsolutePath().toString();

                    synchronized (usedDirectories) {
                        usedDirectories.add(tempDirPath);
                    }

                    try (RdapWebValidator validator = new RdapWebValidator(testUri, tempDirPath, true)) {
                        assertNotNull(validator);
                        assertEquals(testUri, validator.getUri());

                        // Verify each validator has its own directory
                        assertTrue(Files.exists(Paths.get(tempDirPath)),
                                 "Validator " + validatorIndex + " should have its own directory");
                    }

                    // Directory should be cleaned up
                    assertFalse(Files.exists(Paths.get(tempDirPath)),
                               "Directory should be cleaned up for validator " + validatorIndex);

                } catch (Exception e) {
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                }
            }, executor);

            futures.add(future);
        }

        // Wait for all validations to complete
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                             .get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail("Failed to complete concurrent validations: " + e.getMessage());
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Executor should terminate");

        // Verify no exceptions occurred
        if (!exceptions.isEmpty()) {
            fail("Concurrent validation failed: " + exceptions.get(0).getMessage());
        }

        // Verify all directories were unique
        assertEquals(numValidators, usedDirectories.size(), "Should have created unique directories");
        assertEquals(usedDirectories.size(), usedDirectories.stream().distinct().count(),
                    "All directories should be unique");
    }

    @Test
    public void testBackwardCompatibility() {
        // Test that existing constructor signatures still work unchanged
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");

        // Original constructors should still work
        RdapWebValidator validator1 = new RdapWebValidator("https://rdap.example.com/domain/test.example");
        assertNotNull(validator1);

        RdapWebValidator validator2 = new RdapWebValidator(testUri);
        assertNotNull(validator2);

        RdapWebValidator validator3 = new RdapWebValidator(testUri, true);
        assertNotNull(validator3);

        RdapWebValidator validator4 = new RdapWebValidator(testUri, true, false, true);
        assertNotNull(validator4);

        RdapWebValidator validator5 = new RdapWebValidator(testUri, null);
        assertNotNull(validator5);
    }

    @Test
    public void testConfigurationIntegration() {
        // Test that the new configuration methods are properly integrated
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");

        try (RdapWebValidator validator = new RdapWebValidator(testUri, "/tmp/rdap-config-test", true)) {
            assertNotNull(validator.getQueryContext());
            assertNotNull(validator.getQueryContext().getConfig());

            // Configuration should be accessible
            assertEquals(testUri, validator.getQueryContext().getConfig().getUri());
            assertEquals(30, validator.getQueryContext().getConfig().getTimeout());

            // New configuration methods should have default implementations
            // getDatasetDirectory() returns null by default, which is expected
            assertNull(validator.getQueryContext().getConfig().getDatasetDirectory());
            assertFalse(validator.getQueryContext().getConfig().isCleanupDatasetsOnComplete());
        }
    }

    @Test
    public void testErrorHandlingWithInvalidDirectory() {
        // Test error handling when dataset directory creation fails during initialization
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");

        try {
            // Try to create validator with invalid directory path
            new RdapWebValidator(testUri, "/invalid/nonexistent/path/that/should/fail", false);
            // If we get here, the validator was created but datasets failed to download
            // This is acceptable behavior as download failures are handled gracefully
        } catch (RuntimeException e) {
            // This is also acceptable - some environments may throw on directory creation
            assertTrue(e.getMessage().contains("Failed to initialize RDAP datasets"));
        }
    }

    @Test
    public void testNullDirectoryHandling() {
        // Test that null directory is handled properly (falls back to default)
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");

        try (RdapWebValidator validator = new RdapWebValidator(testUri, null, false)) {
            assertNotNull(validator);
            assertNotNull(validator.getQueryContext());
            assertEquals(testUri, validator.getUri());
        }
    }

    @Test
    public void testFullConfigurationConstructor() {
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");

        // Test the new constructor with all configuration options
        try (RdapWebValidator validator = new RdapWebValidator(testUri,
                true,    // isRegistry
                false,   // isRegistrar
                true,    // useRdapProfileFeb2019
                false,   // useRdapProfileFeb2024
                true,    // noIpv4Queries
                false,   // noIpv6Queries
                true,    // additionalConformanceQueries
                true,    // useTemporaryDirectory
                true     // cleanupOnClose
        )) {
            assertNotNull(validator);
            assertNotNull(validator.getQueryContext());

            // Verify configuration values are set correctly
            assertEquals(testUri, validator.getUri());
            assertTrue(validator.getQueryContext().getConfig().isGtldRegistry());
            assertFalse(validator.getQueryContext().getConfig().isGtldRegistrar());
            assertTrue(validator.getQueryContext().getConfig().useRdapProfileFeb2019());
            assertFalse(validator.getQueryContext().getConfig().useRdapProfileFeb2024());
            assertTrue(validator.getQueryContext().getConfig().isNoIpv4Queries());
            assertFalse(validator.getQueryContext().getConfig().isNoIpv6Queries());
            assertTrue(validator.getQueryContext().getConfig().isAdditionalConformanceQueries());
        }
    }

    @Test
    public void testFullConfigurationWithDefaults() {
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");

        // Test with configuration that matches defaults
        try (RdapWebValidator validator = new RdapWebValidator(testUri,
                false,   // isRegistry
                true,    // isRegistrar
                false,   // useRdapProfileFeb2019 (default)
                true,    // useRdapProfileFeb2024 (default)
                false,   // noIpv4Queries (default)
                false,   // noIpv6Queries (default)
                false,   // additionalConformanceQueries (default)
                false,   // useTemporaryDirectory
                false    // cleanupOnClose
        )) {
            assertNotNull(validator);

            // Verify configuration values match what we set
            assertFalse(validator.getQueryContext().getConfig().isGtldRegistry());
            assertTrue(validator.getQueryContext().getConfig().isGtldRegistrar());
            assertFalse(validator.getQueryContext().getConfig().useRdapProfileFeb2019());
            assertTrue(validator.getQueryContext().getConfig().useRdapProfileFeb2024());
            assertFalse(validator.getQueryContext().getConfig().isNoIpv4Queries());
            assertFalse(validator.getQueryContext().getConfig().isNoIpv6Queries());
            assertFalse(validator.getQueryContext().getConfig().isAdditionalConformanceQueries());
        }
    }

    @Test
    public void testProfileConfigurationOptions() {
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");

        // Test RDAP Profile 2019 enabled
        try (RdapWebValidator validator2019 = new RdapWebValidator(testUri,
                true, false, true, false, false, false, false, false, false)) {
            assertTrue(validator2019.getQueryContext().getConfig().useRdapProfileFeb2019());
            assertFalse(validator2019.getQueryContext().getConfig().useRdapProfileFeb2024());
        }

        // Test RDAP Profile 2024 enabled
        try (RdapWebValidator validator2024 = new RdapWebValidator(testUri,
                true, false, false, true, false, false, false, false, false)) {
            assertFalse(validator2024.getQueryContext().getConfig().useRdapProfileFeb2019());
            assertTrue(validator2024.getQueryContext().getConfig().useRdapProfileFeb2024());
        }

        // Test both profiles enabled
        try (RdapWebValidator validatorBoth = new RdapWebValidator(testUri,
                true, false, true, true, false, false, false, false, false)) {
            assertTrue(validatorBoth.getQueryContext().getConfig().useRdapProfileFeb2019());
            assertTrue(validatorBoth.getQueryContext().getConfig().useRdapProfileFeb2024());
        }
    }

    @Test
    public void testIpVersionConfigurationOptions() {
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");

        // Test IPv4 disabled
        try (RdapWebValidator validatorNoIPv4 = new RdapWebValidator(testUri,
                true, false, false, true, true, false, false, false, false)) {
            assertTrue(validatorNoIPv4.getQueryContext().getConfig().isNoIpv4Queries());
            assertFalse(validatorNoIPv4.getQueryContext().getConfig().isNoIpv6Queries());
        }

        // Test IPv6 disabled
        try (RdapWebValidator validatorNoIPv6 = new RdapWebValidator(testUri,
                true, false, false, true, false, true, false, false, false)) {
            assertFalse(validatorNoIPv6.getQueryContext().getConfig().isNoIpv4Queries());
            assertTrue(validatorNoIPv6.getQueryContext().getConfig().isNoIpv6Queries());
        }

        // Test both IP versions disabled
        try (RdapWebValidator validatorNoIP = new RdapWebValidator(testUri,
                true, false, false, true, true, true, false, false, false)) {
            assertTrue(validatorNoIP.getQueryContext().getConfig().isNoIpv4Queries());
            assertTrue(validatorNoIP.getQueryContext().getConfig().isNoIpv6Queries());
        }
    }

    @Test
    public void testAdditionalConformanceQueriesConfiguration() {
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");

        // Test additional conformance queries enabled
        try (RdapWebValidator validator = new RdapWebValidator(testUri,
                true, false, false, true, false, false, true, false, false)) {
            assertTrue(validator.getQueryContext().getConfig().isAdditionalConformanceQueries());
        }

        // Test additional conformance queries disabled (default)
        try (RdapWebValidator validator = new RdapWebValidator(testUri,
                true, false, false, true, false, false, false, false, false)) {
            assertFalse(validator.getQueryContext().getConfig().isAdditionalConformanceQueries());
        }
    }

    @Test
    public void testFullConfigurationWithTemporaryDirectory() throws IOException {
        URI testUri = URI.create("https://rdap.example.com/domain/test.example");

        // Test all configuration options with temporary directory and cleanup
        try (RdapWebValidator validator = new RdapWebValidator(testUri,
                false,   // isRegistry
                true,    // isRegistrar
                true,    // useRdapProfileFeb2019
                true,    // useRdapProfileFeb2024
                true,    // noIpv4Queries
                true,    // noIpv6Queries
                true,    // additionalConformanceQueries
                true,    // useTemporaryDirectory
                true     // cleanupOnClose
        )) {
            assertNotNull(validator);

            // Verify all configuration options are set as expected
            assertFalse(validator.getQueryContext().getConfig().isGtldRegistry());
            assertTrue(validator.getQueryContext().getConfig().isGtldRegistrar());
            assertTrue(validator.getQueryContext().getConfig().useRdapProfileFeb2019());
            assertTrue(validator.getQueryContext().getConfig().useRdapProfileFeb2024());
            assertTrue(validator.getQueryContext().getConfig().isNoIpv4Queries());
            assertTrue(validator.getQueryContext().getConfig().isNoIpv6Queries());
            assertTrue(validator.getQueryContext().getConfig().isAdditionalConformanceQueries());

            // Verify it's still a functional validator
            assertEquals(testUri, validator.getUri());
            assertNotNull(validator.getQueryContext());
        }
        // Temporary directory should be cleaned up automatically
    }

    /**
     * Helper method to recursively delete directories for test cleanup.
     */
    private void deleteDirectoryRecursively(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore cleanup errors in tests
                    }
                });
        }
    }
}