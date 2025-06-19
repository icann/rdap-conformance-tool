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

public class RDAPValidationResultFile {
  private static final Logger logger = LoggerFactory.getLogger(RDAPValidationResultFile.class);

  // Singleton instance
  private static RDAPValidationResultFile instance;

  private RDAPValidatorResults results;
  private RDAPValidatorConfiguration config;
  private ConfigurationFile configurationFile;
  private FileSystem fileSystem;
  public String resultPath;

  // Track if already initialized
  private boolean isInitialized = false;
  private RDAPValidationResultFile() {}

  public static synchronized RDAPValidationResultFile getInstance() {
    if (instance == null) {
      instance = new RDAPValidationResultFile();
    }
    return instance;
  }

  public void initialize(RDAPValidatorResults results,
                         RDAPValidatorConfiguration config,
                         ConfigurationFile configurationFile,
                         FileSystem fileSystem) {
    if(isInitialized) {
      return;
    }
    this.isInitialized = true;
    this.results = results;
    this.config = config;
    this.configurationFile = configurationFile;
    this.fileSystem = fileSystem;
  }

  // For testing purposes
  public static void reset() {
    instance = null;
  }
  private static String getFilename() {
    String datetimePattern = "yyyyMMddHHmmss";
    String dateTime = OffsetDateTime.now(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern(datetimePattern));
    return String.format("results-%s.json", dateTime);
  }

  /**
   * Fill and save the result file.
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
      logger.info("Failed to write results into {}", path, e);
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

  public Map<String, Object> createResultsMap() {
    Map<String, Object> resultsMap = new HashMap<>();
    List<Map<String, Object>> errors = new ArrayList<>();
    List<Map<String, Object>> warnings = new ArrayList<>();

    Set<RDAPValidationResult> allResults = results.getAll();

    Set<Integer> codeToIgnore = new HashSet<>(configurationFile.getDefinitionIgnore());
    Set<RDAPValidationResult> filteredResults = results.getAll().stream()
                                                       .filter(r -> !codeToIgnore.contains(r.getCode()) && r.getCode() != UNKNOWN_ERROR_CODE)
                                                       .collect(Collectors.toSet());

    filteredResults = cullDuplicateIPAddressErrors(filteredResults);
    filteredResults = analyzeResultsWithStatusCheck(filteredResults);

    //  Finally build the resultMap
    for (RDAPValidationResult result : filteredResults) {
      Map<String, Object> resultMap = new HashMap<>();
      resultMap.put("code", result.getCode());
      resultMap.put("value", result.getValue());
      resultMap.put("message", result.getMessage());
      resultMap.put("receivedHttpStatusCode", formatStatusCode(result.getHttpStatusCode()));
      resultMap.put("queriedURI",
          Objects.nonNull(result.getQueriedURI()) ? formatStringToNull(result.getQueriedURI()) :
              (Objects.nonNull(config.getUri()) ? config.getUri().toString() : StringUtils.EMPTY));
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

  public Set<RDAPValidationResult> analyzeResultsWithStatusCheck(Set<RDAPValidationResult> allResults) {
    List<RDAPValidationResult> filtered = new ArrayList<>();
    for (RDAPValidationResult result : allResults) {
      int code = result.getCode();
      // Filter out these codes
      //  -13004 (Blind Copy Queries), -13005 (Redirect to itself), -13006 (test.Invalid), -46701 (Invalid Domain Query was not a 404)
      if (code != -13004 && code != -13005 && code != -13006 && code != -46701) {
        filtered.add(result);
      }
    }

    // Create unique tuples of (code, httpStatusCode)
    Set<List<Object>> uniqueTuples = new HashSet<>();
    for (RDAPValidationResult result : filtered) {
      List<Object> tuple = new ArrayList<>();
      tuple.add(result.getCode());
      Integer status = result.getHttpStatusCode();
      tuple.add((status != null && status == ZERO) ? null : status);
      uniqueTuples.add(tuple);
    }

    // Collect httpStatusCodes - normalize null to 0
    Set<Integer> statusCodes = new HashSet<>();
    for (RDAPValidationResult result : filtered) {
      Integer statusCode = result.getHttpStatusCode();
      statusCodes.add(statusCode == null ? ZERO : statusCode);
    }

    // make sure we grab the filtered set
    Set<RDAPValidationResult> updatedResults = new HashSet<>(filtered);

    // If not all the same, add the new error code
    if (statusCodes.size() > ONE) {
      logger.info("Not all status codes are the same");
      String tupleListJson = "[]";
      try {
        ObjectMapper mapper = new ObjectMapper();
        tupleListJson = mapper.writeValueAsString(new ArrayList<>(uniqueTuples));
      } catch (JsonProcessingException e) {
        logger.info("Error serializing tuple list to JSON", e);
      }
      updatedResults.add(
          RDAPValidationResult.builder()
                              .acceptHeader(DASH)
                              .queriedURI(DASH)
                              .httpMethod(DASH)
                              .httpStatusCode(ZERO)
                              .code(-13018)
                              .value(tupleListJson)
                              .message("Queries do not produce the same HTTP status code.")
                              .build()
      );
    }  else {
      logger.info("All status codes are the same");
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

  public String getResultsPath() {
    return resultPath;
  }
}
