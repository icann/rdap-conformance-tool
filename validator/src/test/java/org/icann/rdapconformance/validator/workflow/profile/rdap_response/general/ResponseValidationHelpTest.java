package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidationTestBase;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.net.URI;

import static org.mockito.Mockito.*;

public class ResponseValidationHelpTest extends ProfileValidationTestBase {

  public static final String HELP = "/help";;

  private final RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
  }

  @Override
  public ResponseValidationHelp_2024 getProfileValidation() {
    doReturn(URI.create("https://rdap.verisign.com/com/v1/domain/GOOGLE.COM")).when(config).getUri();
    return new ResponseValidationHelp_2024(config, results);
  }

}