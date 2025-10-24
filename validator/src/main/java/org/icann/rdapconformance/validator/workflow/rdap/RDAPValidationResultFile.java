package org.icann.rdapconformance.validator.workflow.rdap;

import static org.icann.rdapconformance.validator.CommonUtils.DASH;
import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;
import static org.icann.rdapconformance.validator.exception.parser.ExceptionParser.UNKNOWN_ERROR_CODE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.icann.rdapconformance.validator.BuildInfo;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton service for managing RDAP validation results and generating formatted output files.
 *
 * <p>This class is responsible for collecting, processing, and formatting RDAP validation results
 * into structured JSON output files. It handles result aggregation, filtering, formatting, and
 * file generation with comprehensive metadata about the validation process.</p>
 *
 * <p>Key responsibilities include:</p>
 * <ul>
 *   <li>Collecting and aggregating validation results from multiple sources</li>
 *   <li>Filtering results based on configuration (errors, warnings, ignored codes)</li>
 *   <li>Detecting and handling duplicate IP address validation errors</li>
 *   <li>Ensuring consistent HTTP status codes across queries</li>
 *   <li>Formatting results into structured JSON with proper metadata</li>
 *   <li>Writing timestamped result files to the filesystem</li>
 * </ul>
 *
 * <p>The class operates as a singleton to ensure consistent result aggregation across
 * the entire validation process. It must be initialized once with the required
 * dependencies before use, and provides various methods for result manipulation
 * and output generation.</p>
 *
 * <p>Result formatting includes comprehensive metadata such as:</p>
 * <ul>
 *   <li>Definition identifier and tested URI information</li>
 *   <li>Test execution timestamp and configuration flags</li>
 *   <li>Tool version and build information</li>
 *   <li>Categorized results (errors, warnings, ignored items)</li>
 *   <li>Network and protocol-specific validation outcomes</li>
 * </ul>
 *
 * <p>The generated JSON files follow a standardized format suitable for
 * automated processing, reporting, and compliance verification.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
 * resultFile.initialize(results, config, configFile, fileSystem);
 * boolean success = resultFile.build();
 * if (success) {
 *     String path = resultFile.getResultsPath();
 *     // Process generated result file
 * }
 * </pre>
 *
 * @see RDAPValidationResult
 * @see RDAPValidatorConfiguration
 * @see ConfigurationFile
 * @since 1.0.0
 */
public class RDAPValidationResultFile {

    private static final Logger logger = LoggerFactory.getLogger(RDAPValidationResultFile.class);

    // Singleton instance - using volatile for thread-safe lazy initialization
    private static volatile RDAPValidationResultFile instance;

    // Session-keyed storage for concurrent validation requests
    private static final ConcurrentHashMap<String, RDAPValidatorResults> sessionResults = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, RDAPValidatorConfiguration> sessionConfigs = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ConfigurationFile> sessionConfigFiles = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, FileSystem> sessionFileSystems = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> sessionResultPaths = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Boolean> sessionInitialized = new ConcurrentHashMap<>();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private RDAPValidationResultFile() {
    }

    /**
     * Returns the singleton instance of the validation result file manager.
     *
     * <p>This method provides thread-safe access to the singleton instance using
     * double-checked locking pattern for optimal performance under high concurrency.
     * The instance must be initialized before use through the initialize method.</p>
     *
     * @return the singleton RDAPValidationResultFile instance
     */
    public static RDAPValidationResultFile getInstance() {
        // First check without synchronization for performance
        RDAPValidationResultFile result = instance;
        if (result == null) {
            synchronized (RDAPValidationResultFile.class) {
                // Second check with synchronization for safety
                result = instance;
                if (result == null) {
                    instance = result = new RDAPValidationResultFile();
                }
            }
        }
        return result;
    }

