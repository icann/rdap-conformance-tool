package org.icann.rdapconformance.validator;

import java.io.InputStream;
import java.net.URI;

import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParser;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParserImpl;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQueryTypeProcessor;

import org.slf4j.LoggerFactory;

/**
 * Utility class providing common constants, helper methods, and shared functionality
 * for RDAP validation operations.
 *
 * <p>This class serves as a central repository for commonly used constants, string
 * manipulation utilities, error handling helpers, and configuration management methods
 * used throughout the RDAP validation framework.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>Common protocol and HTTP constants (ports, methods, status codes)</li>
 *   <li>String manipulation utilities for URL and URI processing</li>
 *   <li>Error reporting helpers that integrate with validation results</li>
 *   <li>Dataset initialization and configuration file management</li>
 *   <li>Query type replacement utilities for RDAP URL manipulation</li>
 * </ul>
 *
 * <p>This class contains only static methods and constants - it cannot be instantiated.
 * All functionality is accessed through static method calls, making it easy to use
 * throughout the codebase without object creation overhead.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * // Using constants
 * String url = CommonUtils.HTTPS_PREFIX + "example.com" + CommonUtils.SLASH + "rdap";
 *
 * // Error reporting
 * CommonUtils.addErrorToResultsFile(-12345, "test.com", "Domain validation failed");
 *
 * // Dataset initialization
 * RDAPDatasetService service = CommonUtils.initializeDataSet(config);
 *
 * // String cleaning
 * String cleaned = CommonUtils.cleanStringFromExtraSlash("http://example.com//path");
 * </pre>
 *
 * @see RDAPValidatorConfiguration
 * @see RDAPDatasetService
 * @see ConfigurationFile
 * @since 1.0.0
 */
public class CommonUtils {

    // Strings
    public static final String DOT = ".";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String HTTP_PREFIX = "http://";
    public static final String HTTPS_PREFIX = "https://";
    public static final String SLASH = "/";
    public static final String DOUBLE_SLASH = "//";
    public static final String SEP = "://";
    public static final String LOCALHOST = "localhost";
    public static final String LOCAL_IPv4 = "127.0.0.1";
    public static final String LOCAL_IPv6 = "0000:0000:0000:0000:0000:0000:0000:0001";
    public static final String LOCAL_IPv6_COMPRESSED = "::1";
    public static final String ICANN_ORG_FQDN = "icann.org.";
    public static final String GET = "GET";
    public static final String HEAD = "HEAD";
    public static final String SEMI_COLON = ";";
    public static final String DASH = "-";
    public static final String EMPTY_STRING = "";
    public static final String LOCATION = "Location";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String DOMAIN = "domain";
    public static final String NAMESERVER = "nameserver";
    public static final String AUTNUM = "autnum";
    public static final String ENTITY = "entity";
    public static final String IP = "ip";
    public static final String NAMESERVERS = "nameservers";

    // Numbers
    public static final int PAUSE = 1000;
    public static final int TIMEOUT_IN_5SECS = 5000;
    public static final int HTTPS_PORT = 443;
    public static final int HTTP_PORT = 80;
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final int THREE = 3;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_TOO_MANY_REQUESTS = 429;

    // Regex patterns
    public static final String HANDLE_PATTERN = "(\\w|_){1,80}-\\w{1,8}";
    // IP Address validation pattern
    public static final String IPV4_DOT_DECIMAL_PATTERN = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$";

    // Logger
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    /**
     * Create a URI from a string, with proper exception handling.
     *
     * @param href The URI string to parse
     * @return The created URI
     * @throws IllegalArgumentException if the URI is malformed
     */
    public static URI createUri(String href) {
        URI uri = URI.create(href);
        if (uri.getScheme() == null || uri.getHost() == null) {
            throw new IllegalArgumentException("Missing scheme or host component");
        }
        return uri;
    }

    /**
     * Get the scheme from a URI, with null safety.
     *
     * @param uri The URI to extract scheme from
     * @return The URI scheme, or null if not present
     */
    public static String getUriScheme(URI uri) {
        return uri.getScheme();
    }

    /**
     * Get the host from a URI, with null safety.
     *
     * @param uri The URI to extract host from
     * @return The URI host, or null if not present
     */
    public static String getUriHost(URI uri) {
        return uri.getHost();
    }

