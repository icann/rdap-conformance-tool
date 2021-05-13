package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

public class ResponseValidation2Dot2Test extends HandleValidationTest<ResponseValidation2Dot2> {

  public ResponseValidation2Dot2Test() {
    super("rdapResponseProfile_2_1_Validation", ResponseValidation2Dot2.class);
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
}