    /**
     * Initializes the result file manager with required dependencies for a specific session.
     *
     * <p>This method must be called once per session before using other methods. It sets up
     * the necessary dependencies for result processing and file generation.
     * Subsequent calls to this method for the same sessionId are ignored to prevent reinitialization.</p>
     *
     * @param sessionId unique identifier for this validation session
     * @param results the validation results collection
     * @param config the validator configuration
     * @param configurationFile the configuration file with validation rules
     * @param fileSystem the file system abstraction for file operations
     */
    public void initialize(String sessionId,
                           RDAPValidatorResults results,
                           RDAPValidatorConfiguration config,
                           ConfigurationFile configurationFile,
                           FileSystem fileSystem) {
        if (sessionInitialized.getOrDefault(sessionId, false)) {
            return;
        }
        sessionInitialized.put(sessionId, true);
        sessionResults.put(sessionId, results);
        sessionConfigs.put(sessionId, config);
        sessionConfigFiles.put(sessionId, configurationFile);
        // Handle null fileSystem since ConcurrentHashMap doesn't allow null values
        if (fileSystem != null) {
            sessionFileSystems.put(sessionId, fileSystem);
        } else {
            sessionFileSystems.remove(sessionId); // Remove any existing value
        }
    }

    /**
     * Legacy initialize method for backward compatibility.
     * Creates a default session ID for existing code.
     *
     * @deprecated Use initialize(String sessionId, ...) instead
     */
    @Deprecated
    public void initialize(RDAPValidatorResults results,
                           RDAPValidatorConfiguration config,
                           ConfigurationFile configurationFile,
                           FileSystem fileSystem) {
        initialize("default", results, config, configurationFile, fileSystem);
    }

    /**
     * Resets the singleton instance and clears all session data for testing purposes.
     *
     * <p>This method is intended for use in unit tests to ensure a clean
     * state between test runs. It should not be used in production code.</p>
     */
    public static void reset() {
        instance = null;
        clearAllSessions();
    }

    /**
     * Clears all session data.
     *
     * <p>This method removes all stored session data from the singleton.
     * Use with caution as it affects all active sessions.</p>
     */
    public static void clearAllSessions() {
        sessionResults.clear();
        sessionConfigs.clear();
        sessionConfigFiles.clear();
        sessionFileSystems.clear();
        sessionResultPaths.clear();
        sessionInitialized.clear();
    }

