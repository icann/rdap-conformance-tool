package org.icann.rdapconformance.validator.workflow.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.testng.annotations.Test;

public class RDAPProfileFebruary2019Test {

  @Test
  public void testValidateExceptionHandling() {
    ProfileValidation profileValidation = mock(ProfileValidation.class);
    when(profileValidation.validate()).thenThrow(new RuntimeException("test"));
    RDAPProfileFebruary2019 rdapProfileFebruary2019 = new RDAPProfileFebruary2019(
        List.of(profileValidation));
    assertThat(rdapProfileFebruary2019.validate()).isFalse();
  }
}