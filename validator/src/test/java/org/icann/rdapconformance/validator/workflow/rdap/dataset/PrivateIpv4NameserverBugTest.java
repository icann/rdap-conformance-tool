package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest;
import org.icann.rdapconformance.validator.workflow.LocalFileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetServiceImpl;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test to reproduce and verify the fix for the bug where private IPv4 addresses
 * in nameservers generate false positive syntax validation errors.
 *
 * Issue: When a private IPv4 address (10.10.1.16) is used in a nameserver, we get
 * false positive syntax errors: -11404, -10100, -11406.
 * The fix ensures only proper allocation errors are generated: -10101 (not allocated) or -10102 (special address).
 * Cascade errors (-12407, -12208) are expected when IPv4 validation fails.
 *
 * This test uses real datasets to verify the complete validation chain works correctly.
 */
public class PrivateIpv4NameserverBugTest {

    private QueryContext queryContext;
    private RDAPValidatorConfiguration config;
    private RDAPValidatorResults results;
    private RDAPDatasetService datasets;
    private SchemaValidator domainValidator;
    private String testJsonContent;

    @BeforeMethod
    public void setUp() throws IOException {
        // Create mock configuration for testing
        config = mock(RDAPValidatorConfiguration.class);
        when(config.isGtldRegistrar()).thenReturn(true);

        // Create QueryContext for thread-safe operations
        queryContext = QueryContext.forTesting(config);
        results = queryContext.getResults();
        results.clear();

        // Use real datasets for integration testing
        datasets = new RDAPDatasetServiceImpl(new LocalFileSystem());

        // Download real datasets - this gives us actual IPv4 allocation and special address data
        boolean downloadSuccess = datasets.download(true);
        if (!downloadSuccess) {
            throw new RuntimeException("Failed to download real datasets for integration testing");
        }

        // Load the test JSON file that contains private IPv4 addresses in nameservers
        testJsonContent = SchemaValidatorTest.getResource("/validators/domain/private_ipv4_nameserver_bug.json");

        // Create domain validator using QueryContext for thread-safe validation
        domainValidator = new SchemaValidator("rdap_domain.json", results, datasets, queryContext);
    }


    /**
     * Test that validates the fix for the private IPv4 nameserver bug using real datasets.
     *
     * Expected behavior:
     * - -10101 (not allocated) or -10102 (special address) for private IP addresses
     * - -12407, -12208 (cascade errors when IPv4 validation fails)
     * Buggy behavior that should NOT occur: -11404, -10100, -11406 (false positive syntax errors)
     *
     * This integration test uses real IANA datasets to validate that private IPv4 addresses
     * (10.10.1.16, 192.168.1.1) trigger proper allocation errors without syntax false positives.
     */
    @Test
    public void testPrivateIpv4NameserverValidation() {
        // When: Validating a domain with nameservers containing private IPv4 addresses
        boolean isValid = domainValidator.validate(testJsonContent);

        // Then: Get all validation results
        List<RDAPValidationResult> allResults = new ArrayList<>(results.getAll());

        // Extract all error codes that were generated
        List<Integer> errorCodes = allResults.stream()
            .map(RDAPValidationResult::getCode)
            .distinct()
            .sorted()
            .toList();

        // Private IPv4 addresses in nameservers should trigger validation errors
        assertThat(allResults).isNotEmpty();
        assertThat(isValid).isFalse();

        // Assert that we have the expected private IP errors
        boolean hasExpectedErrors = allResults.stream()
            .anyMatch(r -> r.getCode() == -10101 ||
                          r.getCode() == -10102);

        assertThat(hasExpectedErrors)
            .withFailMessage("Expected private IP validation errors (-10101 or -10102) were not found. Found codes: %s", errorCodes)
            .isTrue();

        // Assert that we DO NOT have false positive syntax errors (the actual bug)
        List<Integer> falsePositiveSyntaxErrors = allResults.stream()
            .map(RDAPValidationResult::getCode)
            .filter(code -> code == -10100 ||                           // syntax error (false positive)
                           code == -11404 ||                            // format error (false positive)
                           code == -11406)                              // pattern error (false positive)
            .toList();

        assertThat(falsePositiveSyntaxErrors)
            .withFailMessage("Found false positive syntax errors for valid private IP addresses: %s. All codes: %s",
                           falsePositiveSyntaxErrors, errorCodes)
            .isEmpty();

        // Verify that cascade errors are present (expected behavior)
        boolean hasCascadeErrors = allResults.stream()
            .anyMatch(r -> r.getCode() == -12407 || r.getCode() == -12208);

        assertThat(hasCascadeErrors)
            .withFailMessage("Expected cascade errors (-12407, -12208) when IPv4 validation fails. Found codes: %s", errorCodes)
            .isTrue();

        // Print results for debugging if test fails
        if (!falsePositiveSyntaxErrors.isEmpty()) {
            System.out.println("=== DEBUGGING: Found false positive syntax errors ===");
            for (RDAPValidationResult result : allResults) {
                if (falsePositiveSyntaxErrors.contains(result.getCode())) {
                    System.out.println(String.format("False Positive Code: %d, Value: %s, Message: %s",
                        result.getCode(), result.getValue(), result.getMessage()));
                }
            }
        }
    }

    /**
     * Test using the actual JSON from the bug report to verify the fix.
     * This test uses a fresh results instance to avoid interference from the first test.
     */
    @Test
    public void testWithOriginalBugReportJson() throws IOException {
        // Clear results to start fresh
        results.clear();

        // Load the original JSON from the bug report
        String originalJsonContent = SchemaValidatorTest.getResource("/validators/domain/private_ipv4_nameserver_bug.json");

        // Create a domain validator using the correct schema with QueryContext
        SchemaValidator validator = new SchemaValidator("rdap_domain.json", results, datasets, queryContext);

        // When: Validate the JSON content
        boolean isValid = validator.validate(originalJsonContent);

        // Then: Get all validation results
        List<RDAPValidationResult> allResults = new ArrayList<>(results.getAll());

        // Extract error codes for analysis
        List<Integer> errorCodes = allResults.stream()
            .map(RDAPValidationResult::getCode)
            .distinct()
            .sorted()
            .toList();

        // Private IPv4 addresses should trigger validation errors
        assertThat(allResults).isNotEmpty();
        assertThat(isValid).isFalse();

        // Verify we have at least one proper private IP error
        boolean hasProperPrivateIPError = allResults.stream()
            .anyMatch(r -> r.getCode() == -10101 ||
                          r.getCode() == -10102);

        assertThat(hasProperPrivateIPError)
            .withFailMessage("Expected at least one proper private IP error (-10101 or -10102). Found codes: %s", errorCodes)
            .isTrue();

        // Verify no false positive syntax/format errors for valid private IPs
        List<Integer> falsePositiveErrors = allResults.stream()
            .map(RDAPValidationResult::getCode)
            .filter(code -> code == -10100 ||    // Should not have syntax errors for valid IPs
                           code == -11404)       // Should not have format errors for valid IPs
            .toList();

        assertThat(falsePositiveErrors)
            .withFailMessage("Found false positive syntax/format errors for valid private IP syntax: %s. All codes: %s",
                           falsePositiveErrors, errorCodes)
            .isEmpty();

        // Print summary for debugging
        System.out.println("=== Bug Report Test Summary ===");
        System.out.println("Total errors: " + allResults.size());
        System.out.println("Error codes: " + errorCodes);
        System.out.println("Has proper private IP errors: " + hasProperPrivateIPError);
        System.out.println("False positive count: " + falsePositiveErrors.size());
    }
}