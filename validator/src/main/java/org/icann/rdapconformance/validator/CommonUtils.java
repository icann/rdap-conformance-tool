package org.icann.rdapconformance.validator;

import java.io.InputStream;
import java.net.URI;

import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParser;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParserImpl;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.LocalFileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetServiceImpl;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
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
    public static final String HANDLE_PATTERN = "(\\w|_){1,80}-\\w{1,8}";

    // IP Address validation patterns
    public static final String IPV4_PATTERN = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    public static final String IP_BRACKET_ZONE_PATTERN = "[\\[\\]%.*]";

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
     * Adds a validation error to the results file without an HTTP status code.
     *
     * <p>This convenience method creates a validation result with the provided error
     * code, value, and message, then adds it to the global results collection. This
     * is typically used for validation errors that don't involve HTTP responses.</p>
     *
     * @param code the error code identifying the specific validation failure
     * @param value the value that caused the validation error (e.g., domain name, IP address)
     * @param message descriptive message explaining the validation failure
     */
    public static void addErrorToResultsFile(int code, String value, String message) {
        RDAPValidatorResultsImpl.getInstance()
                                .add(RDAPValidationResult.builder().code(code).value(value).message(message).build());

    }

    /**
     * Adds a validation error to the results file with an HTTP status code.
     *
     * <p>This method creates a validation result with the provided HTTP status code,
     * error code, value, and message, then adds it to the global results collection.
     * This is typically used for validation errors that involve HTTP responses.</p>
     *
     * @param httpStatusCode the HTTP status code from the response (e.g., 404, 500)
     * @param code the error code identifying the specific validation failure
     * @param value the value that caused the validation error (e.g., domain name, IP address)
     * @param message descriptive message explaining the validation failure
     */
    public static void addErrorToResultsFile(int httpStatusCode, int code, String value, String message) {
        RDAPValidatorResultsImpl.getInstance()
                                .add(RDAPValidationResult.builder()
                                                         .httpStatusCode(httpStatusCode)
                                                         .code(code)
                                                         .value(value)
                                                         .message(message)
                                                         .build());

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
     * Initializes an RDAP dataset service using the provided configuration.
     *
     * <p>This convenience method calls the overloaded version with a null progress callback.</p>
     *
     * @param config the validator configuration containing dataset settings
     * @return initialized RDAPDatasetService instance, or null if initialization failed
     * @see #initializeDataSet(RDAPValidatorConfiguration, ProgressCallback)
     */
    public static RDAPDatasetService initializeDataSet(RDAPValidatorConfiguration config) {
    return initializeDataSet(config, null);
}

/**
 * Initializes an RDAP dataset service using the provided configuration and progress callback.
 *
 * <p>This method creates and configures an RDAPDatasetService instance, downloads necessary
 * datasets based on the configuration, and provides progress updates through the callback.
 * If dataset download fails, the method returns null.</p>
 *
 * @param config the validator configuration containing dataset settings
 * @param progressCallback optional callback for receiving download progress updates, may be null
 * @return initialized RDAPDatasetService instance, or null if initialization or download failed
 * @throws SecurityException if security restrictions prevent dataset access
 * @throws IllegalArgumentException if configuration parameters are invalid
 */
public static RDAPDatasetService initializeDataSet(RDAPValidatorConfiguration config, ProgressCallback progressCallback) {
    RDAPDatasetService datasetService = null;
    try {
        datasetService = RDAPDatasetServiceImpl.getInstance(new LocalFileSystem());
        if(!datasetService.download(config.useLocalDatasets(), progressCallback)) {
            return null;
        }
    } catch (SecurityException  | IllegalArgumentException e) {
        logger.error(ToolResult.FILE_READ_ERROR.getDescription());
        System.exit(ToolResult.FILE_READ_ERROR.getCode());
    }
    return datasetService;
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
}
