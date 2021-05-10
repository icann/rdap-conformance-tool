package org.icann.rdapconformance.validator.workflow.profile;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.ValidationTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class ProfileValidationTestBase implements ValidationTest {

  protected RDAPValidatorResults results;

  public abstract ProfileValidation getTigValidation();

  public void validate() {
    validateOk(results);
  }

  public void validate(int code, String value, String message) {
    validateNotOk(results, code, value, message);
  }

  @BeforeMethod
  public void setUp() throws IOException {
    results = mock(RDAPValidatorResults.class);
  }

  @Test
  public void testValidate_ok() {
    validate();
  }
}
