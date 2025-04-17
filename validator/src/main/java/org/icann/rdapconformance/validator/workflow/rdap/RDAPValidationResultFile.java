package org.icann.rdapconformance.validator.workflow.rdap;

import static org.icann.rdapconformance.validator.exception.parser.ExceptionParser.UNKNOWN_ERROR_CODE;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.icann.rdapconformance.validator.NetworkInfo;
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
  // Private constructor
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
  public boolean build(int statusCode) {
    Map<String, Object> fileMap = new HashMap<>();
    fileMap.put("definitionIdentifier", configurationFile.getDefinitionIdentifier());
    fileMap.put("testedURI", config.getUri());
    fileMap.put("testedDate", Instant.now().toString());
    fileMap.put("receivedHttpStatusCode", statusCode);
    fileMap.put("groupOK", this.results.getGroupOk());
    fileMap.put("groupErrorWarning", this.results.getGroupErrorWarning());
    fileMap.put("results", this.createResultsMap());

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
      resultMap.put("HTTP accept header", result.getAcceptHeader());
      resultMap.put("HTTP method", result.getHttpMethod());
      resultMap.put("Server IP Address", result.getServerIpAddress());
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

  public void mergeResults(RDAPValidatorResults newResults) {
    if (this.results == null) {
      this.results = newResults;
    } else {
      this.results.getAll().addAll(newResults.getAll());
    }
  }
}
