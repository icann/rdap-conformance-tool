package org.icann.rdapconformance.tool;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetServiceImpl;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQuery;

/**
 * Web-safe RDAP validation interface that avoids global state pollution.
 *
 * <p>This class provides a clean interface for validating RDAP responses in web applications
 * without the global logging and TLS configuration changes that occur in
 * {@link RdapConformanceTool#call()}.</p>
 *
 * <p><strong>Thread Safety:</strong> This class is thread-safe and suitable for use in
 * multi-threaded web applications. Each instance operates independently without shared state.</p>
 *
 * <p><strong>Usage in Web Applications:</strong></p>
 * <pre>{@code
 * // Safe for web controllers:
 * RdapWebValidator validator = new RdapWebValidator("https://rdap.example.com/domain/test.example");
 * RDAPValidatorResults results = validator.validate();
 *
 * // Check for validation errors:
 * boolean isValid = results.getResultCount() == 0;
 * for (RDAPValidationResult error : results.getResults()) {
 *     // Handle each validation finding
 * }
 * }</pre>
 *
 * <p><strong>Important:</strong> Do not use {@link RdapConformanceTool#call()} in web applications
 * as it modifies global JVM state including logging configuration and TLS properties.</p>
 */
public class RdapWebValidator implements AutoCloseable {

    private final QueryContext queryContext;
    private final RDAPValidator rdapValidator;
    private final String customDatasetDirectory;
    private final boolean shouldCleanupDatasets;

    /**
     * Creates a new web-safe RDAP validator for the specified URI.
     *
     * @param uri the RDAP URI to validate (e.g., "https://rdap.example.com/domain/test.example")
     * @throws IllegalArgumentException if the URI is invalid
     * @throws RuntimeException if the configuration is invalid
     */
    public RdapWebValidator(String uri) {
        this(validateAndCreateURI(uri), null, null, false);
    }

    /**
     * Creates a new web-safe RDAP validator for the specified URI.
     *
     * @param uri the RDAP URI to validate
     * @throws IllegalArgumentException if the URI is invalid
     * @throws RuntimeException if the configuration is invalid
     */
    public RdapWebValidator(URI uri) {
        this(uri, null, null, false);
    }

    /**
     * Creates a new web-safe RDAP validator with specific registry/registrar configuration.
     *
     * @param uri the RDAP URI to validate
     * @param isRegistry true if this is a gTLD registry, false for registrar
     * @throws IllegalArgumentException if the URI is invalid
     * @throws RuntimeException if the configuration is invalid
     */
    public RdapWebValidator(URI uri, boolean isRegistry) {
        this(uri, new ConfigurableRDAPValidatorConfiguration(uri, isRegistry, !isRegistry), null, false);
    }

    /**
     * Creates a new web-safe RDAP validator with full configuration control.
     *
     * @param uri the RDAP URI to validate
     * @param isRegistry true if this is a gTLD registry
     * @param isRegistrar true if this is a gTLD registrar
     * @param useLocalDatasets true to use local datasets
     * @throws IllegalArgumentException if the URI is invalid
     * @throws RuntimeException if the configuration is invalid
     */
    public RdapWebValidator(URI uri, boolean isRegistry, boolean isRegistrar, boolean useLocalDatasets) {
        this(uri, new ConfigurableRDAPValidatorConfiguration(uri, isRegistry, isRegistrar, useLocalDatasets), null, false);
    }

