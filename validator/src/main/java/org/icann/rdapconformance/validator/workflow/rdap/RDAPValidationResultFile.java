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

import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.icann.rdapconformance.validator.BuildInfo;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.icann.rdapconformance.validator.QueryContext;

/**
 * Instance-based service for managing RDAP validation results and generating formatted output files.
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
 * <p>This class is now instance-based and managed through QueryContext to ensure
 * thread safety and isolation between concurrent validations. Each QueryContext
 * maintains its own instance, eliminating concurrency issues.</p>
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
 * QueryContext qctx = QueryContext.create(config, datasetService, query);
 * RDAPValidationResultFile resultFile = qctx.getResultFile();
 * resultFile.initialize(qctx.getResults(), config, configFile, fileSystem);
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

    private RDAPValidatorResults results;
    private RDAPValidatorConfiguration config;
    private ConfigurationFile configurationFile;
    private FileSystem fileSystem;
    public String resultPath;

    // Track if already initialized
    private boolean isInitialized = false;

    // QueryContext for result building - set during initialization
    private QueryContext queryContext;

    /**
     * Public constructor for instance-based usage through QueryContext.
     */
    public RDAPValidationResultFile() {
    }

    /**
     * Initializes the result file manager with required dependencies.
     *
     * <p>This method must be called once before using other methods. It sets up
     * the necessary dependencies for result processing and file generation.
     * Subsequent calls to this method are ignored to prevent reinitialization.</p>
     *
     * @param results the validation results collection
     * @param config the validator configuration
     * @param configurationFile the configuration file with validation rules
     * @param fileSystem the file system abstraction for file operations
     * @param queryContext the query context for building validation results
     */
    public void initialize(RDAPValidatorResults results,
                           RDAPValidatorConfiguration config,
                           ConfigurationFile configurationFile,
                           FileSystem fileSystem,
                           QueryContext queryContext) {
        if (isInitialized) {
            return;
        }
        this.isInitialized = true;
        this.results = results;
        this.config = config;
        this.configurationFile = configurationFile;
        this.fileSystem = fileSystem;
        this.queryContext = queryContext;
    }

    /**
     * Legacy initialize method for backward compatibility with tests.
     *
     * @deprecated Use initialize(results, config, configurationFile, fileSystem, queryContext) instead
     */
    @Deprecated
    public void initialize(RDAPValidatorResults results,
                           RDAPValidatorConfiguration config,
                           ConfigurationFile configurationFile,
                           FileSystem fileSystem) {
        initialize(results, config, configurationFile, fileSystem, null);
    }


    private static String getFilename() {
        String datetimePattern = "yyyyMMddHHmmss";
        String dateTime = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(datetimePattern));
        return String.format("results-%s.json", dateTime);
    }

    /**
     * Builds and saves the validation results to a JSON file.
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
     * @return true if the result file was successfully created and written, false otherwise
     */
    public boolean build() {
        Map<String, Object> fileMap = new HashMap<>();
        fileMap.put("definitionIdentifier", configurationFile.getDefinitionIdentifier());
        fileMap.put("testedURI", config.getUri());
        fileMap.put("testedDate", Instant.now().toString());
        fileMap.put("groupOK", this.results.getGroupOk());
        fileMap.put("groupErrorWarning", this.results.getGroupErrorWarning());
        fileMap.put("results", this.createResultsMap());
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
            if (resultsFilePath == null || resultsFilePath.isEmpty()) {
                fileSystem.mkdir("results");
            }
            this.resultPath = path.toString();
            fileSystem.write(path.toString(), object.toString(4));
            return true;
        } catch (IOException e) {
            logger.debug("Failed to write results into {}", path, e);
            return false;
        }
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
     * Creates a structured map of validation results categorized by type.
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
     * @return structured map containing categorized validation results
     */
    public Map<String, Object> createResultsMap() {
        Map<String, Object> resultsMap = new HashMap<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        List<Map<String, Object>> warnings = new ArrayList<>();

        Set<RDAPValidationResult> allResults = results.getAll();
        Set<Integer> codeToIgnore = new HashSet<>(configurationFile.getDefinitionIgnore());
        Set<RDAPValidationResult> filteredResults = results.getAll()
                                                           .stream()
                                                           .filter(r -> !codeToIgnore.contains(r.getCode())
                                                               && r.getCode() != UNKNOWN_ERROR_CODE)
                                                           .collect(Collectors.toSet());

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
                                                   .build(queryContext));
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
     * Returns the total count of validation errors.
     *
     * <p>This method counts all results that are classified as errors based on
     * the configuration file definitions. This includes explicitly defined errors
     * and results that are not categorized as warnings or ignored.</p>
     *
     * @return the number of validation errors found
     */
    public int getErrorCount() {
        return getErrors().size();
    }

    /**
     * Returns a list of all validation results classified as errors.
     *
     * <p>This method filters the complete result set to return only those results
     * that are considered errors based on the configuration file definitions.
     * Results are classified as errors if they are explicitly defined as errors
     * or if they are not categorized as warnings or ignored codes.</p>
     *
     * @return list of validation results that are classified as errors
     */
    public List<RDAPValidationResult> getErrors() {
        return this.results.getAll()
                           .stream()
                           .filter(result -> this.configurationFile.isError(result.getCode()) || (
                               !this.configurationFile.isWarning(result.getCode())
                                   && !this.configurationFile.getDefinitionIgnore().contains(result.getCode())))
                           .collect(Collectors.toList());
    }

    /**
     * Removes error results from the result set, keeping only warnings and response format errors.
     *
     * <p>This method filters the results to retain only warnings and specific response
     * format validation errors that should be preserved regardless of the overall
     * response status. This is typically used when processing 404 responses where
     * most errors are expected but response format validation should still apply.</p>
     */
    public void removeErrors() {
        Set<RDAPValidationResult> filteredResults = this.results.getAll()
                .stream()
                .filter(result -> this.configurationFile.isWarning(result.getCode())
                                  || isResponseFormatError(result.getCode())
                                  || isDomainValidationError(result.getCode()))
                .collect(Collectors.toSet());

        this.results.addAll(filteredResults);
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
     * Removes result groups from the validation results.
     *
     * <p>This method clears any grouped results from the result set, typically
     * used to reset or clean up the results collection during processing.</p>
     */
    public void removeResultGroups() {
        this.results.removeGroups();
    }

    /**
     * Returns a list of all validation results.
     *
     * <p>This method provides access to the complete set of validation results
     * without any filtering or categorization. The returned list is a copy
     * to prevent external modification of the internal result set.</p>
     *
     * @return list containing all validation results
     */
    public List<RDAPValidationResult> getAllResults() {
        return new ArrayList<>(this.results.getAll());
    }

    /**
     * Returns the file path where the validation results were written.
     *
     * <p>This method provides access to the actual file path used for the
     * generated results file. The path is set during the build process and
     * reflects either the configured output path or the automatically
     * generated timestamped filename.</p>
     *
     * @return the file path of the generated results file, or null if not yet built
     */
    public String getResultsPath() {
        return resultPath;
    }

// manual debug usage only -- unused right now
public void debugPrintResultBreakdown() {
    Set<RDAPValidationResult> allResults = this.results.getAll();
    List<RDAPValidationResult> errors = getErrors();

    logger.debug("Total Results: {}", allResults.size());
    logger.debug("Error Results: {}", errors.size());

    // Count results by category
    int explicitErrors = ZERO;
    int explicitWarnings = ZERO;
    int explicitIgnored = ZERO;
    int implicitErrors = ZERO; // Not defined as warnings or ignored

    for (RDAPValidationResult result : allResults) {
        int code = result.getCode();
        if (this.configurationFile.isError(code)) {
            explicitErrors++;
        } else if (this.configurationFile.isWarning(code)) {
            explicitWarnings++;
        } else if (this.configurationFile.getDefinitionIgnore().contains(code)) {
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
                                                       .filter(r -> this.configurationFile.isError(r.getCode()))
                                                       .limit(10)
                                                       .collect(Collectors.toList()));

    printCategoryExamples("Explicit Warnings", allResults.stream()
                                                         .filter(r -> this.configurationFile.isWarning(r.getCode()))
                                                         .limit(10)
                                                         .collect(Collectors.toList()));

    printCategoryExamples("Explicit Ignored", allResults.stream()
                                                        .filter(r -> this.configurationFile.getDefinitionIgnore()
                                                                                           .contains(r.getCode()))
                                                        .limit(10)
                                                        .collect(Collectors.toList()));

    printCategoryExamples("Implicit Errors", allResults.stream()
                                                       .filter(r -> !this.configurationFile.isError(r.getCode())
                                                           && !this.configurationFile.isWarning(r.getCode())
                                                           && !this.configurationFile.getDefinitionIgnore()
                                                                                     .contains(r.getCode()))
                                                       .limit(10)
                                                       .collect(Collectors.toList()));
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
