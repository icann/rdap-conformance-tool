package org.icann.rdapconformance.tool;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.testng.annotations.Ignore;

/**
 * Comprehensive test that actually performs validation against real RDAP endpoints.
 * This test makes real network calls and shows actual validation results.
 *
 * This test is ignored for CI runs as it requires network connectivity to real RDAP endpoints.
 * To run this test manually, remove the @Ignore annotation or run it directly.
 */
@Ignore("Network-dependent test for manual execution only")
public class EndpointsWebValidationTest {

    // Real RDAP endpoints for testing
    private static final TestEndpoint[] TEST_ENDPOINTS = {
        new TestEndpoint("https://rdap.publicinterestregistry.org/rdap/domain/wikipedia.org", true, false, "PIR Registry"),
        new TestEndpoint("https://rdap.verisign.com/net/v1/domain/google.net", true, false, "Verisign Registry"),
        new TestEndpoint("https://rdap.nic.cz/domain/nic.cz", true, false, "CZ.NIC Registry"),
        new TestEndpoint("https://rdap.cscglobal.com/dbs/rdap-api/v1/domain/VERISIGN.COM", false, true, "CSC Registrar"),
        new TestEndpoint("https://rdap.iana.org/domain/com", true, false, "IANA Registry")
    };

    private static class TestEndpoint {
        final String uri;
        final boolean isRegistry;
        final boolean isRegistrar;
        final String description;

        TestEndpoint(String uri, boolean isRegistry, boolean isRegistrar, String description) {
            this.uri = uri;
            this.isRegistry = isRegistry;
            this.isRegistrar = isRegistrar;
            this.description = description;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== RDAP Validation Test Against Real Endpoints ===");
        System.out.println("This test makes real network calls and performs actual validation.\n");

        int totalTests = 0;
        int successfulValidations = 0;
        int failedValidations = 0;

        for (int i = 0; i < TEST_ENDPOINTS.length; i++) {
            TestEndpoint endpoint = TEST_ENDPOINTS[i];
            totalTests++;

            System.out.println((i + 1) + ". Testing " + endpoint.description);
            System.out.println("   URI: " + endpoint.uri);
            System.out.println("   Registry: " + endpoint.isRegistry + ", Registrar: " + endpoint.isRegistrar);

            try {
                // Create validator with proper configuration
                RdapWebValidator validator = new RdapWebValidator(
                    URI.create(endpoint.uri),
                    endpoint.isRegistry,
                    endpoint.isRegistrar,
                    true // use local datasets
                );

                System.out.println("   Performing validation...");

                // Actually perform the validation (this makes network calls)
                RDAPValidatorResults results = validator.validate();

                // Analyze results
                int errorCount = 0;
                int warningCount = 0;

                System.out.println("   Validation completed!");
                System.out.println("   Total results: " + results.getResultCount());
                System.out.println("   Is valid: " + validator.isValid());

                if (results.getResultCount() > 0) {
                    System.out.println("   Validation findings:");
                    for (RDAPValidationResult result : results.getAll()) {
                        if (result.getCode() < 0) {
                            errorCount++;
                            System.out.println("     ERROR " + result.getCode() + ": " + result.getMessage());
                            if (result.getValue() != null && !result.getValue().isEmpty()) {
                                System.out.println("       Value: " + result.getValue());
                            }
                        } else {
                            warningCount++;
                            System.out.println("     WARNING " + result.getCode() + ": " + result.getMessage());
                        }
                    }
                    System.out.println("   Summary: " + errorCount + " errors, " + warningCount + " warnings");
                } else {
                    System.out.println("   No validation issues found!");
                }

                successfulValidations++;
                System.out.println("   Status: VALIDATION COMPLETED");

            } catch (Exception e) {
                failedValidations++;
                System.out.println("   Status: VALIDATION FAILED");
                System.out.println("   Error: " + e.getMessage());
                if (e.getCause() != null) {
                    System.out.println("   Cause: " + e.getCause().getMessage());
                }
            }

            System.out.println();
        }

        // Final summary
        System.out.println("=== FINAL RESULTS ===");
        System.out.println("Total endpoints tested: " + totalTests);
        System.out.println("Successful validations: " + successfulValidations);
        System.out.println("Failed validations: " + failedValidations);

        if (successfulValidations > 0) {
            System.out.println("\nSUCCESS: Web-safe interface successfully performed real RDAP validations!");
            System.out.println("The interface is working correctly with real RDAP servers.");
        } else {
            System.out.println("\nWARNING: No validations completed successfully.");
            System.out.println("This may be due to network issues or endpoint availability.");
        }

        System.out.println("\nNote: This test makes real network calls to RDAP servers.");
        System.out.println("Results may vary based on network connectivity and server availability.");
    }
}