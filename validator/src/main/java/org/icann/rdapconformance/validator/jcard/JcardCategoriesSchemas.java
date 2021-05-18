package org.icann.rdapconformance.validator.jcard;

import java.util.Objects;
import java.util.regex.Pattern;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JcardCategoriesSchemas {

  private final JSONObject jsonSchema;
  private final static Pattern extensionPattern = Pattern.compile("x-[a-z0-9-]*");

  public JcardCategoriesSchemas() {
    jsonSchema = new JSONObject(
        new JSONTokener(
            Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("json-schema/jcard_categories"
                    + ".json")))).getJSONObject("definitions");
  }

  public boolean hasCategory(String category) {
    return jsonSchema.has(category);
  }

  public Schema getCategory(String category) {
    if (!jsonSchema.has(category) && extensionPattern.matcher(category).find()) {
      category = "x-[a-z0-9-]*";
    }

    return SchemaLoader.builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .schemaJson(jsonSchema.getJSONObject(category))
        .resolutionScope("classpath://json-schema/")
        .draftV7Support()
        .build().load().build();
  }
}
