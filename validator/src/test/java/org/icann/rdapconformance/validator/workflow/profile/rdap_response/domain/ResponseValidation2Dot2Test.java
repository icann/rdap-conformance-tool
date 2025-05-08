package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.mockito.Mockito.when;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.HandleValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.mockito.Mockito;
import org.testng.annotations.Test;


public class ResponseValidation2Dot2Test extends HandleValidationTest<ResponseValidation2Dot2> {
  private boolean mockedForFeb2024 = false;

  public ResponseValidation2Dot2Test() {
    super("/validators/domain/valid.json", "rdapResponseProfile_2_1_Validation",
        RDAPQueryType.DOMAIN, ResponseValidation2Dot2.class);
  }

  @Override
  protected String givenInvalidHandle() {
    replaceValue("handle", "ABCD");
    return "#/handle:ABCD";
  }

  @Override
  protected String getValidValueWithRoidExmp() {
    return "#/handle:2138514_DOMAIN_COM-EXMP";
  }

  protected String givenNullHandle() {
    replaceValue("handle", null);
    return "#/handle:null";
  }

  protected String givenNullHandle2() {
    removeKey("handle");
    return "#/handle:null";
  }

  protected String givenReservedICANNHandle() {
    replaceValue("handle", "12345678-ICANNRST");
    return "#/handle:12345678-ICANNRST";
  }

  @Test
  public void testValidate_HandleIsNull_AddErrorCode() {
    String value = givenNullHandle();
    getProfileValidation();
    validate(-46200, value,
        "The handle in the entity object does not comply with the format "
            + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
  }

  @Test
  public void testValidate_HandleIsNull2_AddErrorCode() {
    String value = givenNullHandle2();
    getProfileValidation();
    validate(-46200, value,
        "The handle in the entity object does not comply with the format "
            + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
  }

  // this is a problem, you cannot run it like the others above
  @Test
  public void testValidate_HandleIsInvalid_AddErrorCode() {
    // Store the original configuration
    RDAPValidatorConfiguration originalConfig = config;

    try {
      // Create a mock configuration using SPY instead of mock
      RDAPValidatorConfiguration mockConfig = Mockito.spy(originalConfig);

      // SUPER important ->  override this method
      when(mockConfig.useRdapProfileFeb2024()).thenReturn(true);

      // Replace the config with our mock
      this.config = mockConfig;

      // Run the test with our mocked configuration
      String value = givenReservedICANNHandle();
      getProfileValidation();
      validate(-46205, value,
          "The globally unique identifier in the domain object handle is using an EPPROID reserved for testing by ICANN.");
    } finally {
      // Restore the original configuration
      this.config = originalConfig;
    }
  }
}