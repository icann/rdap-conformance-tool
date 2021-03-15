package org.icann.rdapconformance.validator.models.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;
import org.icann.rdapconformance.validator.RDAPValidatorTestContext;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.validators.StdRdapLdhNameValidation;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DomainTest {

  private RDAPValidatorTestContext context;
  private StdRdapLdhNameValidation ldhNameValidationMock;

  @BeforeMethod
  public void setUp() {
    ConfigurationFile configurationFile = mock(ConfigurationFile.class);
    context = new RDAPValidatorTestContext(configurationFile);
    ldhNameValidationMock = context
        .mockValidator("stdRdapLdhNameValidation", StdRdapLdhNameValidation.class);
    doReturn(true).when(ldhNameValidationMock).validate(any());
    doReturn(List.of()).when(configurationFile).getDefinitionIgnore();
  }

  @Test
  public void testValidate_WrongObjectClassName() throws IOException {
    String rdapContent = context.getResource("/validators/domain/wrong_objectClassName.json");
    Domain domain = context.getDeserializer().deserialize(rdapContent, Domain.class);

    assertThat(domain.validate()).isFalse();
    assertThat(context.getResults()).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", -12203)
        .hasFieldOrPropertyWithValue("value", "objectClassName/wrong")
        .hasFieldOrPropertyWithValue("message", "The JSON value is not \"domain\".");
  }

  @Test
  public void testValidate_InvalidHandle() throws IOException {
    String rdapContent = context.getResource("/validators/domain/invalid_handle.json");
    Domain domain = context.getDeserializer().deserialize(rdapContent, Domain.class);

    assertThat(domain.validate()).isFalse();
    assertThat(context.getResults()).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", -12204)
        .hasFieldOrPropertyWithValue("value", "handle/{key=value}")
        .hasFieldOrPropertyWithValue("message", "The JSON value is not a string.");
  }

  @Test
  public void testValidate_InvalidLdhName() throws IOException {
    String rdapContent = context.getResource("/validators/domain/ldhName.json");
    Domain domain = context.getDeserializer().deserialize(rdapContent, Domain.class);
    doReturn(false).when(ldhNameValidationMock).validate("test");

    assertThat(domain.validate()).isFalse();
    assertThat(context.getResults()).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", -12205)
        .hasFieldOrPropertyWithValue("value", "ldhName/test")
        .hasFieldOrPropertyWithValue("message",
            "The value for the JSON name value does not pass ldhName validation [stdRdapLdhNameValidation].");
  }
}