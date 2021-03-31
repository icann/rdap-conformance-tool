package org.icann.rdapconformance.validator.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConfigurationFileParser {


  public ConfigurationFileParser() {
  }

  public ConfigurationFile parse(File configuration) throws IOException {
    StringBuilder jsonConfigStr = new StringBuilder();
    try (InputStream fis = new FileInputStream(configuration);
        Reader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr)) {
      String line;
      while ((line = br.readLine()) != null) {
        jsonConfigStr.append(line).append("\n");
      }
    }
    JSONObject jsonConfig = new JSONObject(jsonConfigStr.toString());
    return new ConfigurationFile.Builder()
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
      definitionAlerts.add(new DefinitionAlerts.Builder()
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
