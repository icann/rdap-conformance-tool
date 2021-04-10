package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.json.JSONObject;

public abstract class SchemaValidatorForArrayOfStringTest extends SchemaValidatorForArrayTest {

  public SchemaValidatorForArrayOfStringTest(
      String schemaName,
      String validJson) {
    super(schemaName, validJson);
  }

  protected void notListOfString(int errorCode) {
    jsonObject.put(name, List.of(0));
    validateIsNotAJsonString(errorCode, "#/"+name+"/0:0");
  }

  protected void notListOfEnum(int errorCode, String enumType) {
    jsonObject.put(name, List.of("wrong enum value"));
    validateNotEnum(errorCode, enumType,
        "#/"+name+"/0:wrong enum value");
  }
}