package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.testng.annotations.Test;

public class SchemaValidatorIpAddressTest extends SchemaValidatorTest {

  public SchemaValidatorIpAddressTest() {
    super(
        "test_rdap_ipAddress.json",
        "/validators/ipAddress/valid.json");
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
   * 7.2.10.4 == 7.2.10.2
   */
  @Test
  public void v4Orv6NotBoth() {
    // add a v6 address to the jsonObject with v4 already:
    jsonObject.getJSONObject("ipAddress").put("v6", "2001:db8:0000:1:1:1:1:1");
    // validate...
    schemaValidator.validate(jsonObject.toString());
    assertThat(results.getAll())
        .contains(
            RDAPValidationResult.builder()
                .code(-11401)
                .value("#/ipAddress/v6:2001:db8:0000:1:1:1:1:1")
                .message("The name in the name/value pair is not of: v4.")
                .build(),
            RDAPValidationResult.builder()
                .code(-11401)
                .value("#/ipAddress/v4:172.16.254.1")
                .message("The name in the name/value pair is not of: v6.")
                .build()
        );
  }

  /**
   * 7.2.10.5.1
   */
  @Test
  public void v4Invalid() {
    jsonObject.getJSONObject("ipAddress").put("v4", "0.0.0.wrong-ipv4");
    validate(-11404, "#/ipAddress/v4:0.0.0.wrong-ipv4",
        "The v4 structure is not syntactically valid.");
  }

  /**
   * 7.2.10.5.2.1
   */
  @Test
  public void v4NotJsonString() {
    jsonObject.getJSONObject("ipAddress").put("v4", 0);
    validateIsNotAJsonString(-11405, "#/ipAddress/v4:0");
  }

  /**
   * 7.2.10.5.2.2
   */
  @Test
  public void v4NotDotDecimal() {
    jsonObject.getJSONObject("ipAddress").put("v4", "999");
    validate(-11406, "#/ipAddress/v4:999",
        "The IPv4 address is not syntactically valid in dot-decimal notation.");
  }

  /**
   * 7.2.10.6.1
   */
  @Test
  public void v6Invalid() {
    jsonObject.getJSONObject("ipAddress").remove("v4");
    jsonObject.getJSONObject("ipAddress").put("v6", "0:0:0:wrong-ipv6");
    validate(-11407, "#/ipAddress/v6:0:0:0:wrong-ipv6",
        "The v6 structure is not syntactically valid.");
  }

  /**
   * 7.2.10.5.6.1
   */
  @Test
  public void v6NotJsonString() {
    jsonObject.getJSONObject("ipAddress").remove("v4");
    jsonObject.getJSONObject("ipAddress").put("v6", 0);
    validateIsNotAJsonString(-11408, "#/ipAddress/v6:0");
  }

  /**
   * 7.2.10.5.6.2
   */
  @Test
  public void v6NotSyntacticallyValid() {
    jsonObject.getJSONObject("ipAddress").remove("v4");
    jsonObject.getJSONObject("ipAddress").put("v6", "999");
    validate(-11409, "#/ipAddress/v6:999",
        "The IPv6 address is not syntactically valid.");
  }
}
