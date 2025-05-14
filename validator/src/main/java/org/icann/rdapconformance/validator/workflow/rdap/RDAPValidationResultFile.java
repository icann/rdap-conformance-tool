package org.icann.rdapconformance.validator.workflow.rdap;

import static org.icann.rdapconformance.validator.exception.parser.ExceptionParser.UNKNOWN_ERROR_CODE;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
  public boolean build(int exitCode) {
    // TODO: for the moment we do nothing with the exitCode
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
      logger.error("Failed to write results into {}", path, e);
      return false;
    }
  }

  private Object formatStatusCode(Integer statusCode) {
    return statusCode != null && statusCode == 0 ? JSONObject.NULL : statusCode;
  }

  private Map<String, Object> createResultsMap() {
    Map<String, Object> resultsMap = new HashMap<>();
    List<Map<String, Object>> errors = new ArrayList<>();
    List<Map<String, Object>> warnings = new ArrayList<>();

    Set<RDAPValidationResult> allResults = results.getAll();

    Set<Integer> codeToIgnore = new HashSet<>(configurationFile.getDefinitionIgnore());
    for (RDAPValidationResult result : allResults) {
      if (codeToIgnore.contains(result.getCode()) || result.getCode() == UNKNOWN_ERROR_CODE) {
        continue;
      }

      Map<String, Object> resultMap = new HashMap<>();
      resultMap.put("code", result.getCode());
      resultMap.put("value", result.getValue());
      resultMap.put("message", result.getMessage());
      resultMap.put("receivedHttpStatusCode", formatStatusCode(result.getHttpStatusCode()));
      resultMap.put("queriedURI",
          Objects.nonNull(result.getQueriedURI()) ? result.getQueriedURI() :
              (Objects.nonNull(config.getUri()) ? config.getUri().toString() : StringUtils.EMPTY));
      resultMap.put("acceptMediaType", result.getAcceptHeader());
      resultMap.put("httpMethod", result.getHttpMethod());
      resultMap.put("serverIpAddress", result.getServerIpAddress());
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

  public String getResultsPath() {
    return resultPath;
  }
}