    /**
     * Clears data for a specific session.
     *
     * <p>This method removes all stored data for the specified session ID,
     * effectively cleaning up resources for a completed validation.</p>
     *
     * @param sessionId the session ID to clear
     */
    public static void clearSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        sessionResults.remove(sessionId);
        sessionConfigs.remove(sessionId);
        sessionConfigFiles.remove(sessionId);
        sessionFileSystems.remove(sessionId);
        sessionResultPaths.remove(sessionId);
        sessionInitialized.remove(sessionId);
    }

    private static String getFilename() {
        String datetimePattern = "yyyyMMddHHmmss";
        String dateTime = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(datetimePattern));
        return String.format("results-%s.json", dateTime);
    }

    /**
     * Builds and saves the validation results to a JSON file for a specific session.
     *
     * <p>This method processes all collected validation results, applies filtering
     * and formatting rules, and generates a comprehensive JSON output file containing
     * structured validation results with metadata. The file includes categorized
     * results (errors, warnings, ignored), configuration information, and test
     * execution details.</p>
     *
     * <p>The output file is saved to either a configured path or a timestamped
     * default location in the results directory. The JSON format is standardized
     * and suitable for automated processing and reporting.</p>
     *
     * @param sessionId the session ID to build results for
     * @return true if the result file was successfully created and written, false otherwise
     */
    public boolean build(String sessionId) {
        RDAPValidatorResults results = sessionResults.get(sessionId);
        RDAPValidatorConfiguration config = sessionConfigs.get(sessionId);
        ConfigurationFile configurationFile = sessionConfigFiles.get(sessionId);
        FileSystem fileSystem = sessionFileSystems.get(sessionId);

        if (results == null || config == null || configurationFile == null) {
            logger.error("Session {} not properly initialized for build operation", sessionId);
            return false;
        }
        Map<String, Object> fileMap = new HashMap<>();
        fileMap.put("definitionIdentifier", configurationFile.getDefinitionIdentifier());
        fileMap.put("testedURI", config.getUri());
        fileMap.put("testedDate", Instant.now().toString());
        fileMap.put("groupOK", results.getGroupOk());
        fileMap.put("groupErrorWarning", results.getGroupErrorWarning());
        fileMap.put("results", this.createResultsMap(sessionId));
        fileMap.put("gtldRegistrar", config.isGtldRegistrar());
        fileMap.put("gtldRegistry", config.isGtldRegistry());
        fileMap.put("thinRegistry", config.isThin());
        fileMap.put("rdapProfileFebruary2019", config.useRdapProfileFeb2019());
        fileMap.put("rdapProfileFebruary2024", config.useRdapProfileFeb2024());
        fileMap.put("additionalConformanceQueries", config.isAdditionalConformanceQueries());
        fileMap.put("noIpv4", config.isNoIpv4Queries());
        fileMap.put("noIpv6", config.isNoIpv6Queries());

        if (config.useRdapProfileFeb2024()) {
            fileMap.put("conformanceToolVersion", BuildInfo.getVersion());
            fileMap.put("buildDate", BuildInfo.getBuildDate());
        }

        JSONObject object = new JSONObject(fileMap);
        String resultsFilePath = config.getResultsFile();
        Path path;

        if (resultsFilePath != null && !resultsFilePath.isEmpty()) {
            path = Paths.get(resultsFilePath);
        } else {
            path = Paths.get("results", getFilename());
        }

        try {
            String resultPath = path.toString();
            sessionResultPaths.put(sessionId, resultPath);

            // Skip file operations if fileSystem is null (e.g., in tests)
            if (fileSystem != null) {
                if (resultsFilePath == null || resultsFilePath.isEmpty()) {
                    fileSystem.mkdir("results");
                }
                fileSystem.write(path.toString(), object.toString(4));
            } else {
                logger.debug("FileSystem is null for session {}, skipping file write operations", sessionId);
            }
            return true;
        } catch (IOException e) {
            logger.debug("Failed to write results into {}", path, e);
            return false;
        }
    }

    /**
     * Legacy build method for backward compatibility.
     * Uses the default session ID.
     *
     * @deprecated Use build(String sessionId) instead
     */
    @Deprecated
    public boolean build() {
        return build("default");
    }

    // if you sent us a 0 - that means it gets NULLed out
    private Object formatStatusCode(Integer statusCode) {
        return statusCode != null && statusCode == ZERO ? JSONObject.NULL : statusCode;
    }

    // if you sent us a dash - that means it gets NULLed out
    private Object formatStringToNull(String maybeDash) {
        if (maybeDash == null || maybeDash.equals(DASH)) {
            return JSONObject.NULL;
        } else {
            return maybeDash;
        }
    }

    /**
     * Creates a structured map of validation results categorized by type for a specific session.
     *
     * <p>This method processes all validation results and organizes them into
     * errors, warnings, and ignored categories based on the configuration file
     * definitions. It also applies filtering to remove duplicates and adds
     * consistency checks for HTTP status codes across queries.</p>
     *
     * <p>The returned map contains:</p>
     * <ul>
     *   <li>error: List of error results with full details</li>
     *   <li>warning: List of warning results with full details</li>
     *   <li>ignore: List of ignored error codes</li>
     *   <li>notes: Configuration notes and explanations</li>
     * </ul>
     *
     * @param sessionId the session ID to create results map for
     * @return structured map containing categorized validation results
     */
    public Map<String, Object> createResultsMap(String sessionId) {
        RDAPValidatorResults results = sessionResults.get(sessionId);
        RDAPValidatorConfiguration config = sessionConfigs.get(sessionId);
        ConfigurationFile configurationFile = sessionConfigFiles.get(sessionId);

        if (results == null || config == null || configurationFile == null) {
            logger.error("Session {} not properly initialized for createResultsMap operation", sessionId);
            return new HashMap<>();
        }
        Map<String, Object> resultsMap = new HashMap<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        List<Map<String, Object>> warnings = new ArrayList<>();

        Set<RDAPValidationResult> allResults;
        Set<RDAPValidationResult> filteredResults;

        if (results instanceof RDAPValidatorResultsImpl) {
            RDAPValidatorResultsImpl resultsImpl = (RDAPValidatorResultsImpl) results;
            allResults = resultsImpl.getAll(sessionId);
            Set<Integer> codeToIgnore = new HashSet<>(configurationFile.getDefinitionIgnore());
            filteredResults = resultsImpl.getAll(sessionId)
                                                               .stream()
                                                               .filter(r -> !codeToIgnore.contains(r.getCode())
                                                                   && r.getCode() != UNKNOWN_ERROR_CODE)
                                                               .collect(Collectors.toSet());
        } else {
            // Fallback for mock objects or other implementations
            allResults = results.getAll();
            Set<Integer> codeToIgnore = new HashSet<>(configurationFile.getDefinitionIgnore());
            filteredResults = results.getAll()
                                                               .stream()
                                                               .filter(r -> !codeToIgnore.contains(r.getCode())
                                                                   && r.getCode() != UNKNOWN_ERROR_CODE)
                                                               .collect(Collectors.toSet());
        }

        filteredResults = cullDuplicateIPAddressErrors(filteredResults);
        filteredResults = addErrorIfAllQueriesDoNotReturnSameStatusCode(filteredResults);

        //  Finally build the resultMap
        for (RDAPValidationResult result : filteredResults) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("code", result.getCode());
            resultMap.put("value", result.getValue());
            resultMap.put("message", result.getMessage());
            
            Object formattedStatus = formatStatusCode(result.getHttpStatusCode());
            resultMap.put("receivedHttpStatusCode", formattedStatus);
            resultMap.put("queriedURI",
                Objects.nonNull(result.getQueriedURI()) ? formatStringToNull(result.getQueriedURI())
                    : (Objects.nonNull(config.getUri()) ? config.getUri().toString() : StringUtils.EMPTY));
            resultMap.put("acceptMediaType", formatStringToNull(result.getAcceptHeader()));
            resultMap.put("httpMethod", formatStringToNull(result.getHttpMethod()));
            resultMap.put("serverIpAddress", formatStringToNull(result.getServerIpAddress()));
            resultMap.put("notes", configurationFile.getAlertNotes(result.getCode()));
            if (configurationFile.isError(result.getCode())) {
                errors.add(resultMap);
            } else if (configurationFile.isWarning(result.getCode())) {
                warnings.add(resultMap);
            } else {
                // TODO what is the default?
                errors.add(resultMap);
            }
        }

        resultsMap.put("error", errors);
        resultsMap.put("warning", warnings);
        resultsMap.put("ignore", configurationFile.getDefinitionIgnore());
        resultsMap.put("notes", configurationFile.getDefinitionNotes());
        return resultsMap;
    }

    /**
     * Legacy createResultsMap method for backward compatibility.
     * Uses the default session ID.
     *
     * @deprecated Use createResultsMap(String sessionId) instead
     */
    @Deprecated
    public Map<String, Object> createResultsMap() {
        return createResultsMap("default");
    }

    public Set<RDAPValidationResult> addErrorIfAllQueriesDoNotReturnSameStatusCode(Set<RDAPValidationResult> allResults) {
        List<RDAPValidationResult> filtered = new ArrayList<>();
        for (RDAPValidationResult result : allResults) {
            int code = result.getCode();
            // Filter out these codes, they are not relevant for this check and are exempt
            //  -13004 (Blind Copy Queries), -13005 (Redirect to itself), -13006 (test.Invalid), -65300 (Invalid Domain Query was not a 404)
            if (code != -13004 && code != -13005 && code != -13006 && code != -65300) {
                filtered.add(result);
            }
        }

        // Create unique tuples of (code, httpStatusCode)
        Set<List<Object>> uniqueTuples = new HashSet<>();
        for (RDAPValidationResult result : filtered) {
            List<Object> tuple = new ArrayList<>();
            tuple.add(result.getCode());
            Integer status = result.getHttpStatusCode();
            // Always normalize null to 0 (ZERO) for consistent comparison
            tuple.add(status == null ? ZERO : status);
            uniqueTuples.add(tuple);
        }

        // Collect httpStatusCodes - normalize null to 0
        Set<Integer> statusCodes = new HashSet<>();
        for (RDAPValidationResult result : filtered) {
            Integer statusCode = result.getHttpStatusCode();
            statusCodes.add(statusCode == null ? ZERO : statusCode);
        }

        // we still care about all our results, that's what gets returned
        Set<RDAPValidationResult> updatedResults = new HashSet<>(allResults);

        // If not all the same, add the new error code
        if (statusCodes.size() > ONE) {
            logger.debug("Not all status codes are the same");
            String tupleListJson = "[]";
            try {
                ObjectMapper mapper = new ObjectMapper();
                tupleListJson = mapper.writeValueAsString(new ArrayList<>(uniqueTuples));
            } catch (JsonProcessingException e) {
                logger.debug("Error serializing tuple list to JSON", e);
                // we can't parse them so just add the empty array
            }

            updatedResults.add(RDAPValidationResult.builder()
                                                   .acceptHeader(DASH)
                                                   .queriedURI(DASH)
                                                   .httpMethod(DASH)
                                                   .httpStatusCode(ZERO)
                                                   .code(-13018)
                                                   .value(tupleListJson)
                                                   .message("Queries do not produce the same HTTP status code.")
                                                   .build());
        } else {
            logger.debug("All status codes are the same");
        }
        return updatedResults;
    }

    // New culling function
    public Set<RDAPValidationResult> cullDuplicateIPAddressErrors(Set<RDAPValidationResult> results) {
        int ipv4Count = ZERO;
        int ipv6Count = ZERO;
        Set<RDAPValidationResult> toRemove = new HashSet<>();

        // First pass - count occurrences
        for (RDAPValidationResult result : results) {
            if (result.getCode() == -20400) {
                ipv4Count++;
                if (ipv4Count > ONE) {
                    toRemove.add(result);
                }
            } else if (result.getCode() == -20401) {
                ipv6Count++;
                if (ipv6Count > ONE) {
                    toRemove.add(result);
                }
            }
        }

        // Remove duplicates
        results.removeAll(toRemove);
        return results;
    }

    /**
     * Returns the total count of validation errors for a specific session.
     *
     * <p>This method counts all results that are classified as errors based on
     * the configuration file definitions. This includes explicitly defined errors
     * and results that are not categorized as warnings or ignored.</p>
     *
     * @param sessionId the session ID to count errors for
     * @return the number of validation errors found
     */
    public int getErrorCount(String sessionId) {
        return getErrors(sessionId).size();
    }

    /**
     * Legacy getErrorCount method for backward compatibility.
     * Uses the default session ID.
     *
     * @deprecated Use getErrorCount(String sessionId) instead
     */
    @Deprecated
    public int getErrorCount() {
        return getErrorCount("default");
    }

    /**
     * Returns a list of all validation results classified as errors for a specific session.
     *
     * <p>This method filters the complete result set to return only those results
     * that are considered errors based on the configuration file definitions.
     * Results are classified as errors if they are explicitly defined as errors
     * or if they are not categorized as warnings or ignored codes.</p>
     *
     * @param sessionId the session ID to get errors for
     * @return list of validation results that are classified as errors
     */
    public List<RDAPValidationResult> getErrors(String sessionId) {
        RDAPValidatorResults results = sessionResults.get(sessionId);
        ConfigurationFile configurationFile = sessionConfigFiles.get(sessionId);

        if (results == null || configurationFile == null) {
            return new ArrayList<>();
        }

        Set<RDAPValidationResult> allResults;
        if (results instanceof RDAPValidatorResultsImpl) {
            RDAPValidatorResultsImpl resultsImpl = (RDAPValidatorResultsImpl) results;
            allResults = resultsImpl.getAll(sessionId);
        } else {
            // Fallback for mock objects or other implementations
            allResults = results.getAll();
        }

        return allResults.stream()
                      .filter(result -> configurationFile.isError(result.getCode()) || (
                          !configurationFile.isWarning(result.getCode())
                              && !configurationFile.getDefinitionIgnore().contains(result.getCode())))
                      .collect(Collectors.toList());
    }

    /**
     * Legacy getErrors method for backward compatibility.
     * Uses the default session ID.
     *
     * @deprecated Use getErrors(String sessionId) instead
     */
    @Deprecated
    public List<RDAPValidationResult> getErrors() {
        return getErrors("default");
    }

    /**
     * Removes error results from the result set for a specific session, keeping only warnings and response format errors.
     *
     * <p>This method filters the results to retain only warnings and specific response
     * format validation errors that should be preserved regardless of the overall
     * response status. This is typically used when processing 404 responses where
     * most errors are expected but response format validation should still apply.</p>
     *
     * @param sessionId the session ID to remove errors for
     */
    public void removeErrors(String sessionId) {
        RDAPValidatorResults results = sessionResults.get(sessionId);
        ConfigurationFile configurationFile = sessionConfigFiles.get(sessionId);

        if (results == null || configurationFile == null) {
            return;
        }

        Set<RDAPValidationResult> filteredResults;

        if (results instanceof RDAPValidatorResultsImpl) {
            RDAPValidatorResultsImpl resultsImpl = (RDAPValidatorResultsImpl) results;
            filteredResults = resultsImpl.getAll(sessionId)
                    .stream()
                    .filter(result -> configurationFile.isWarning(result.getCode())
                                      || isResponseFormatError(result.getCode())
                                      || isDomainValidationError(result.getCode()))
                    .collect(Collectors.toSet());

            resultsImpl.clear(sessionId);
            resultsImpl.addAll(sessionId, filteredResults);
        } else {
            // Fallback for mock objects or other implementations
            filteredResults = results.getAll()
                    .stream()
                    .filter(result -> configurationFile.isWarning(result.getCode())
                                      || isResponseFormatError(result.getCode())
                                      || isDomainValidationError(result.getCode()))
                    .collect(Collectors.toSet());

            results.clear();
            results.addAll(filteredResults);
        }
    }

    /**
     * Legacy removeErrors method for backward compatibility.
     * Uses the default session ID.
     *
     * @deprecated Use removeErrors(String sessionId) instead
     */
    @Deprecated
    public void removeErrors() {
        removeErrors("default");
    }

    /**
     * Determines if an error code represents response format validation.
     * Response format errors (like missing errorCode field) should be preserved
     * even when filtering errors for 404 responses, as they validate the structure
     * of the error response itself rather than the missing resource.
     */
    private boolean isResponseFormatError(int code) {
        // -121XX series are response format validation errors that should be preserved
        return code <= -12100 && code >= -12199;
    }

    /**
     * Determines if an error code represents domain validation.
     * Domain validation errors should be preserved even when filtering errors for
     * 404 responses, as they validate the input domain name provided by the user
     * rather than the server response.
     */
    private boolean isDomainValidationError(int code) {
        // Domain validation error codes: -10300 to -10303
        return code >= -10303 && code <= -10300;
    }

    /**
     * Removes result groups from the validation results for a specific session.
     *
     * <p>This method clears any grouped results from the result set, typically
     * used to reset or clean up the results collection during processing.</p>
     *
     * @param sessionId the session ID to remove result groups for
     */
    public void removeResultGroups(String sessionId) {
        RDAPValidatorResults results = sessionResults.get(sessionId);
        if (results != null) {
            results.removeGroups();
        }
    }

    /**
     * Legacy removeResultGroups method for backward compatibility.
     * Uses the default session ID.
     *
     * @deprecated Use removeResultGroups(String sessionId) instead
     */
    @Deprecated
    public void removeResultGroups() {
        removeResultGroups("default");
    }

    /**
     * Returns a list of all validation results for a specific session.
     *
     * <p>This method provides access to the complete set of validation results
     * without any filtering or categorization. The returned list is a copy
     * to prevent external modification of the internal result set.</p>
     *
     * @param sessionId the session ID to get results for
     * @return list containing all validation results, or empty list if session not found
     */
    public List<RDAPValidationResult> getAllResults(String sessionId) {
        RDAPValidatorResults results = sessionResults.get(sessionId);
        if (results == null) {
            return new ArrayList<>();
        } else if (results instanceof RDAPValidatorResultsImpl) {
            return new ArrayList<>(((RDAPValidatorResultsImpl) results).getAll(sessionId));
        } else {
            // Fallback for mock objects or other implementations
            return new ArrayList<>(results.getAll());
        }
    }

    /**
     * Legacy getAllResults method for backward compatibility.
     * Uses the default session ID.
     *
     * @deprecated Use getAllResults(String sessionId) instead
     */
    @Deprecated
    public List<RDAPValidationResult> getAllResults() {
        return getAllResults("default");
    }

    /**
     * Returns the file path where the validation results were written for a specific session.
     *
     * <p>This method provides access to the actual file path used for the
     * generated results file. The path is set during the build process and
     * reflects either the configured output path or the automatically
     * generated timestamped filename.</p>
     *
     * @param sessionId the session ID to get results path for
     * @return the file path of the generated results file, or null if not yet built
     */
    public String getResultsPath(String sessionId) {
        return sessionResultPaths.get(sessionId);
    }

    /**
     * Legacy getResultsPath method for backward compatibility.
     * Uses the default session ID.
     *
     * @deprecated Use getResultsPath(String sessionId) instead
     */
    @Deprecated
    public String getResultsPath() {
        return getResultsPath("default");
    }