    /**
     * Replaces RDAP query type segments in URLs with a replacement word.
     *
     * <p>This method takes an RDAP URL containing a query type segment (like "/domain",
     * "/nameserver", etc.) and replaces it with the provided replacement word. This is
     * useful for transforming RDAP URLs for testing or validation purposes.</p>
     *
     * @param httpQueryType the RDAP query type that determines which segment to replace
     * @param originalString the original URL string containing the query type segment
     * @param replacementWord the word to replace the query type segment with
     * @return the modified string with the query type segment replaced, or the original
     *         string if no matching query type is found
     */
    public static String replaceQueryTypeInStringWith(RDAPHttpQueryTypeProcessor.RDAPHttpQueryType httpQueryType,
                                                      String originalString,
                                                      String replacementWord) {
        return switch (httpQueryType) {
            case RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.DOMAIN ->
                originalString.replace(SLASH + DOMAIN, replacementWord);
            case RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.NAMESERVER ->
                originalString.replace(SLASH + NAMESERVER, replacementWord);
            case RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.AUTNUM ->
                originalString.replace(SLASH + AUTNUM, replacementWord);
            case RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.ENTITY ->
                originalString.replace(SLASH + ENTITY, replacementWord);
            case RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.IP -> originalString.replace(SLASH + IP, replacementWord);
            case RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.NAMESERVERS ->
                originalString.replace(SLASH + NAMESERVERS, replacementWord);
            default -> originalString;
        };
    }

    /**
     * Removes extra slashes from a string and trailing slashes.
     *
     * <p>This utility method cleans up URL strings by removing double slashes
     * (except in protocols like "http://") and removing trailing slashes.
     * This helps normalize URLs for consistent processing.</p>
     *
     * @param input the string to clean up, may be null
     * @return the cleaned string with extra slashes removed, or the original
     *         input if it was null
     */
    public static String cleanStringFromExtraSlash(String input) {
        if (input != null) {
            String uriCleaned = input.replaceAll(DOUBLE_SLASH, SLASH);
            if (uriCleaned.endsWith(SLASH)) {
                return input.substring(ZERO, input.length() - ONE);
            }
        }

        return input;
    }


    /**
     * Checks if the configuration file specified in the configuration exists.
     *
     * <p>This method verifies the existence of configuration files for both local file
     * paths and URI-based configurations. For non-file URIs (like HTTP), it assumes
     * existence and lets later validation handle any access failures.</p>
     *
     * @param config the validator configuration containing the configuration file URI
     * @param fileSystem the file system abstraction for file operations
     * @return true if the file exists or is a non-file URI, false if the local file doesn't exist
     */
    public static boolean configFileExists(RDAPValidatorConfiguration config, FileSystem fileSystem) {
        java.net.URI configUri = config.getConfigurationFile();
        String filePath;
        
        // Convert URI to file path for existence check
        if (!configUri.isAbsolute()) {
            filePath = java.nio.file.Path.of(configUri.toString()).toAbsolutePath().toString();
        } else if ("file".equals(configUri.getScheme())) {
            filePath = new java.io.File(configUri).getAbsolutePath();
        } else {
            // For non-file URIs (http, etc), we assume they exist (will fail later if they don't)
            return true;
        }
        
        // Check if file exists (only for local files)
        if (configUri.getScheme() == null || "file".equals(configUri.getScheme())) {
            return fileSystem.exists(filePath);
        }
        
        return true;
    }

    /**
     * Parses and verifies the configuration file specified in the configuration.
     *
     * <p>This method loads the configuration file from the URI specified in the
     * configuration, parses it using the configuration file parser, and returns
     * the parsed configuration object. If parsing fails for any reason, returns null.</p>
     *
     * @param config the validator configuration containing the configuration file URI
     * @param fileSystem the file system abstraction for file operations
     * @return the parsed ConfigurationFile object, or null if parsing failed
     */
    public static ConfigurationFile verifyConfigFile(RDAPValidatorConfiguration config, FileSystem fileSystem) {
        ConfigurationFile configFile = null;
        try (InputStream is = fileSystem.uriToStream(config.getConfigurationFile())) {
            ConfigurationFileParser configParser = new ConfigurationFileParserImpl();
            configFile = configParser.parse(is);
        } catch (Exception e) {
            return null;
        }
        return configFile;
    }

