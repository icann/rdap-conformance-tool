package org.icann.rdapconformance.validator.workflow.profile.tig_section;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class TigValidationTestBase {

  protected RDAPValidatorResults results;

  public static void validateOk(TigValidation validation, RDAPValidatorResults results) {
    assertThat(validation.validate()).isTrue();
    verify(results).addGroup(validation.getGroupName(), false);
    verifyNoMoreInteractions(results);
  }

  public static void validateNotOk(TigValidation validation, RDAPValidatorResults results,
      int code, String value, String message) {
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);
    assertThat(validation.validate()).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", code)
        .hasFieldOrPropertyWithValue("value", value)
        .hasFieldOrPropertyWithValue("message", message);
    verify(results).addGroup(validation.getGroupName(), true);
  }

  @BeforeMethod
  public void setUp() throws Throwable {
    results = mock(RDAPValidatorResults.class);
  }

  protected void validateOk(TigValidation validation) {
    validateOk(validation, results);
  }

  protected void validateNotOk(TigValidation validation, int code, String value, String message) {
    validateNotOk(validation, results, code, value, message);
  }

  @Test
  public abstract void testValidate() throws Throwable;
}
