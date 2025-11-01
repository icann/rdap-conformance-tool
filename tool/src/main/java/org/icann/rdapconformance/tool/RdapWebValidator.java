package org.icann.rdapconformance.tool;

import java.net.URI;
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
public class RdapWebValidator {

    private final QueryContext queryContext;
    private final RDAPValidator rdapValidator;

    /**
     * Creates a new web-safe RDAP validator for the specified URI.
     *
     * @param uri the RDAP URI to validate (e.g., "https://rdap.example.com/domain/test.example")
     * @throws IllegalArgumentException if the URI is invalid
     * @throws RuntimeException if the configuration is invalid
     */
    public RdapWebValidator(String uri) {
        this(validateAndCreateURI(uri), null);
    }

    /**
     * Creates a new web-safe RDAP validator for the specified URI.
     *
     * @param uri the RDAP URI to validate
     * @throws IllegalArgumentException if the URI is invalid
     * @throws RuntimeException if the configuration is invalid
     */
    public RdapWebValidator(URI uri) {
        this(uri, null);
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
        this(uri, new ConfigurableRDAPValidatorConfiguration(uri, isRegistry, !isRegistry));
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
        this(uri, new ConfigurableRDAPValidatorConfiguration(uri, isRegistry, isRegistrar, useLocalDatasets));
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
        // Create default configuration if none provided
        if (config == null) {
            // Create a simple configuration implementation
            config = new SimpleRDAPValidatorConfiguration(uri);
        }

        // Create filesystem for dataset service
        org.icann.rdapconformance.validator.workflow.FileSystem fileSystem =
            new org.icann.rdapconformance.validator.workflow.LocalFileSystem();

        // Create dataset service for ICANN data
        RDAPDatasetServiceImpl datasetService = new RDAPDatasetServiceImpl(fileSystem);

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
    }
}