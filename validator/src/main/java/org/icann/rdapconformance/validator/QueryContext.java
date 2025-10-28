package org.icann.rdapconformance.validator;

import java.net.http.HttpResponse;
import java.util.UUID;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQuery;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResultFile;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQueryTypeProcessor;
import org.icann.rdapconformance.validator.workflow.rdap.http.HttpClientManager;
import org.icann.rdapconformance.validator.workflow.rdap.file.RDAPFileQueryTypeProcessor;
import org.json.JSONObject;

/**
 * QueryContext serves as the complete "world object" for RDAP validation operations.
 *
 * <p>This class encapsulates ALL stateful components and configuration needed for a
 * validation run, ensuring complete isolation between concurrent validations and
 * eliminating the need for singleton patterns that cause concurrency issues.</p>
 *
 * <p>The QueryContext follows a hybrid approach:</p>
 * <ul>
 *   <li><strong>Immutable:</strong> Configuration and services that don't change during validation</li>
 *   <li><strong>Mutable:</strong> Runtime state holders for results, connections, and current response data</li>
 * </ul>
 *
 * <p>Usage examples:</p>
 * <pre>
 * // Production usage
 * QueryContext qctx = QueryContext.create(config, datasetService, query);
 * RDAPValidator validator = new RDAPValidator(qctx);
 *
 * // Testing usage
 * QueryContext qctx = QueryContext.forTesting();
 * ResponseValidation validation = new ResponseValidation2Dot10(qctx);
 * </pre>
 */
public class QueryContext {

    // IMMUTABLE: Configuration & Services (set once during construction)
    private final String queryId;
    private final RDAPValidatorConfiguration config;
    private final RDAPDatasetService datasetService;
    private final RDAPQuery query;
    private RDAPQueryType queryType; // Made mutable for testing purposes

    // MUTABLE: Runtime State Holders (objects that accumulate state during validation)
    private final RDAPValidatorResults results;
    private final ConnectionTracker connectionTracker;
    private final RDAPValidationResultFile resultFile;
    private final DNSCacheResolver dnsResolver;
    private final HttpClientManager httpClientManager;
    private final RDAPHttpQueryTypeProcessor httpQueryTypeProcessor;
    private final RDAPFileQueryTypeProcessor fileQueryTypeProcessor;
    private final NetworkInfo networkInfo;

    // MUTABLE: Current Response Data (changes as validation progresses)
    private String rdapResponseData;
    private HttpResponse<String> currentHttpResponse;
    private JSONObject jsonResponseData;

    /**
     * Constructs a new QueryContext with the specified configuration and services.
     *
     * @param queryId unique identifier for this validation query
     * @param config the RDAP validator configuration
     * @param datasetService service for accessing RDAP datasets
     * @param query the RDAP query to be validated
     */
    public QueryContext(String queryId,
                       RDAPValidatorConfiguration config,
                       RDAPDatasetService datasetService,
                       RDAPQuery query) {
        this(queryId, config, datasetService, query, (String) null);
    }

    /**
     * Constructs a new QueryContext with the specified configuration, services, and custom DNS server.
     *
     * @param queryId unique identifier for this validation query
     * @param config the RDAP validator configuration
     * @param datasetService service for accessing RDAP datasets
     * @param query the RDAP query to be validated
     * @param customDnsServer custom DNS server IP address, or null to use system default
     */
    public QueryContext(String queryId,
                       RDAPValidatorConfiguration config,
                       RDAPDatasetService datasetService,
                       RDAPQuery query,
                       String customDnsServer) {
        // Initialize immutable configuration
        this.queryId = queryId;
        this.config = config;
        this.datasetService = datasetService;
        this.query = query;

        // Initialize mutable state holders (fresh instances for this validation)
        this.results = new RDAPValidatorResultsImpl();
        this.connectionTracker = new ConnectionTracker();
        this.resultFile = new RDAPValidationResultFile();
        this.dnsResolver = new DNSCacheResolver(customDnsServer);
        // Initialize DNS cache once at startup with the target URL (skip for test URIs to improve performance)
        if (config.getUri() != null) {
            String uriString = config.getUri().toString();
            if (!uriString.contains("example.com")) {
                this.dnsResolver.initFromUrl(uriString);
            }
        }
        this.httpClientManager = new HttpClientManager();
        this.httpQueryTypeProcessor = new RDAPHttpQueryTypeProcessor();
        this.fileQueryTypeProcessor = new RDAPFileQueryTypeProcessor();
        this.networkInfo = new NetworkInfo();

        // Set QueryContext in ThreadLocal for processors to access BEFORE determining query type
        // This ensures that domain validation in check() method has access to QueryContext
        org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQueryTypeProcessor.setCurrentQueryContext(this);

        // Determine query type after initializing processors and setting ThreadLocal
        this.queryType = determineQueryType();

        // Set QueryContext reference on the query for network operations
        if (query instanceof org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQuery) {
            ((org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQuery) query).setQueryContext(this);
        }
    }