    /**
     * Creates a new web-safe RDAP validator with temporary directory support.
     * Uses a unique temporary directory for IANA datasets that will be cleaned up automatically.
     *
     * @param uri the RDAP URI to validate
     * @param isRegistry true if this is a gTLD registry
     * @param isRegistrar true if this is a gTLD registrar
     * @param useTemporaryDirectory true to use a temporary directory for datasets
     * @param cleanupOnClose true to cleanup the temporary directory when close() is called
     * @throws IllegalArgumentException if the URI is invalid
     * @throws RuntimeException if the configuration is invalid or temp directory creation fails
     */
    public RdapWebValidator(URI uri, boolean isRegistry, boolean isRegistrar, boolean useTemporaryDirectory, boolean cleanupOnClose) {
        this(uri, new ConfigurableRDAPValidatorConfiguration(uri, isRegistry, isRegistrar, false),
             useTemporaryDirectory ? createTempDirectory() : null, cleanupOnClose);
    }

    /**
     * Creates a new web-safe RDAP validator with custom dataset directory.
     *
     * @param uri the RDAP URI to validate
     * @param customDatasetDirectory custom directory path for IANA datasets, null for default
     * @param cleanupOnClose true to delete the dataset directory when close() is called
     * @throws IllegalArgumentException if the URI is invalid
     * @throws RuntimeException if the configuration is invalid
     */
    public RdapWebValidator(URI uri, String customDatasetDirectory, boolean cleanupOnClose) {
        this(uri, null, customDatasetDirectory, cleanupOnClose);
    }

    /**
     * Creates a new web-safe RDAP validator with custom configuration.
     *
     * @param uri the RDAP URI to validate
     * @param config custom configuration, or null for default configuration
     * @throws IllegalArgumentException if the URI is invalid
     * @throws RuntimeException if the configuration is invalid
     */
    public RdapWebValidator(URI uri, RDAPValidatorConfiguration config) {
        this(uri, config, null, false);
    }