    // Error code for resource not found warning
    public static final int RESOURCE_NOT_FOUND_WARNING_CODE = -13020;

    // Warning message for 404 responses
    public static final String RESOURCE_NOT_FOUND_MESSAGE =
        "This URL returned an HTTP 404 status code that was validly formed. If the provided URL " +
        "does not reference a registered resource, then this warning may be ignored. If the provided URL " +
        "does reference a registered resource, then this should be considered an error.";

    /**
     * Handles the resource not found (404) warning logic for both CLI and web validator.
     *
     * <p>This method checks if all relevant queries returned 404 status codes and, if so,
     * adds the appropriate warning to the validation results. This centralizes the 404
     * handling logic that was previously duplicated between RdapConformanceTool and
     * RdapWebValidator.</p>
     *
     * <p>The warning is added when:</p>
     * <ul>
     *   <li>All HEAD and main GET queries returned 404 status</li>
     *   <li>Either a gTLD profile is in use (Feb 2019 or Feb 2024) with registry/registrar mode</li>
     *   <li>Or it's a plain GET request without profile requirements</li>
     * </ul>
     *
     * @param queryContext the QueryContext containing connection tracker and results
     * @param config the validator configuration with profile and mode settings
     * @return true if all relevant queries returned 404 (caller may want to filter errors), false otherwise
     */
    public static boolean handleResourceNotFoundWarning(QueryContext queryContext,
                                                        RDAPValidatorConfiguration config) {
        ConnectionTracker tracker = queryContext.getConnectionTracker();

        // Use the pure query method to check if all relevant queries returned 404
        if (!tracker.areAllRelevantQueriesNotFound()) {
            logger.debug("At least one HEAD or Main query returned a non-404 response code.");
            return false;
        }

        logger.debug("All HEAD and Main queries returned a 404 Not Found response code.");

        // Add the warning for 404 responses
        // The warning is added for all 404 cases - categorization as warning vs error
        // is handled by the configuration file's definitionWarning settings
        queryContext.addError(HTTP_NOT_FOUND, RESOURCE_NOT_FOUND_WARNING_CODE,
            config.getUri().toString(), RESOURCE_NOT_FOUND_MESSAGE);

        return true;
    }

    /**
     * Filters validation results for 404 responses, keeping only relevant results.
     *
     * <p>When all queries return 404, most validation errors are expected and should be
     * removed. This method filters results to keep only:</p>
     * <ul>
     *   <li>The -13020 resource not found warning</li>
     *   <li>Response format errors (-12100 to -12199) - validate error response structure</li>
     *   <li>Domain validation errors (-10300 to -10303) - validate user input</li>
     * </ul>
     *
     * <p>This matches the behavior of RDAPValidationResultFile.removeErrors() but works
     * directly with RDAPValidatorResults for use in RdapWebValidator.</p>
     *
     * @param results the validation results to filter
     */
    public static void filterResultsForResourceNotFound(
            org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults results) {
        var allResults = results.getAll();
        var filteredResults = allResults.stream()
            .filter(result -> isResultToKeepFor404(result.getCode()))
            .collect(java.util.stream.Collectors.toSet());

        results.clear();
        results.addAll(filteredResults);
        results.removeGroups();

        logger.debug("Filtered results for 404 response: kept {} of {} results",
            filteredResults.size(), allResults.size());
    }

    /**
     * Determines if a result should be kept when filtering for 404 responses.
     *
     * @param code the error/warning code
     * @return true if the result should be kept, false if it should be filtered out
     */
    private static boolean isResultToKeepFor404(int code) {
        // Keep the resource not found warning
        if (code == RESOURCE_NOT_FOUND_WARNING_CODE) {
            return true;
        }
        // Keep response format errors (-12100 to -12199)
        if (code <= -12100 && code >= -12199) {
            return true;
        }
        // Keep domain validation errors (-10300 to -10303)
        if (code >= -10303 && code <= -10300) {
            return true;
        }
        return false;
    }
}
