package org.icann.rdapconformance.validator.schemavalidator;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class SchemaValidatorSecureDnsTest extends SchemaValidatorTest {

  public SchemaValidatorSecureDnsTest() {
    super("rdap_domain.json",
        "/validators/domain/valid.json");
    validationName = "stdRdapSecureDnsValidation";
  }

  @BeforeMethod
  public void setUp() throws IOException {
    Locale.setDefault(Locale.US);
    super.setUp();
    results.clear();
    name = "secureDNS";
  }

  /**
   * 7.2.16.1
   */
  @Test
  public void invalid() {
    invalid(-12000);
  }

  /**
   * 7.2.16.2
   */
  @Test
  public void unauthorizedKey() {
    validateAuthorizedKeys(-12001, List.of("delegationSigned", "dsData", "keyData", "maxSigLife",
        "zoneSigned"
    ));
  }

  /**
   * 7.2.16.4
   */
  @Test
  public void zoneSignedNotBoolean() {
    jsonObject.getJSONObject("secureDNS").put("zoneSigned", 0);
    validateIsNotABoolean(-12003, "#/secureDNS/zoneSigned:0");
  }

  /**
   * 7.2.16.5
   */
  @Test
  public void delegationSignedDoesNotExist() {
    jsonObject.getJSONObject("secureDNS").remove("delegationSigned");
    validateKeyMissing(-12004, "delegationSigned");
  }

  /**
   * 7.2.16.6
   */
  @Test
  public void delegationSignedNotBoolean() {
    jsonObject.getJSONObject("secureDNS").put("delegationSigned", 0);
    validateIsNotABoolean(-12005, "#/secureDNS/delegationSigned:0");
  }

  /**
   * 7.2.16.7
   */
  @Test
  public void maxSigLifeNotNumber() {
    jsonObject.getJSONObject("secureDNS").put("maxSigLife", "not-a-number");
    validate(-12006, "#/secureDNS/maxSigLife:not-a-number", "The JSON value is not a number between 1 and 2,147,483,647.");
  }

  /**
   * 7.2.16.8
   */
  @Test
  public void keyDataAndDsDataMissing() {
    //  Test is inline so that we save the state and don't have the results cleared
    jsonObject.getJSONObject("secureDNS").put("delegationSigned", true);
    jsonObject.getJSONObject("secureDNS").remove("dsData");

    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    // Verify both required validation errors are present
    assertThat(results.getAll())
        .filteredOn(r -> r.getCode() == -12007 && r.getMessage().equals("The dsData element does not exist."))
        .hasSize(1);
    assertThat(results.getAll())
        .filteredOn(r -> r.getCode() == -12007 && r.getMessage().equals("The keyData element does not exist."))
        .hasSize(1);
  }

}