    /**
     * Package-private constructor for testing that allows custom results.
     *
     * @param queryId unique identifier for this validation session
     * @param config configuration for this validation
     * @param datasetService service for accessing datasets
     * @param query the RDAP query being validated
     * @param results custom results object for testing
     */
    QueryContext(String queryId,
                RDAPValidatorConfiguration config,
                RDAPDatasetService datasetService,
                RDAPQuery query,
                RDAPValidatorResults results) {
        // Initialize immutable configuration
        this.queryId = queryId;
        this.config = config;
        this.datasetService = datasetService;
        this.query = query;
        this.queryType = (query != null) ? determineQueryType() : RDAPQueryType.DOMAIN; // Default for testing

        // Initialize mutable state holders (use provided results for testing)
        this.results = results;
        this.connectionTracker = new ConnectionTracker();
        this.resultFile = new RDAPValidationResultFile();
        this.dnsResolver = new DNSCacheResolver();
        // Initialize DNS cache once at startup with the target URL (skip for test URIs to improve performance)
        if (config.getUri() != null) {
            String uriString = config.getUri().toString();
            if (!uriString.contains("example.com")) {
                this.dnsResolver.initFromUrl(uriString);
            }
        }
        this.httpClientManager = new HttpClientManager();
        this.httpQueryTypeProcessor = new RDAPHttpQueryTypeProcessor();
        this.fileQueryTypeProcessor = new RDAPFileQueryTypeProcessor();
        this.networkInfo = new NetworkInfo();

        // Set QueryContext reference on the query for network operations
        if (query instanceof org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQuery) {
            ((org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQuery) query).setQueryContext(this);
        }
    }

    /**
     * Package-private constructor for testing that allows custom results and query type.
     *
     * @param queryId unique identifier for this validation session
     * @param config configuration for this validation
     * @param datasetService service for accessing datasets
     * @param query the RDAP query being validated
     * @param results custom results object for testing
     * @param queryType the query type for testing
     */
    public QueryContext(String queryId,
                       RDAPValidatorConfiguration config,
                       RDAPDatasetService datasetService,
                       RDAPQuery query,
                       RDAPValidatorResults results,
                       RDAPQueryType queryType) {
        // Initialize immutable configuration
        this.queryId = queryId;
        this.config = config;
        this.datasetService = datasetService;
        this.query = query;
        this.queryType = queryType; // Use provided query type for testing

        // Initialize mutable state holders (use provided results for testing)
        this.results = results;
        this.connectionTracker = new ConnectionTracker();
        this.resultFile = new RDAPValidationResultFile();
        this.dnsResolver = new DNSCacheResolver();
        // Initialize DNS cache once at startup with the target URL (skip for test URIs to improve performance)
        if (config.getUri() != null) {
            String uriString = config.getUri().toString();
            if (!uriString.contains("example.com")) {
                this.dnsResolver.initFromUrl(uriString);
            }
        }
        this.httpClientManager = new HttpClientManager();
        this.httpQueryTypeProcessor = new RDAPHttpQueryTypeProcessor();
        this.fileQueryTypeProcessor = new RDAPFileQueryTypeProcessor();
        this.networkInfo = new NetworkInfo();

        // Set QueryContext reference on the query for network operations
        if (query instanceof org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQuery) {
            ((org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQuery) query).setQueryContext(this);
        }
    }

    /**
     * Determines the query type based on the provided query and configuration.
     */
    private RDAPQueryType determineQueryType() {
        // Handle null URI (common in tests)
        if (config.getUri() == null) {
            return RDAPQueryType.DOMAIN; // Default for testing
        }

        if (config.getUri().getScheme().startsWith("file")) {
            fileQueryTypeProcessor.setConfiguration(config);
            return fileQueryTypeProcessor.getQueryType();
        } else {
            httpQueryTypeProcessor.setConfiguration(config);
            if (httpQueryTypeProcessor.determineQueryType()) {
                return httpQueryTypeProcessor.getQueryType();
            } else {
                return RDAPQueryType.DOMAIN; // Default fallback
            }
        }
    }

    // =========================
    // IMMUTABLE FIELD GETTERS
    // =========================

    public String getQueryId() {
        return queryId;
    }

    public RDAPValidatorConfiguration getConfig() {
        return config;
    }

    public RDAPDatasetService getDatasetService() {
        return datasetService;
    }