// manual debug usage only -- unused right now
/**
 * Debug method to print result breakdown for a specific session.
 * @param sessionId the session ID to debug
 */
public void debugPrintResultBreakdown(String sessionId) {
    List<RDAPValidationResult> allResults = getAllResults(sessionId);
    List<RDAPValidationResult> errors = getErrors(sessionId);
    ConfigurationFile configurationFile = sessionConfigFiles.get(sessionId);

    if (configurationFile == null) {
        logger.debug("No configuration file found for session: {}", sessionId);
        return;
    }

    logger.debug("Total Results: {}", allResults.size());
    logger.debug("Error Results: {}", errors.size());

    // Count results by category
    int explicitErrors = ZERO;
    int explicitWarnings = ZERO;
    int explicitIgnored = ZERO;
    int implicitErrors = ZERO; // Not defined as warnings or ignored

    for (RDAPValidationResult result : allResults) {
        int code = result.getCode();
        if (configurationFile.isError(code)) {
            explicitErrors++;
        } else if (configurationFile.isWarning(code)) {
            explicitWarnings++;
        } else if (configurationFile.getDefinitionIgnore().contains(code)) {
            explicitIgnored++;
        } else {
            implicitErrors++; // Not explicitly categorized
        }
    }

    logger.debug("Explicitly defined errors: {}", explicitErrors);
    logger.debug("Explicitly defined warnings: {}", explicitWarnings);
    logger.debug("Explicitly defined ignored: {}", explicitIgnored);
    logger.debug("Implicit errors (not defined as warnings or ignored): {}", implicitErrors);

    // Print first 10 examples of each category for inspection
    printCategoryExamples("Explicit Errors", allResults.stream()
                                                       .filter(r -> configurationFile.isError(r.getCode()))
                                                       .limit(10)
                                                       .collect(Collectors.toList()));

    printCategoryExamples("Explicit Warnings", allResults.stream()
                                                         .filter(r -> configurationFile.isWarning(r.getCode()))
                                                         .limit(10)
                                                         .collect(Collectors.toList()));

    printCategoryExamples("Explicit Ignored", allResults.stream()
                                                        .filter(r -> configurationFile.getDefinitionIgnore()
                                                                                      .contains(r.getCode()))
                                                        .limit(10)
                                                        .collect(Collectors.toList()));

    printCategoryExamples("Implicit Errors", allResults.stream()
                                                       .filter(r -> !configurationFile.isError(r.getCode())
                                                           && !configurationFile.isWarning(r.getCode())
                                                           && !configurationFile.getDefinitionIgnore()
                                                                                .contains(r.getCode()))
                                                       .limit(10)
                                                       .collect(Collectors.toList()));
}

/**
 * Legacy debug method for backward compatibility.
 * Uses the default session ID.
 *
 * @deprecated Use debugPrintResultBreakdown(String sessionId) instead
 */
@Deprecated
public void debugPrintResultBreakdown() {
    debugPrintResultBreakdown("default");
}

private void printCategoryExamples(String category, List<RDAPValidationResult> examples) {
    logger.debug("{} examples:", category);
    if (examples.isEmpty()) {
        logger.debug("  None found");
    } else {
        for (RDAPValidationResult result : examples) {
            logger.debug("  Code: {}, Value: {}", result.getCode(),
                (result.getValue() != null ?
                    result.getValue().substring(0, Math.min(50, result.getValue().length())) + "..." : "null"));
        }
    }
}
}
