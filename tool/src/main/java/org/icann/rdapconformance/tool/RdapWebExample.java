package org.icann.rdapconformance.tool;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;

/**
 * Example demonstrating safe usage of RDAP validation in web applications.
 *
 * <p>This class shows how to use {@link RdapWebValidator} safely in multi-threaded
 * web environments without the global state pollution that occurs when using
 * {@link RdapConformanceTool#call()}.</p>
 *
 * <p><strong>Key Safety Features:</strong></p>
 * <ul>
 *   <li>No global logging configuration changes</li>
 *   <li>No TLS system property modifications</li>
 *   <li>Thread-safe operation</li>
 *   <li>No file I/O side effects</li>
 *   <li>Structured in-memory results</li>
 * </ul>
 *
 * <p><strong>Logging Configuration:</strong></p>
 * <p>RdapWebValidator uses your application's existing logging configuration.
 * You can control RDAP validation logging in several ways:</p>
 * <ul>
 *   <li><strong>Via configuration file:</strong>
 *   <pre>{@code
 *   <!-- In your web app's logback-spring.xml -->
 *   <logger name="org.icann.rdapconformance" level="ERROR"/>
 *   }</pre></li>
 *   <li><strong>Programmatically:</strong>
 *   <pre>{@code
 *   // If you need to adjust logging levels specifically for RDAP validation:
 *   Logger rdapLogger = (Logger) LoggerFactory.getLogger("org.icann.rdapconformance");
 *   Level originalLevel = rdapLogger.getLevel();
 *   rdapLogger.setLevel(Level.ERROR); // Or whatever level you want
 *
 *   // Perform validation
 *   RdapWebValidator validator = new RdapWebValidator(uri);
 *   RDAPValidatorResults results = validator.validate();
 *
 *   // Restore original level if needed
 *   rdapLogger.setLevel(originalLevel);
 *   }</pre></li>
 * </ul>
 */
public class RdapWebExample {

    /**
     * Basic usage example - suitable for web controllers.
     * Uses default configuration (assumes generic RDAP endpoint, not registry/registrar specific).
     */
    public static ValidationResponse validateDomain(String rdapUri) {
        try {
            // Create validator instance with proper configuration - this is thread-safe
            // Using default configuration with local datasets for better performance
            RdapWebValidator validator = new RdapWebValidator(
                URI.create(rdapUri),
                false, // not a registry
                false, // not a registrar
                true   // use local datasets for better performance and accuracy
            );

            // Perform validation - no global state changes
            RDAPValidatorResults results = validator.validate();

            // Process results
            List<ValidationError> errors = new ArrayList<>();
            List<ValidationError> warnings = new ArrayList<>();

            for (RDAPValidationResult result : results.getAll()) {
                ValidationError error = new ValidationError(
                    result.getCode(),
                    result.getMessage(),
                    result.getValue()
                );

                // Categorize by severity (example logic)
                if (result.getCode() < 0) {
                    errors.add(error);
                } else {
                    warnings.add(error);
                }
            }

            return new ValidationResponse(
                validator.isValid(),
                validator.getUri().toString(),
                errors,
                warnings
            );

        } catch (Exception e) {
            // Handle validation failures gracefully
            return new ValidationResponse(
                false,
                rdapUri,
                List.of(new ValidationError(-999, "Validation failed: " + e.getMessage(), rdapUri)),
                List.of()
            );
        }
    }

    /**
     * Spring Boot Controller Example
     */
    public static class SpringBootExample {

        // @RestController
        // @RequestMapping("/api/rdap")
        public static class RdapController {

            // @PostMapping("/validate")
            public ValidationResponse validateRdapUrl(/* @RequestBody */ ValidationRequest request) {
                return RdapWebExample.validateDomain(request.getRdapUri());
            }

            // @GetMapping("/validate")
            public ValidationResponse validateRdapUrlGet(/* @RequestParam */ String uri) {
                return RdapWebExample.validateDomain(uri);
            }
        }
    }

