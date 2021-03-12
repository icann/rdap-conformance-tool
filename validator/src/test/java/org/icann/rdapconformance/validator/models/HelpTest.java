package org.icann.rdapconformance.validator.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.icann.rdapconformance.validator.RDAPValidatorTestContext;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.validators.StdRdapConformanceValidation;
import org.icann.rdapconformance.validator.validators.StdRdapNoticesRemarksValidation;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HelpTest {

  private final ConfigurationFile configurationFile = new ConfigurationFile();
  private final RDAPValidatorTestContext context = new RDAPValidatorTestContext(configurationFile);
  StdRdapNoticesRemarksValidation noticesRemarksValidationMock = context
      .mockValidator("stdRdapNoticesRemarksValidation", StdRdapNoticesRemarksValidation.class);
  StdRdapConformanceValidation rdapConformanceValidationMock = context
      .mockValidator("stdRdapConformanceValidation", StdRdapConformanceValidation.class);
  private Help help;

  @BeforeMethod
  public void setUp() {
    help = new Help();
    help.context = context;
  }

  @Test
  public void testValidateNoticesRemarks() {
    help.validate();
    verify(noticesRemarksValidationMock).validate(any());
  }

  @Test
  public void testValidate_InvalidNotices() {
    help.notices = "test";
    when(noticesRemarksValidationMock.validate("test")).thenReturn(false);

    assertThat(help.validate()).isFalse();
    assertThat(context.getResults()).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", -12503)
        .hasFieldOrPropertyWithValue("value", "notices/test")
        .hasFieldOrPropertyWithValue("message",
            "The value for the JSON name value does not pass notices validation stdRdapNoticesRemarksValidation.");
  }

  @Test
  public void testValidate_InvalidRdapConformance() {
    help.rdapConformance = "test";
    when(rdapConformanceValidationMock.validate("test")).thenReturn(false);

    assertThat(help.validate()).isFalse();
    assertThat(context.getResults()).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", -12505)
        .hasFieldOrPropertyWithValue("value", "rdapConformance/test")
        .hasFieldOrPropertyWithValue("message",
            "The value for the JSON name value does not pass rdapConformance validation stdRdapConformanceValidation.");
  }
}