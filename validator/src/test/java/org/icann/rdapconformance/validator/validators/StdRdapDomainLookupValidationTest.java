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

public class StdRdapDomainLookupValidationTest extends StdRdapValidationTest<Domain> {

  public StdRdapDomainLookupValidationTest() {
    super(Domain.class, "stdRdapDomainLookupValidation");
  }
}