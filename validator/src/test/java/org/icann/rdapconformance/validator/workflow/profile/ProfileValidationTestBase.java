package org.icann.rdapconformance.validator.workflow.profile;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.ValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class ProfileValidationTestBase implements ValidationTest {

  protected RDAPValidatorResults results;
  protected RDAPValidatorConfiguration config;
  protected QueryContext queryContext;

  public abstract ProfileValidation getProfileValidation();

  public void validate() {
    validateOk(results);
  }

  public void validate(int code, String value, String message) {
    validateNotOk(results, code, value, message);
  }

  @BeforeMethod
  public void setUp() throws IOException {
    results = mock(RDAPValidatorResults.class);
    config = mock(RDAPValidatorConfiguration.class);
    when(config.isGtldRegistrar()).thenReturn(true);

    // Create QueryContext for testing - this will be available for subclasses
    queryContext = QueryContext.forTesting("{}", results, config);

    // PERFORMANCE FIX: Reduce HTTP 429 retry backoff from 30 seconds to 1 second for tests
    // This prevents tests from hanging for 30 seconds when hitting rate limits
    RDAPHttpRequest.DEFAULT_BACKOFF_SECS = 1;
  }

  @Test
  public void testValidate_ok() {
    validate();
  }
}
