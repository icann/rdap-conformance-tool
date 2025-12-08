package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class SchemaValidatorErrorResponseTest extends SchemaValidatorObjectTest {

  public SchemaValidatorErrorResponseTest() {
    super(
        "error",
        "rdap_error.json",
        "/validators/error/valid.json",
        -12100, // 7.2.17.1
        -12101, // 7.2.17.2
        -12102, // 7.2.17.3
        List.of("description", "errorCode", "title"));
    validationName = "stdRdapErrorResponseBodyValidation";
  }

  /**
   * 7.2.17.4
   */
  @Test
  public void errorCodeNotNumber() {
    validateIsNotANumber(-12103, "errorCode");
  }

  /**
   * 7.2.17.5
   */
  @Test
  public void titleNotString() {
    validateIsNotAJsonString(-12104, "title");
  }

  /**
   * Per RFC 9083, only errorCode is required. Title and description are optional.
   * Tests minimal valid error response: {"errorCode": 418}
   */
  @Test
  public void minimalErrorCodeOnly() {
    jsonObject = new JSONObject();
    jsonObject.put("errorCode", 418);
    assertThat(schemaValidator.validate(jsonObject.toString())).isTrue();
    assertThat(results.getGroupOk()).contains(validationName);
  }

  /**
   * Tests error response with errorCode and title, but no description.
   */
  @Test
  public void errorCodeWithTitleOnly() {
    jsonObject = new JSONObject();
    jsonObject.put("errorCode", 404);
    jsonObject.put("title", "Not Found");
    assertThat(schemaValidator.validate(jsonObject.toString())).isTrue();
    assertThat(results.getGroupOk()).contains(validationName);
  }

  /**
   * Tests error response with errorCode and description, but no title.
   */
  @Test
  public void errorCodeWithDescriptionOnly() {
    jsonObject = new JSONObject();
    jsonObject.put("errorCode", 500);
    jsonObject.put("description", new JSONArray().put("Internal error"));
    assertThat(schemaValidator.validate(jsonObject.toString())).isTrue();
    assertThat(results.getGroupOk()).contains(validationName);
  }

  /**
   * Tests that errorCode is still required - validation should fail when missing.
   */
  @Test
  public void missingErrorCodeShouldFail() {
    jsonObject = new JSONObject();
    jsonObject.put("title", "Error");
    jsonObject.put("description", new JSONArray().put("Something went wrong"));
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll())
        .filteredOn(r -> r.getCode() == -12101)
        .hasSize(1);
  }
}
