package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.testng.annotations.Test;

public class SchemaValidatorPort43Test extends SchemaValidatorTest {

  public SchemaValidatorPort43Test() {
    super(
        "test_rdap_port43.json",
        "/validators/port43/valid.json");
  }

  /**
   * 7.2.7.1.
   */
  @Test
  public void stdRdapPort43WhoisServerAllValidations() {
    jsonObject.put(name, "0.0.0-wrong");
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll())
        .contains(
            RDAPValidationResult.builder()
                .code(-11100)
                .value("#/port43:0.0.0-wrong")
                .message("The value for the JSON name value does not pass #/port43 validation [IPv4Validation].")
                .build(),
            RDAPValidationResult.builder()
                .code(-11100)
                .value("#/port43:0.0.0-wrong")
                .message("The value for the JSON name value does not pass #/port43 validation [IPv6Validation].")
                .build(),
            RDAPValidationResult.builder()
                .code(-11100)
                .value("#/port43:0.0.0-wrong")
                .message("The value for the JSON name value does not pass #/port43 validation [DomainNameValidation].")
                .build()
        );
  }
}
