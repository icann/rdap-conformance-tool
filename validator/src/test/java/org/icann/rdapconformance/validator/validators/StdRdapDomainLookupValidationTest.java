package org.icann.rdapconformance.validator.validators;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.ArrayList;
import org.icann.rdapconformance.validator.RDAPDeserializer;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorTestContext;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.models.domain.Domain;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StdRdapDomainLookupValidationTest {

  private final ConfigurationFile configurationFile = new ConfigurationFile();
  private final RDAPValidatorTestContext context = new RDAPValidatorTestContext(configurationFile);
  private final Validator validator = new StdRdapDomainLookupValidation(context);
  private final Domain mockedDomain = mock(Domain.class);

  @BeforeMethod
  public void setUp() throws JsonProcessingException {
    RDAPDeserializer deserializer = context.spyDeserializer();
    doReturn(new ArrayList<RDAPValidationResult>()).when(mockedDomain).validate();
    doReturn(mockedDomain).when(deserializer).deserialize(any(), eq(Domain.class));
  }

  @Test
  public void testValidate_InvalidJson() throws IOException {
    String rdapContent = context.getResource("/validators/domain/invalid.json");

    assertThat(validator.validate(rdapContent)).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", -12200)
        .hasFieldOrPropertyWithValue("value", rdapContent)
        .hasFieldOrPropertyWithValue("message", "The domain structure is not syntactically valid.");
    verify(mockedDomain, never()).validate();
  }

  @Test
  public void testValidate_InvalidKeyValuePair() throws IOException {
    String rdapContent = context.getResource("/validators/domain/unknown_key.json");

    assertThat(validator.validate(rdapContent)).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", -12201)
        .hasFieldOrPropertyWithValue("value", "unknown/[{test=value}]")
        .hasFieldOrPropertyWithValue("message",
            "The name in the name/value pair is not of: objectClassName, handle, ldhName, "
                + "unicodeName, variants, nameservers, secureDNS, entities, status, publicIds, "
                + "remarks, links, port43, events, notices or rdapConformance.");
    verify(mockedDomain).validate();
  }

  @Test
  public void testValidate_DuplicatedKey() throws IOException {
    String rdapContent = context.getResource("/validators/domain/duplicated_key.json");

    assertThat(validator.validate(rdapContent)).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", -12202)
        .hasFieldOrPropertyWithValue("value", "handle/duplicated")
        .hasFieldOrPropertyWithValue("message",
            "The name in the name/value pair of a domain structure was found more than once.");
    verify(mockedDomain, never()).validate();
  }
}