    /**
     * Jakarta Servlet Example
     */
    public static class ServletExample {

        // @WebServlet("/rdap-validate")
        protected void doPost(/* HttpServletRequest request, HttpServletResponse response */) {
            try {
                // String rdapUri = request.getParameter("uri");
                String rdapUri = "https://rdap.example.com/domain/test.example"; // Example

                ValidationResponse result = RdapWebExample.validateDomain(rdapUri);

                // response.setContentType("application/json");
                // response.getWriter().write(toJson(result));

            } catch (Exception e) {
                // Handle errors appropriately
                // response.setStatus(500);
            }
        }
    }

    /**
     * Concurrent validation example - demonstrates thread safety.
     */
    public static void concurrentValidationExample() {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        List<String> urisToValidate = List.of(
            "https://rdap.arin.net/registry/entity/GOGL",
            "https://rdap.verisign.com/com/v1/domain/google.com",
            "https://rdap.org/domain/example.org"
        );

        // Submit concurrent validation tasks
        List<CompletableFuture<ValidationResponse>> futures = urisToValidate.stream()
            .map(uri -> CompletableFuture.supplyAsync(() -> validateDomain(uri), executor))
            .toList();

        // Wait for all validations to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> {
                System.out.println("All validations completed successfully!");

                // Process results
                futures.forEach(future -> {
                    try {
                        ValidationResponse response = future.get();
                        System.out.println("URI: " + response.getUri() +
                                         " - Valid: " + response.isValid() +
                                         " - Errors: " + response.getErrors().size());
                    } catch (Exception e) {
                        System.err.println("Validation failed: " + e.getMessage());
                    }
                });

                executor.shutdown();
            });
    }

    /**
     * Example demonstrating proper registry/registrar configuration.
     * Use this when validating against specific types of RDAP servers.
     */
    public static ValidationResponse validateWithProperConfiguration(String rdapUri, boolean isRegistry, boolean isRegistrar) {
        try {
            // Create validator with proper registry/registrar flags and local datasets
            // This ensures proper validation according to the RDAP Profile February 2024
            RdapWebValidator validator = new RdapWebValidator(
                URI.create(rdapUri),
                isRegistry,
                isRegistrar,
                true // use local datasets for better performance and accuracy
            );

            // Perform validation with proper configuration
            RDAPValidatorResults results = validator.validate();

            // Process results
            List<ValidationError> errors = new ArrayList<>();
            List<ValidationError> warnings = new ArrayList<>();

            for (RDAPValidationResult result : results.getAll()) {
                ValidationError error = new ValidationError(
                    result.getCode(),
                    result.getMessage(),
                    result.getValue()
                );

                if (result.getCode() < 0) {
                    errors.add(error);
                } else {
                    warnings.add(error);
                }
            }

            return new ValidationResponse(
                validator.isValid(),
                validator.getUri().toString(),
                errors,
                warnings
            );

        } catch (Exception e) {
            return new ValidationResponse(
                false,
                rdapUri,
                List.of(new ValidationError(-999, "Validation failed: " + e.getMessage(), rdapUri)),
                List.of()
            );
        }
    }

    /**
     * Example demonstrating programmatic logging control for RDAP validation.
     */
    public static ValidationResponse validateWithCustomLogging(String rdapUri) {
        // If you need to adjust logging levels specifically for RDAP validation:
        Logger rdapLogger = (Logger) LoggerFactory.getLogger("org.icann.rdapconformance");
        Level originalLevel = rdapLogger.getLevel();

        try {
            // Set desired logging level for validation
            rdapLogger.setLevel(Level.ERROR); // Or whatever level you want

            // Perform validation
            RdapWebValidator validator = new RdapWebValidator(rdapUri);
            RDAPValidatorResults results = validator.validate();

            // Process results
            List<ValidationError> errors = new ArrayList<>();
            List<ValidationError> warnings = new ArrayList<>();

            for (RDAPValidationResult result : results.getAll()) {
                ValidationError error = new ValidationError(
                    result.getCode(),
                    result.getMessage(),
                    result.getValue()
                );

                if (result.getCode() < 0) {
                    errors.add(error);
                } else {
                    warnings.add(error);
                }
            }

            return new ValidationResponse(
                validator.isValid(),
                validator.getUri().toString(),
                errors,
                warnings
            );

        } catch (Exception e) {
            return new ValidationResponse(
                false,
                rdapUri,
                List.of(new ValidationError(-999, "Validation failed: " + e.getMessage(), rdapUri)),
                List.of()
            );
        } finally {
            // Restore original logging level
            if (originalLevel != null) {
                rdapLogger.setLevel(originalLevel);
            }
        }
    }

    /**
     * Performance best practices example.
     */
    public static class PerformanceBestPractices {

        // For high-throughput scenarios, consider connection pooling
        // and caching dataset downloads (the ICANN data)

        /**
         * Example of validating multiple domains efficiently.
         */
        public static List<ValidationResponse> validateMultipleDomains(List<String> uris) {
            return uris.parallelStream()
                .map(RdapWebExample::validateDomain)
                .toList();
        }

        /**
         * Example with custom configuration for performance tuning.
         */
        public static ValidationResponse validateWithCustomConfig(String rdapUri, int timeoutSeconds) {
            try {
                // Create validator with default configuration
                // Note: Custom timeout configuration would require extending SimpleRDAPValidatorConfiguration
                RdapWebValidator validator = new RdapWebValidator(URI.create(rdapUri));
                RDAPValidatorResults results = validator.validate();

                // Process results as before...
                return new ValidationResponse(validator.isValid(), rdapUri, List.of(), List.of());

            } catch (Exception e) {
                return new ValidationResponse(false, rdapUri,
                    List.of(new ValidationError(-999, e.getMessage(), rdapUri)), List.of());
            }
        }
    }

    // Simple data classes for examples

    public static class ValidationRequest {
        private String rdapUri;

        public ValidationRequest() {}
        public ValidationRequest(String rdapUri) { this.rdapUri = rdapUri; }

        public String getRdapUri() { return rdapUri; }
        public void setRdapUri(String rdapUri) { this.rdapUri = rdapUri; }
    }

    public static class ValidationResponse {
        private final boolean valid;
        private final String uri;
        private final List<ValidationError> errors;
        private final List<ValidationError> warnings;

        public ValidationResponse(boolean valid, String uri, List<ValidationError> errors, List<ValidationError> warnings) {
            this.valid = valid;
            this.uri = uri;
            this.errors = errors;
            this.warnings = warnings;
        }

        public boolean isValid() { return valid; }
        public String getUri() { return uri; }
        public List<ValidationError> getErrors() { return errors; }
        public List<ValidationError> getWarnings() { return warnings; }
    }

    public static class ValidationError {
        private final int code;
        private final String message;
        private final String value;

        public ValidationError(int code, String message, String value) {
            this.code = code;
            this.message = message;
            this.value = value;
        }

        public int getCode() { return code; }
        public String getMessage() { return message; }
        public String getValue() { return value; }
    }

    /**
     * Main method for testing the examples.
     */
    public static void main(String[] args) {
        System.out.println("=== RDAP Web Validation Examples ===\n");

        // Basic validation example
        System.out.println("1. Basic Validation:");
        ValidationResponse result = validateDomain("https://rdap.arin.net/registry/entity/GOGL");
        System.out.println("URI: " + result.getUri());
        System.out.println("Valid: " + result.isValid());
        System.out.println("Errors: " + result.getErrors().size());
        System.out.println("Warnings: " + result.getWarnings().size());
        System.out.println();

        // Concurrent validation example
        System.out.println("2. Concurrent Validation:");
        concurrentValidationExample();

        System.out.println("\n=== Examples completed ===");
    }
}