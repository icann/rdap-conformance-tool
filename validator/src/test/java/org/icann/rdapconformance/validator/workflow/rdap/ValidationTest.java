package org.icann.rdapconformance.validator.workflow.rdap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.mockito.ArgumentCaptor;

public interface ValidationTest {

  ProfileValidation getTigValidation();

  default void validateOk(RDAPValidatorResults results) {
    ProfileValidation validation = getTigValidation();
    assertThat(validation.validate()).isTrue();
    verify(results).addGroup(validation.getGroupName());
    verifyNoMoreInteractions(results);
  }

  default void validateNotOk(RDAPValidatorResults results,
      int code, String value, String message) {
    ProfileValidation validation = getTigValidation();
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);
    assertThat(validation.validate()).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", code)
        .hasFieldOrPropertyWithValue("value", value)
        .hasFieldOrPropertyWithValue("message", message);
    verify(results).addGroupErrorWarning(validation.getGroupName());
  }

}
