package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class SchemaValidatorTest {

  protected final String schemaName;
  protected final String validJson;
  protected SchemaValidator schemaValidator;
  protected JSONObject jsonObject;
  protected RDAPValidatorResults results;
  protected String name;
  protected String rdapContent;
  protected RDAPDatasetService datasetService;
  protected static final String WRONG_ENUM_VALUE = "wrong enum value";

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
    datasetService = new RDAPDatasetServiceMock();
    datasetService.download(true);
    results = new RDAPValidatorResults();
    schemaValidator = new SchemaValidator(schemaName, results, datasetService);
    rdapContent = getResource(validJson);
    jsonObject = new JSONObject(rdapContent);
    name = schemaValidator.getSchema().getTitle();
  }

  @Test
  public void testValidate_ok() {
    assertThat(schemaValidator.validate(rdapContent)).isTrue();
  }

  protected void invalid(int error) {
    jsonObject.put(name, 0);
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll()).filteredOn(r -> r.getCode() == error)
        .hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("value", "#/"+name+":0")
        .hasFieldOrPropertyWithValue("message",
            "The #/" + name + " structure is not syntactically valid.");
  }

  protected void insertForbiddenKey() {
    JSONObject value = new JSONObject();
    value.put("test", "value");
    jsonObject.getJSONObject(name).put("unknown", List.of(value));
  }

  protected void validate(int errorCode, String value, String msg) {
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll())
        .contains(RDAPValidationResult.builder()
            .code(errorCode)
            .value(value)
            .message(msg)
            .build());
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

  protected void validateIsNotADateTime(int errorCode, String value) {
    validate(errorCode, value, "The JSON value shall be a syntactically valid time and date according to RFC3339.");
  }

  protected void validateIsNotAJsonString(int errorCode, String value) {
    validateBasicType(errorCode, value, "string");
  }

  protected void validateIsNotANumber(int errorCode, String value) {
    validateBasicType(errorCode, value, "number");
  }

  protected void validateIsNotABoolean(int errorCode, String value) {
    validateBasicType(errorCode, value, "boolean");
  }

  private void validateBasicType(int errorCode, String value, String violatedType) {
    validate(errorCode, value, "The JSON value is not a " + violatedType + ".");
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

  protected void validateSubValidation(String validationName, String keyValue, int errorCode) {
    if (!keyValue.contains(":")) {
      jsonObject.put(keyValue, 0);
      keyValue = "#/" + keyValue + ":0";
    }
    this.validateSubValidation(jsonObject.toString(), errorCode, validationName, keyValue);
  }

  protected void validateSubValidation(String invalidJson, int errorCode, String validationName,
      String value) {
    String key = getKey(value);
    assertThat(schemaValidator.validate(invalidJson)).isFalse();
    assertThat(results.getAll()).contains(
        RDAPValidationResult.builder()
            .code(errorCode)
            .value(value)
            .message("The value for the JSON name value does not pass "
                + key + " validation [" + validationName + "].")
            .build()
    );
  }

  protected void testWrongConstant(int errorCode, String field, String goodValue) {
    jsonObject.put(field, "wrong-constant");
    validate(errorCode, "#/" + field + ":wrong-constant", "The JSON value is not " + goodValue + ".");
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
    assertThat(results.getAll())
        .filteredOn(r -> r.getCode() == errorCode && r.getMessage().equals("The " + key + " element does not exist."))
        .hasSize(1);
  }

  protected void stdRdapRolesValidation(int errorCode) {
    validateSubValidation("stdRdapRolesValidation", "roles", errorCode);
  }

  protected void stdRdapPublicIdsValidation(int errorCode) {
    validateSubValidation("stdRdapPublicIdsValidation", "publicIds", errorCode);
  }

  protected void stdRdapEntitiesValidation(int errorCode) {
    validateSubValidation("stdRdapEntitiesValidation", "entities", errorCode);
  }

  protected void stdRdapRemarksValidation(int errorCode) {
    validateSubValidation("stdRdapRemarksValidation", "remarks", errorCode);
  }

  protected void stdRdapLinksValidation(int errorCode) {
    validateSubValidation("stdRdapLinksValidation", "links", errorCode);
  }

  protected void stdRdapEventsValidation(int errorCode) {
    validateSubValidation("stdRdapEventsValidation", "events", errorCode);
  }

  protected void stdRdapAsEventActorValidation(int errorCode) {
    validateSubValidation("stdRdapAsEventActorValidation", "asEventActor", errorCode);
  }

  protected void stdRdapStatusValidation(int errorCode) {
    validateSubValidation("stdRdapStatusValidation", "status", errorCode);
  }

  protected void stdRdapPort43WhoisServerValidation(int errorCode) {
    validateSubValidation("stdRdapPort43WhoisServerValidation", "port43", errorCode);
  }

  protected void stdRdapNoticesRemarksValidation(int errorCode) {
    validateSubValidation("stdRdapNoticesRemarksValidation", "notices", errorCode);
  }

  protected void stdRdapConformanceValidation(int errorCode) {
    validateSubValidation("stdRdapConformanceValidation", "rdapConformance", errorCode);
  }

  protected void stdRdapUnicodeNameValidation(int errorCode) {
    validateSubValidation("stdRdapUnicodeNameValidation", "unicodeName", errorCode);
  }

  protected void stdRdapLdhNameValidation(int errorCode) {
    validateSubValidation("stdRdapLdhNameValidation", "ldhName", errorCode);
  }

  protected String getKey(String value) {
    return value.split(":")[0];
  }
}