package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.jknack.handlebars.internal.text.WordUtils;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class SchemaValidatorTest {
  private final String schemaName;
  private final String validJson;
  protected SchemaValidator schemaValidator;
  protected JSONObject jsonObject;
  protected RDAPValidatorResults results;
  private String name;
  private String rdapContent;

  public SchemaValidatorTest(
      String schemaName,
      String validJson) {
    this.schemaName = schemaName;
    this.validJson = validJson;
  }

  public static String getResource(String path) throws IOException {
    URL jsonUri = SchemaValidatorTest.class.getResource(path);
    assert null != jsonUri;
    try (InputStream is = SchemaValidatorTest.class.getResourceAsStream(path)) {
      assert null != is;
      try (InputStreamReader isr = new InputStreamReader(is);
          BufferedReader reader = new BufferedReader(isr)) {
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
      }
    }
  }

  @BeforeMethod
  public void setUp() throws IOException {
    results = new RDAPValidatorResults();
    schemaValidator = new SchemaValidator(schemaName, results);
    rdapContent = getResource(validJson);
    jsonObject = new JSONObject(rdapContent);
    name = schemaValidator.getSchema().getTitle();
  }

  @Test
  public void testValidate_ok() {
    assertThat(schemaValidator.validate(rdapContent)).isTrue();
  }

  public void keyDoesNotExistInArray(String key, int errorCode) {
    jsonObject.getJSONArray(name).getJSONObject(0).remove(key);
    validateKeyMissing(errorCode, key);
  }

  protected void replaceArrayProperty(String key, Object value) {
    jsonObject.put(name, List.of(jsonObject.getJSONArray(name).getJSONObject(0).put(key,
        value)));
  }

  protected void insertForbiddenKey() {
    JSONObject value = new JSONObject();
    value.put("test", "value");
    jsonObject.put("unknown", List.of(value));
  }

  protected void validateArrayAuthorizedKeys(int error, List<String> authorizedKeys) {
    JSONObject value = new JSONObject();
    value.put("test", "value");
    jsonObject.getJSONArray(name).getJSONObject(0).put("unknown", List.of(value));
    validateAuthorizedKeys(error, authorizedKeys);
  }

  protected void validateAuthorizedKeys(int errorCode, List<String> authorizedKeys) {
    insertForbiddenKey();
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll())
        .filteredOn("code", errorCode)
        .first()
        .matches(r -> r.getValue().endsWith("/unknown:[{\"test\":\"value\"}]"))
        .hasFieldOrPropertyWithValue("message",
            "The name in the name/value pair is not of: " + String.join(", ", authorizedKeys) +
                ".");
  }

  protected void validateRegex(int errorCode, String regexType, String value) {
    String key = getKey(value);
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll())
        .filteredOn("code", errorCode)
        .last()
        .hasFieldOrPropertyWithValue("value", value)
        .hasFieldOrPropertyWithValue("message",
            "The value of the JSON string data in the " + key + " does not conform to "
                + regexType + " syntax.");
  }

  protected void arrayItemKeyIsNotDateTime(String key, int errorCode) {
    replaceArrayProperty(key, "not a date-time");
    validateIsNotADateTime(errorCode, "#/" + name + "/0/" + key + ":not a date-time");
  }

  protected void validateIsNotADateTime(int errorCode, String value) {
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll())
        .filteredOn("code", errorCode)
        .last()
        .hasFieldOrPropertyWithValue("value", value)
        .hasFieldOrPropertyWithValue("message",
            "The JSON value shall be a syntactically valid time and date according to RFC3339.");
  }

  protected void arrayItemKeyIsNotString(String key, int errorCode) {
    replaceArrayProperty(key, 0);
    validateIsNotAJsonString(errorCode, "#/" + name + "/0/" + key + ":0");
  }

  protected void validateIsNotAJsonString(int errorCode, String value) {
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll())
        .filteredOn("code", errorCode)
        .last()
        .hasFieldOrPropertyWithValue("value", value)
        .hasFieldOrPropertyWithValue("message", "The JSON value is not a string.");
  }

  protected void linksViolatesLinksValidation(int errorCode) {
    arrayItemKeySubValidation("links", "stdRdapLinksValidation", errorCode);
  }

  protected void arrayItemKeySubValidation(String key, String validationName, int errorCode) {
    replaceArrayProperty(key, 0);
    validateSubValidation(errorCode, validationName, "#/" + name + "/0/" + key + ":0");
  }

  protected void arrayInvalid(int error) {
    jsonObject.put(name, 0);
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll()).filteredOn(r -> r.getCode() == error)
        .hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("value", "#/"+name+":0")
        .hasFieldOrPropertyWithValue("message",
            "The #/" + name + " structure is not syntactically valid.");
  }

  protected void validateInvalidJson(int error, String value) {
    String key = getKey(value);
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll()).filteredOn(r -> r.getCode() == error)
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
    String key = getKey(value);
    assertThat(schemaValidator.validate(invalidJson)).isFalse();
    assertThat(results.getAll()).filteredOn("code", errorCode)
        .first()
        .hasFieldOrPropertyWithValue("value", value)
        .hasFieldOrPropertyWithValue("message",
            "The value for the JSON name value does not pass "
                + key + " validation [" + validationName + "].");
  }

  protected void testWrongConstant(int errorCode, String field, String goodValue) {
    jsonObject.put(field, "wrong-constant");
    schemaValidator.validate(jsonObject.toString());
    assertThat(results.getAll())
        .filteredOn("code", errorCode)
        .first()
        .hasFieldOrPropertyWithValue("value", "#/" + field + ":wrong-constant")
        .hasFieldOrPropertyWithValue("message",
            "The JSON value is not " + goodValue + ".");
  }

  protected void validateNotEnum(int errorCode, String enumType, String value) {
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll())
        .filteredOn("code", errorCode)
        .first()
        .hasFieldOrPropertyWithValue("value", value)
        .matches(r -> r.getMessage().startsWith("The JSON string is not included as a Value with "
            + "Type=\"" + enumType + "\" dataset"));
  }

  protected void validateKeyMissing(int errorCode, String key) {
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll()).filteredOn(r -> r.getCode() == errorCode)
        .hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("message",
            "The " + key + " element does not exist.");
  }

  private String getKey(String value) {
    return value.split(":")[0];
  }
}