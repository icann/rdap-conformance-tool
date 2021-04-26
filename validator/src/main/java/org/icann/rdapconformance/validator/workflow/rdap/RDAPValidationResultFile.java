package org.icann.rdapconformance.validator.workflow.rdap;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPValidationResultFile {

  private static final Logger logger = LoggerFactory.getLogger(RDAPValidationResultFile.class);

  private final RDAPValidatorResults results;
  private final RDAPValidatorConfiguration config;
  private final ConfigurationFile configurationFile;
  private final FileSystem fileSystem;

  public RDAPValidationResultFile(RDAPValidatorResults results,
      RDAPValidatorConfiguration config,
      ConfigurationFile configurationFile,
      FileSystem fileSystem) {
    this.results = results;
    this.config = config;
    this.configurationFile = configurationFile;
    this.fileSystem = fileSystem;
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
  public void build(int statusCode) {
    Map<String, Object> fileMap = new HashMap<>();
    fileMap.put("definitionIdentifier", configurationFile.getDefinitionIdentifier());
    fileMap.put("testedURI", config.getUri());
    fileMap.put("testedDate", Instant.now().toString());
    fileMap.put("receivedHttpStatusCode", statusCode);
    fileMap.put("groupOK", this.createGroupOk());
    fileMap.put("groupErrorWarning", new ArrayList<>());
    fileMap.put("results", this.createResultsMap());

    JSONObject object = new JSONObject(fileMap);
    Path path = Paths.get("results", getFilename());
    try {
      fileSystem.mkdir("results");
      fileSystem.write(path.toString(), object.toString(4));
    } catch (IOException e) {
      logger.error("Failed to write results into {}", path.toString(), e);
    }
  }

  Set<String> createGroupOk() {
    return results.getGroupOk();
  }

  private Map<String, Object> createResultsMap() {
    Map<String, Object> resultsMap = new HashMap<>();
    List<Map<String, Object>> errors = new ArrayList<>();
    List<Map<String, Object>> warnings = new ArrayList<>();

    Set<RDAPValidationResult> allResults = results.getAll();

    for (RDAPValidationResult result : allResults) {
      Map<String, Object> resultMap = new HashMap<>();
      resultMap.put("code", result.getCode());
      resultMap.put("value", result.getValue());
      resultMap.put("message", result.getMessage());
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
}