    /**
     * Creates a new web-safe RDAP validator with custom configuration and dataset directory.
     *
     * @param uri the RDAP URI to validate
     * @param config custom configuration, or null for default configuration
     * @param customDatasetDirectory custom directory for datasets, or null for default
     * @param shouldCleanupDatasets whether to cleanup the dataset directory on close
     * @throws IllegalArgumentException if the URI is invalid
     * @throws RuntimeException if the configuration is invalid
     */
    public RdapWebValidator(URI uri, RDAPValidatorConfiguration config, String customDatasetDirectory, boolean shouldCleanupDatasets) {
        // Store cleanup settings
        this.customDatasetDirectory = customDatasetDirectory;
        this.shouldCleanupDatasets = shouldCleanupDatasets;

        // Create default configuration if none provided
        if (config == null) {
            // Create a simple configuration implementation
            config = new SimpleRDAPValidatorConfiguration(uri);
        }

        // Create filesystem for dataset service
        org.icann.rdapconformance.validator.workflow.FileSystem fileSystem =
            new org.icann.rdapconformance.validator.workflow.LocalFileSystem();

        // Create dataset service for ICANN data with custom directory if provided
        RDAPDatasetServiceImpl datasetService = customDatasetDirectory != null ?
            new RDAPDatasetServiceImpl(fileSystem, customDatasetDirectory) :
            new RDAPDatasetServiceImpl(fileSystem);

        // Download and initialize datasets (required for validation)
        try {
            datasetService.download(config.useLocalDatasets(), null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize RDAP datasets: " + e.getMessage(), e);
        }

        // Create query object for HTTP operations
        RDAPHttpQuery query = new RDAPHttpQuery(config);

        // Create QueryContext - this is the central "world object" for validation
        this.queryContext = QueryContext.create(config, datasetService, query);

        // Create the RDAP validator using our QueryContext
        this.rdapValidator = new RDAPValidator(queryContext);
    }

    /**
     * Performs RDAP validation and returns the results.
     *
     * <p>This method is thread-safe and does not modify any global JVM state.
     * Unlike {@link RdapConformanceTool#call()}, this method:</p>
     * <ul>
     *   <li>Does not modify logging configuration</li>
     *   <li>Does not set TLS system properties</li>
     *   <li>Does not write files to disk</li>
     *   <li>Returns structured results in memory</li>
     * </ul>
     *
     * @return validation results containing any errors or warnings found
     * @throws RuntimeException if validation fails due to configuration or network issues
     */
    public RDAPValidatorResults validate() {
        // Perform validation - this uses the existing validation logic
        // but skips all the global setup from RdapConformanceTool.call()
        rdapValidator.validate();

        // Return the results that were collected during validation
        return queryContext.getResults();
    }

    /**
     * Returns the QueryContext used for validation.
     *
     * <p>This provides access to additional validation metadata including:</p>
     * <ul>
     *   <li>HTTP response data via {@code getQueryContext().getRdapResponseData()}</li>
     *   <li>Query type via {@code getQueryContext().getQueryType()}</li>
     *   <li>Configuration via {@code getQueryContext().getConfig()}</li>
     * </ul>
     *
     * @return the QueryContext containing validation state and results
     */
    public QueryContext getQueryContext() {
        return queryContext;
    }

    /**
     * Returns the URI being validated.
     *
     * @return the RDAP URI
     */
    public URI getUri() {
        return queryContext.getConfig().getUri();
    }

    /**
     * Returns whether the validation was successful (no errors found).
     *
     * @return true if no validation errors were found, false otherwise
     */
    public boolean isValid() {
        return queryContext.getResults().getResultCount() == 0;
    }

    /**
     * Closes the validator and cleans up resources.
     * If this validator was created with a temporary directory, it will be deleted.
     */
    @Override
    public void close() {
        if (shouldCleanupDatasets && customDatasetDirectory != null) {
            try {
                deleteDirectoryRecursively(Path.of(customDatasetDirectory));
            } catch (IOException e) {
                // Log warning but don't throw - cleanup is best-effort
                System.err.println("Warning: Failed to cleanup temporary dataset directory: " + customDatasetDirectory + " - " + e.getMessage());
            }
        }
    }

    /**
     * Creates a unique temporary directory for IANA datasets.
     *
     * @return path to the created temporary directory
     * @throws RuntimeException if temporary directory creation fails
     */
    private static String createTempDirectory() {
        try {
            Path tempDir = Files.createTempDirectory("rdap-validation-");
            return tempDir.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary directory for RDAP datasets", e);
        }
    }

    /**
     * Recursively deletes a directory and all its contents.
     *
     * @param directory the directory to delete
     * @throws IOException if deletion fails
     */
    private static void deleteDirectoryRecursively(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to delete: " + path, e);
                    }
                });
        }
    }

    /**
     * Validates and creates a URI from a string, throwing IllegalArgumentException for invalid URIs.
     *
     * @param uriString the URI string to validate
     * @return a valid URI object
     * @throws IllegalArgumentException if the URI is invalid or has no scheme
     */
    private static URI validateAndCreateURI(String uriString) {
        if (uriString == null || uriString.trim().isEmpty()) {
            throw new IllegalArgumentException("URI cannot be null or empty");
        }

        try {
            URI uri = URI.create(uriString);

            // Validate that the URI has a scheme (protocol)
            if (uri.getScheme() == null || uri.getScheme().trim().isEmpty()) {
                throw new IllegalArgumentException("URI must have a valid scheme (e.g., https://): " + uriString);
            }

            // Validate that it's an HTTP or HTTPS scheme for RDAP
            if (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https")) {
                throw new IllegalArgumentException("URI must use HTTP or HTTPS scheme for RDAP: " + uriString);
            }

            return uri;
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("URI must")) {
                throw e; // Re-throw our validation errors
            }
            throw new IllegalArgumentException("Invalid URI format: " + uriString, e);
        }
    }

    /**
     * Simple implementation of RDAPValidatorConfiguration for web-safe validation.
     * Provides sensible defaults for web application usage.
     */
    private static class SimpleRDAPValidatorConfiguration implements RDAPValidatorConfiguration {
        private URI uri;
        private final int timeout = 30; // 30 second timeout
        private final int maxRedirects = 5;

        public SimpleRDAPValidatorConfiguration(URI uri) {
            this.uri = uri;
        }

        @Override
        public URI getConfigurationFile() { return null; }

        @Override
        public URI getUri() { return uri; }

        @Override
        public void setUri(URI uri) { this.uri = uri; }

        @Override
        public int getTimeout() { return timeout; }

        @Override
        public int getMaxRedirects() { return maxRedirects; }

        @Override
        public boolean useLocalDatasets() { return false; }

        @Override
        public boolean useRdapProfileFeb2019() { return false; }

        @Override
        public boolean useRdapProfileFeb2024() { return true; }

        @Override
        public boolean isGtldRegistrar() { return false; }

        @Override
        public boolean isGtldRegistry() { return false; }

        @Override
        public boolean isThin() { return false; }

        @Override
        public String getResultsFile() { return null; }

        @Override
        public boolean isNoIpv4Queries() { return false; }

        @Override
        public RDAPQueryType getQueryType() { return null; }

        @Override
        public boolean isNoIpv6Queries() { return false; }

        @Override
        public boolean isNetworkEnabled() { return true; }

        @Override
        public boolean isAdditionalConformanceQueries() { return false; }

        @Override
        public void clean() { /* no-op */ }

        @Override
        public String getDatasetDirectory() {
            return null; // Use default dataset directory
        }

        @Override
        public boolean isCleanupDatasetsOnComplete() {
            return false; // Don't cleanup by default
        }
    }

    /**
     * Configurable implementation of RDAPValidatorConfiguration for web-safe validation.
     * Allows setting registry/registrar flags and dataset options.
     */
    private static class ConfigurableRDAPValidatorConfiguration implements RDAPValidatorConfiguration {
        private URI uri;
        private final int timeout = 30; // 30 second timeout
        private final int maxRedirects = 5;
        private final boolean isRegistry;
        private final boolean isRegistrar;
        private final boolean useLocalDatasets;

        public ConfigurableRDAPValidatorConfiguration(URI uri, boolean isRegistry, boolean isRegistrar) {
            this(uri, isRegistry, isRegistrar, true); // Default to using local datasets
        }

        public ConfigurableRDAPValidatorConfiguration(URI uri, boolean isRegistry, boolean isRegistrar, boolean useLocalDatasets) {
            this.uri = uri;
            this.isRegistry = isRegistry;
            this.isRegistrar = isRegistrar;
            this.useLocalDatasets = useLocalDatasets;
        }

        @Override
        public URI getConfigurationFile() { return null; }

        @Override
        public URI getUri() { return uri; }

        @Override
        public void setUri(URI uri) { this.uri = uri; }

        @Override
        public int getTimeout() { return timeout; }

        @Override
        public int getMaxRedirects() { return maxRedirects; }

        @Override
        public boolean useLocalDatasets() { return useLocalDatasets; }

        @Override
        public boolean useRdapProfileFeb2019() { return false; }

        @Override
        public boolean useRdapProfileFeb2024() { return true; }

        @Override
        public boolean isGtldRegistrar() { return isRegistrar; }

        @Override
        public boolean isGtldRegistry() { return isRegistry; }

        @Override
        public boolean isThin() { return false; }

        @Override
        public String getResultsFile() { return null; }

        @Override
        public boolean isNoIpv4Queries() { return false; }

        @Override
        public RDAPQueryType getQueryType() { return null; }

        @Override
        public boolean isNoIpv6Queries() { return false; }

        @Override
        public boolean isNetworkEnabled() { return true; }

        @Override
        public boolean isAdditionalConformanceQueries() { return false; }

        @Override
        public void clean() { /* no-op */ }

        @Override
        public String getDatasetDirectory() {
            return null; // Use default dataset directory
        }

        @Override
        public boolean isCleanupDatasetsOnComplete() {
            return false; // Don't cleanup by default
        }
    }
}