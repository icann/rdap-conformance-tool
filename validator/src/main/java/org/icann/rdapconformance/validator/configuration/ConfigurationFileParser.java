package org.icann.rdapconformance.validator.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConfigurationFileParser {

  private final FileSystem fileSystem;

  public ConfigurationFileParser(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  public ConfigurationFile parse(File configuration) throws IOException {
    String jsonConfigStr = fileSystem.readFile(configuration.getAbsolutePath());
    JSONObject jsonConfig = new JSONObject(jsonConfigStr);
    return ConfigurationFile.builder()
        .definitionIdentifier(jsonConfig.getString("definitionIdentifier"))
        .definitionError(optJsonArrayToDefinitionAlerts(jsonConfig.optJSONArray("definitionError")))
        .definitionWarning(
            optJsonArrayToDefinitionAlerts(jsonConfig.optJSONArray("definitionWarning")))
        .definitionIgnore(optJsonArrayToInt(jsonConfig.optJSONArray("definitionIgnore")))
        .definitionNotes(optJsonArrayToStr(jsonConfig.optJSONArray("definitionNotes")))
        .build();
  }

  private List<DefinitionAlerts> optJsonArrayToDefinitionAlerts(JSONArray jsonArray) {
    if (null == jsonArray) {
      return Collections.emptyList();
    }

    Iterator<Object> it = jsonArray.iterator();
    List<DefinitionAlerts> definitionAlerts = new ArrayList<>();
    while (it.hasNext()) {
      JSONObject obj = (JSONObject) it.next();
      definitionAlerts.add(DefinitionAlerts.builder()
          .code(obj.getInt("code"))
          .notes(obj.optString("notes"))
          .build());
    }
    return definitionAlerts;
  }

  private List<Integer> optJsonArrayToInt(JSONArray jsonArray) {
    if (null == jsonArray) {
      return Collections.emptyList();
    }
    return jsonArray.toList().stream()
        .map(o -> (int) o)
        .collect(Collectors.toList());
  }

  private List<String> optJsonArrayToStr(JSONArray jsonArray) {
    if (null == jsonArray) {
      return Collections.emptyList();
    }
    return jsonArray.toList().stream()
        .map(String::valueOf)
        .collect(Collectors.toList());
  }
}
