package org.icann.rdapconformance.validator.validators;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.models.Help;
import org.testng.annotations.Test;

public class StdRdapHelpValidationTest extends StdRdapValidationTest<Help> {

  public StdRdapHelpValidationTest() {
    super(Help.class, "stdRdapHelpValidation");
  }
}