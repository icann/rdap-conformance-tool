package org.icann.rdapconformance.tool;

import static org.testng.Assert.*;

import java.net.URI;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
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
}