    public RDAPQuery getQuery() {
        return query;
    }

    public RDAPQueryType getQueryType() {
        return queryType;
    }

    /**
     * Set the query type for testing purposes.
     * @param queryType the new query type
     */
    public void setQueryType(RDAPQueryType queryType) {
        this.queryType = queryType;
    }

    // ============================
    // MUTABLE STATE HOLDER GETTERS
    // ============================

    public RDAPValidatorResults getResults() {
        return results;
    }

    public ConnectionTracker getConnectionTracker() {
        return connectionTracker;
    }

    public RDAPValidationResultFile getResultFile() {
        return resultFile;
    }

    public DNSCacheResolver getDnsResolver() {
        return dnsResolver;
    }

    public HttpClientManager getHttpClientManager() {
        return httpClientManager;
    }

    public RDAPHttpQueryTypeProcessor getHttpQueryTypeProcessor() {
        return httpQueryTypeProcessor;
    }

    public RDAPFileQueryTypeProcessor getFileQueryTypeProcessor() {
        return fileQueryTypeProcessor;
    }

    public NetworkInfo getNetworkInfo() {
        return networkInfo;
    }

    // ===============================
    // MUTABLE RESPONSE DATA ACCESSORS
    // ===============================

    public String getRdapResponseData() {
        return rdapResponseData;
    }

    public void setRdapResponseData(String rdapResponseData) {
        this.rdapResponseData = rdapResponseData;
    }

    public HttpResponse<String> getCurrentHttpResponse() {
        return currentHttpResponse;
    }

    public void setCurrentHttpResponse(HttpResponse<String> currentHttpResponse) {
        this.currentHttpResponse = currentHttpResponse;
    }

    public JSONObject getJsonResponseData() {
        return jsonResponseData;
    }

    public void setJsonResponseData(JSONObject jsonResponseData) {
        this.jsonResponseData = jsonResponseData;
    }

    // ================================
    // NETWORK CONFIGURATION METHODS
    // (delegates to NetworkInfo instance)
    // ================================

    public String getAcceptHeader() {
        return networkInfo.getAcceptHeaderValue();
    }

    public void setAcceptHeaderToApplicationJson() {
        networkInfo.setAcceptHeaderToApplicationJsonValue();
    }

    public void setAcceptHeaderToApplicationRdapJson() {
        networkInfo.setAcceptHeaderToApplicationRdapJsonValue();
    }

    public String getHttpMethod() {
        return networkInfo.getHttpMethodValue();
    }

    public void setHttpMethod(String httpMethod) {
        networkInfo.setHttpMethodValue(httpMethod);
    }

    public String getServerIpAddress() {
        return networkInfo.getServerIpAddressValue();
    }

    public void setServerIpAddress(String serverIpAddress) {
        networkInfo.setServerIpAddressValue(serverIpAddress);
    }

    public NetworkProtocol getNetworkProtocol() {
        return networkInfo.getNetworkProtocolValue();
    }

    public String getNetworkProtocolAsString() {
        return networkInfo.getNetworkProtocolAsStringValue();
    }

    public void setNetworkProtocol(NetworkProtocol networkProtocol) {
        networkInfo.setNetworkProtocolValue(networkProtocol);
    }

    public void setStackToV6() {
        networkInfo.setStackToV6Value();
    }

    public void setStackToV4() {
        networkInfo.setStackToV4Value();
    }

    // ================================
    // RESULT MANAGEMENT METHODS
    // ================================

    /**
     * Adds a validation error to the results without HTTP status code.
     * Replaces CommonUtils.addErrorToResultsFile(int, String, String)
     */
    public void addError(int code, String value, String message) {
        results.add(RDAPValidationResult.builder()
                .code(code)
                .value(value)
                .message(message)
                .build(this));
    }

    /**
     * Adds a validation error to the results with HTTP status code.
     * Replaces CommonUtils.addErrorToResultsFile(int, int, String, String)
     */
    public void addError(int httpStatusCode, int code, String value, String message) {
        results.add(RDAPValidationResult.builder()
                .httpStatusCode(httpStatusCode)
                .code(code)
                .value(value)
                .message(message)
                .build(this));
    }

    // =================
    // FACTORY METHODS
    // =================

    /**
     * Creates a QueryContext for production use.
     *
     * @param config the RDAP validator configuration
     * @param datasetService service for accessing RDAP datasets
     * @param query the RDAP query to be validated
     * @return a new QueryContext with unique query ID
     */
    public static QueryContext create(RDAPValidatorConfiguration config,
                                    RDAPDatasetService datasetService,
                                    RDAPQuery query) {
        return new QueryContext(UUID.randomUUID().toString(), config, datasetService, query);
    }

