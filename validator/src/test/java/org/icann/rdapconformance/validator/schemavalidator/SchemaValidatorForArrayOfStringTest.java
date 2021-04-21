package org.icann.rdapconformance.validator.schemavalidator;

import java.util.List;

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

  protected String wrongEnum() {
    jsonObject.put(name, List.of(WRONG_ENUM_VALUE));
    return "#/"+name+"/0:"+WRONG_ENUM_VALUE;
  }

  protected void notListOfEnum(int errorCode, String enumType) {
    validateNotEnum(errorCode, enumType, wrongEnum());
  }

  protected void notListOfEnumDataset(int errorCode, String msg) {
    validate(errorCode, wrongEnum(), msg);
  }
}