package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;

public class TigValidationTestBase extends SchemaValidatorTest {

  protected final String validationName;

  public TigValidationTestBase(
      String schemaName,
      String validJson,
      String validationName) {
    super(schemaName, validJson);
    this.validationName = validationName;
  }

  public void testValidate_ok(TigValidation validation) {
    assertThat(validation.validate()).isTrue();
    assertThat(results.getGroupOk()).containsExactly(validationName);
  }

  protected void validate(TigValidation validation, int errorCode, String value, String msg) {
    assertThat(validation.validate()).isFalse();
    assertThat(results.getAll())
        .containsExactly(RDAPValidationResult.builder()
            .code(errorCode)
            .value(value)
            .message(msg)
            .build());
    assertThat(results.getGroupErrorWarning()).containsExactly(validationName);
    assertThat(results.getGroupOk()).isEmpty();
  }
}