    /**
     * Creates a QueryContext for production use with custom DNS server.
     *
     * @param config the RDAP validator configuration
     * @param datasetService service for accessing RDAP datasets
     * @param query the RDAP query to be validated
     * @param customDnsServer custom DNS server IP address, or null to use system default
     * @return a new QueryContext with unique query ID and custom DNS resolver
     */
    public static QueryContext create(RDAPValidatorConfiguration config,
                                    RDAPDatasetService datasetService,
                                    RDAPQuery query,
                                    String customDnsServer) {
        return new QueryContext(UUID.randomUUID().toString(), config, datasetService, query, customDnsServer);
    }

    /**
     * Creates a QueryContext for testing with all default mock components.
     *
     * @return a new QueryContext suitable for testing
     */
    public static QueryContext forTesting() {
        return forTesting(createMockConfig(), createMockDatasetService(), createMockQuery());
    }

    /**
     * Creates a QueryContext for testing with custom configuration.
     *
     * @param config custom configuration for testing
     * @return a new QueryContext with mock dataset service and query
     */
    public static QueryContext forTesting(RDAPValidatorConfiguration config) {
        return forTesting(config, createMockDatasetService(), createMockQuery());
    }

    /**
     * Creates a QueryContext for testing with custom configuration and dataset service.
     *
     * @param config custom configuration for testing
     * @param datasetService custom dataset service for testing
     * @return a new QueryContext with mock query
     */
    public static QueryContext forTesting(RDAPValidatorConfiguration config,
                                        RDAPDatasetService datasetService) {
        return forTesting(config, datasetService, createMockQuery());
    }

    /**
     * Creates a QueryContext for testing with all custom components.
     *
     * @param config custom configuration for testing
     * @param datasetService custom dataset service for testing
     * @param query custom query for testing
     * @return a new QueryContext for testing
     */
    public static QueryContext forTesting(RDAPValidatorConfiguration config,
                                        RDAPDatasetService datasetService,
                                        RDAPQuery query) {
        return new QueryContext("test-" + UUID.randomUUID().toString(), config, datasetService, query);
    }

    /**
     * Creates a QueryContext for testing with RDAP response data and custom results.
     * This method is specifically designed for test classes that need to provide
     * their own results mock and RDAP response data.
     *
     * @param rdapResponseData the RDAP response data as JSON string
     * @param results custom results mock for testing
     * @param config custom configuration for testing
     * @return a new QueryContext configured for testing
     */
    public static QueryContext forTesting(String rdapResponseData,
                                        RDAPValidatorResults results,
                                        RDAPValidatorConfiguration config) {
        return forTesting(rdapResponseData, results, config, createMockDatasetService());
    }

    /**
     * Creates a QueryContext for testing with RDAP response data, custom results, and dataset service.
     * This method is specifically designed for test classes that need to provide
     * their own results mock, RDAP response data, and dataset service.
     *
     * @param rdapResponseData the RDAP response data as JSON string
     * @param results custom results mock for testing
     * @param config custom configuration for testing
     * @param datasetService custom dataset service for testing
     * @return a new QueryContext configured for testing
     */
    public static QueryContext forTesting(String rdapResponseData,
                                        RDAPValidatorResults results,
                                        RDAPValidatorConfiguration config,
                                        RDAPDatasetService datasetService) {
        // For testing, we need a null query since we don't have access to Mockito here
        // Tests that need specific query behavior should create their own mocks
        QueryContext context = new QueryContext("test-" + UUID.randomUUID().toString(), config, datasetService, null, results);

        // Set the RDAP response data for testing
        context.setRdapResponseData(rdapResponseData);

        return context;
    }

    // ====================
    // MOCK CREATION HELPERS
    // ====================

    private static RDAPValidatorConfiguration createMockConfig() {
        // TODO: Implement proper mock with all interface methods
        // For now, this is a placeholder - tests will need to provide their own mocks
        throw new UnsupportedOperationException("Mock config not yet implemented. Use QueryContext.forTesting(config, ...) with your own mock.");
    }

    private static RDAPDatasetService createMockDatasetService() {
        // Return null for default testing - tests should provide their own mocks when needed
        return null;
    }

    private static RDAPQuery createMockQuery() {
        // Return null for default testing - tests should provide their own mocks when needed
        return null;
    }

    @Override
    public String toString() {
        return "QueryContext{" +
                "queryId='" + queryId + '\'' +
                ", queryType=" + queryType +
                ", hasResponseData=" + (rdapResponseData != null) +
                ", resultsCount=" + results.getAll().size() +
                '}';
    }
}