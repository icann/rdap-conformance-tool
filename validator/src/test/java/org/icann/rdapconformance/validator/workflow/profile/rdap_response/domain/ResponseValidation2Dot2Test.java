package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.rdap_response.HandleValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.Test;

public class ResponseValidation2Dot2Test extends HandleValidationTest<ResponseValidation2Dot2> {

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

  @Test
  public void testValidate_HandleIsNull_AddErrorCode() {
    String value = givenNullHandle();
    getProfileValidation();
    validate(-46200, value,
        String.format("The handle in the %s object does not comply with the format "
            + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.", baseQueryType.name().toLowerCase()));
  }

  @Test
  public void testValidate_HandleIsNull2_AddErrorCode() {
    String value = givenNullHandle2();
    getProfileValidation();
    validate(-46200, value,
        String.format("The handle in the %s object does not comply with the format "
            + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.", baseQueryType.name().toLowerCase()));
  }
}