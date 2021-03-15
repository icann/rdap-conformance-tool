package org.icann.rdapconformance.validator.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.icann.rdapconformance.validator.RDAPValidatorTestContext;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.validators.StdRdapConformanceValidation;
import org.icann.rdapconformance.validator.validators.StdRdapNoticesRemarksValidation;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HelpTest {

  private RDAPValidatorTestContext context;
  private StdRdapNoticesRemarksValidation noticesRemarksValidationMock;
  private StdRdapConformanceValidation rdapConformanceValidationMock;
  private Help help;

  @BeforeMethod
  public void setUp() {
    help = new Help();
    ConfigurationFile configurationFile = mock(ConfigurationFile.class);
    context = new RDAPValidatorTestContext(configurationFile);
    noticesRemarksValidationMock = context
      .mockValidator("stdRdapNoticesRemarksValidation", StdRdapNoticesRemarksValidation.class);
        rdapConformanceValidationMock = context
      .mockValidator("stdRdapConformanceValidation", StdRdapConformanceValidation.class);
    help.context = context;
    doReturn(List.of()).when(configurationFile).getDefinitionIgnore();
  }

  @Test
  public void testValidateNoticesRemarks() {
    help.notices = "[]";
    help.validate();
    verify(noticesRemarksValidationMock).validate(help.notices);
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
            "The value for the JSON name value does not pass notices validation [stdRdapNoticesRemarksValidation].");
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
            "The value for the JSON name value does not pass rdapConformance validation [stdRdapConformanceValidation].");
  }
}