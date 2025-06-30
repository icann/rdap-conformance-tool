package org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver;

import static org.mockito.Mockito.when;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.HandleValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.mockito.Mockito;
import org.testng.annotations.Test;

public class ResponseValidation4Dot1HandleTest extends
    HandleValidationTest<ResponseValidation4Dot1Handle> {

  public ResponseValidation4Dot1HandleTest() {
    super("/validators/nameserver/valid.json", "rdapResponseProfile_4_1_Validation",
        RDAPQueryType.NAMESERVER, ResponseValidation4Dot1Handle.class, "nameserver");
  }

  @Override
  protected String givenInvalidHandle() {
    replaceValue("handle", "ABCD");
    return "#/handle:ABCD";
  }

  protected String givenReservedICANNHandle() {
    replaceValue("handle", "ABCD-ICANNRST");
    return "#/handle:ABCD-ICANNRST";
  }

  @Override
  protected String getValidValueWithRoidExmp() {
    return "#/handle:2138514_NS_COM-EXMP";
  }
}