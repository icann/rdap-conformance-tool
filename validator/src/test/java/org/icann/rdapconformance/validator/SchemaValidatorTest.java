package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class SchemaValidatorTest {

  protected RDAPValidatorTestContext context;
  protected SchemaValidator schemaValidator;
  String rdapContent;
  protected JSONObject jsonObject;

  public SchemaValidatorTest(
      String schemaName,
      String validJson) {
    this.schemaName = schemaName;
    this.validJson = validJson;
  }

  String schemaName;
  String validJson;

  @BeforeMethod
  public void setUp() throws IOException {
    context = new RDAPValidatorTestContext(new ConfigurationFile.Builder().build());
    schemaValidator = new SchemaValidator(schemaName, context);
    rdapContent = context.getResource(validJson);
    jsonObject = new JSONObject(rdapContent);
  }

  @Test
  public void testValidate_ok() {
    assertThat(schemaValidator.validate(rdapContent)).isTrue();
  }

  protected void validateIsNotAJsonString(int errorCode, String value) {
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(context.getResults())
        .filteredOn("code", errorCode)
        .last()
        .hasFieldOrPropertyWithValue("value", value)
        .hasFieldOrPropertyWithValue("message", "The JSON value is not a string.");
  }


  protected void validateInvalidJson(int error, String value) {
    String key = value.split(":")[0];
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(context.getResults()).filteredOn(r -> r.getCode() == error)
        .hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("value", value)
        .hasFieldOrPropertyWithValue("message",
            "The " + key + " structure is not syntactically valid.");
  }

  protected void validateSubValidation(ComplexValidation complexValidation) {
    jsonObject.put(complexValidation.validatedField, 0);
    validateSubValidation(
        complexValidation.errorCode,
        complexValidation.validationName,
        "#/" + complexValidation.validatedField + ":0");
  }

  protected void validateSubValidation(int errorCode, String validationName,
      String value) {
    this.validateSubValidation(jsonObject.toString(), errorCode, validationName, value);
  }

  protected void validateSubValidation(String invalidJson, int errorCode, String validationName,
      String value) {
    String key = value.split(":")[0];
    assertThat(schemaValidator.validate(invalidJson)).isFalse();
    assertThat(context.getResults()).filteredOn("code", errorCode)
        .first()
        .hasFieldOrPropertyWithValue("value", value)
        .hasFieldOrPropertyWithValue("message",
            "The value for the JSON name value does not pass "
                + key + " validation [" + validationName + "].");
  }

  protected void testWrongConstant(String field, String goodValue, int errorCode) {
    jsonObject.put(field, "wrong-constant");
    schemaValidator.validate(jsonObject.toString());
    assertThat(context.getResults()).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", errorCode)
        .hasFieldOrPropertyWithValue("value", "#/" + field + ":wrong-constant")
        .hasFieldOrPropertyWithValue("message",
            "The JSON value is not " + goodValue + ".");
  }

  protected void validateNotEnum(int errorCode, String enumType, String value) {
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(context.getResults())
        .filteredOn("code", errorCode)
        .first()
        .hasFieldOrPropertyWithValue("value", value)
        .matches(r -> r.getMessage().startsWith("The JSON string is not included as a Value with "
            + "Type=\"" + enumType + "\" dataset"));
  }

  protected void validateKeyMissing(String key, int errorCode) {
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(context.getResults()).filteredOn(r -> r.getCode() == errorCode)
        .hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("message",
            "The " + key + " element does not exist.");
  }
}