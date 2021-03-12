package org.icann.rdapconformance.validator.validators;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.RDAPDeserializer;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorTestContext;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.models.RDAPValidate;
import org.icann.rdapconformance.validator.models.common.RDAPObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class StdRdapValidationTest<T extends RDAPValidate> {

  private final ConfigurationFile configurationFile = new ConfigurationFile();
  protected final RDAPValidatorTestContext context = new RDAPValidatorTestContext(
      configurationFile);
  private final Validator<T> validator;
  private T mockedType;
  private final Class<T> clazz;
  private final String validatorName;

  public StdRdapValidationTest(Class<T> clazz, String validatorName) {
    this.clazz = clazz;
    this.validatorName = validatorName;
    this.validator = this.context.getValidator(validatorName);
  }

  @BeforeMethod
  public void setUp() throws JsonProcessingException {
    this.mockedType = mock(clazz);
    RDAPDeserializer deserializer = context.spyDeserializer();
    doReturn(new ArrayList<RDAPValidationResult>()).when(mockedType).validate();
    doReturn(mockedType).when(deserializer).deserialize(any(), eq(clazz));
  }

  @Test
  public void testStdRdapValidationExtendsValidator() {
    assertThat(validator).isInstanceOf(Validator.class);
  }

  @Test
  public void testValidate_Ok() throws JsonProcessingException {
    Map<String, String> keyValues = validator
        .getAuthorizedKeys()
        .stream()
        .collect(Collectors.toMap(k -> k, v -> v));
    String json = new ObjectMapper().writeValueAsString(keyValues);
    assertThat(validator.validate(json)).isEmpty();
    verify(mockedType).validate();
  }

  @Test
  public void testValidate_InvalidJson() throws IOException {
    String invalidRdapContent = "{\"invalid-json\": \"with trailing comma\",}";

    assertThat(validator.validate(invalidRdapContent)).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", validator.getInvalidJsonErrorCode())
        .hasFieldOrPropertyWithValue("value", invalidRdapContent)
        .hasFieldOrPropertyWithValue("message",
            "The " + clazz.getSimpleName() + " structure is not syntactically valid.");
    verify(mockedType, never()).validate();
  }

  @Test
  public void testValidate_InvalidKeyValuePair() throws IOException {
    String invalidRdapContent = "{\"unknown\": [{\"test\":  \"value\"}]}";

    assertThat(validator.validate(invalidRdapContent)).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", validator.getInvalidKeysErrorCode())
        .hasFieldOrPropertyWithValue("value", "unknown/[{test=value}]")
        .hasFieldOrPropertyWithValue("message",
            "The name in the name/value pair is not of: " + String.join(", ",
                validator.getAuthorizedKeys()) + ".");
    verify(mockedType).validate();
  }

  @Test
  public void testValidate_DuplicatedKey() throws IOException {
    String invalidRdapContent = "{\"notices\": \"duplicated\", \"notices\": \"duplicated\"}";

    assertThat(validator.validate(invalidRdapContent)).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", validator.getDuplicateKeyErrorCode())
        .hasFieldOrPropertyWithValue("value", "notices/duplicated")
        .hasFieldOrPropertyWithValue("message",
            "The name in the name/value pair of a "+clazz.getSimpleName()+" structure was found more than once.");
    verify(mockedType, never()).validate();
  }
}