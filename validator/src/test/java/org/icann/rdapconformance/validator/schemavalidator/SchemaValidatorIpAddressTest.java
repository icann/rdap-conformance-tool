package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class SchemaValidatorIpAddressTest extends SchemaValidatorTest {

  public SchemaValidatorIpAddressTest() {
    super(
        "test_rdap_ipAddress.json",
        "/validators/ipAddress/valid.json");
    validationName = "stdRdapIpAddressesValidation";
  }

  /**
   * 7.2.10.1
   */
  @Test
  public void invalid() {
    invalid(-11400);
  }

  /**
   * 7.2.10.2
   */
  @Test
  public void unauthorizedKey() {
    validateAuthorizedKeys(-11401, List.of("v4", "v6"));
  }

  /**
   * 7.2.10.4
   */
  @Test
  public void v4NOrv6() {
    // no v4 nor v6:
    jsonObject.put("ipAddress", new JSONObject());
    schemaValidator.validate(jsonObject.toString());
    assertThat(results.getAll())
        .contains(
            RDAPValidationResult.builder()
                .code(-11403)
                .value("{\"ipAddress\":{}}")
                .message("The v4 element does not exist.")
                .build(queryContext),
            RDAPValidationResult.builder()
                .code(-11403)
                .value("{\"ipAddress\":{}}")
                .message("The v6 element does not exist.")
                .build(queryContext)
        );
  }

  /**
   * 7.2.10.5.1
   */
  @Test
  public void v4Invalid() {
    jsonObject.getJSONObject("ipAddress").put("v4", List.of("0.0.0.wrong-ipv4"));
    validate(-10100, "#/ipAddress/v4/0:0.0.0.wrong-ipv4",
        "The IPv4 address is not syntactically valid in dot-decimal notation.");
  }

  /**
   * 7.2.10.5.2.1
   */
  @Test
  public void v4NotJsonString() {
    jsonObject.getJSONObject("ipAddress").put("v4", List.of(0));
    validateIsNotAJsonString(-11405, "#/ipAddress/v4/0:0");
  }

  /**
   * 7.2.10.5.2.2
   */
  @Test
  public void v4NotDotDecimal() {
    jsonObject.getJSONObject("ipAddress").put("v4", List.of("999"));
    validate(-10100, "#/ipAddress/v4/0:999",
        "The IPv4 address is not syntactically valid in dot-decimal notation.");
  }

  /**
   * 7.2.10.6.1
   */
  @Test
  public void v6Invalid() {
    jsonObject.getJSONObject("ipAddress").remove("v4");
    jsonObject.getJSONObject("ipAddress").put("v6", List.of("0:0:0:wrong-ipv6"));
    validateWithoutGroupTests(-10200, "#/ipAddress/v6/0:0:0:0:wrong-ipv6",
        "The IPv6 address is not syntactically valid.");
  }

  /**
   * 7.2.10.5.6.1
   */
  @Test
  public void v6NotJsonString() {
    jsonObject.getJSONObject("ipAddress").remove("v4");
    jsonObject.getJSONObject("ipAddress").put("v6", List.of(0));
    validateIsNotAJsonString(-11408, "#/ipAddress/v6/0:0");
  }

  /**
   * 7.2.10.5.6.2
   */
  @Test
  public void v6NotSyntacticallyValid() {
    jsonObject.getJSONObject("ipAddress").remove("v4");
    jsonObject.getJSONObject("ipAddress").put("v6", List.of("999"));
    validateWithoutGroupTests(-10200, "#/ipAddress/v6/0:999",
        "The IPv6 address is not syntactically valid.");
  }
}
