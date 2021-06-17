package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;

public class ResponseValidation2Dot11Test extends
    NoticesValidationTest {

  private static final String NOTICE_VALUE = "#/notices:["
      + "{\"description\":[\"Service subject to Terms of Use.\"],"
      + "\"links\":[{"
      + "\"href\":\"https://www.example.com/domain-names/registration-data-access-protocol/terms-service/index.xhtml\","
      + "\"type\":\"text/html\"}],"
      + "\"title\":\"Terms of Use\"},"
      + "{\"description\":[\"For more information on domain status codes, please visit https://icann.org/epp\"],"
      + "\"links\":[{\"href\":\"https://icann.org/epp\",\"type\":\"text/html\"}],"
      + "\"title\":\"Status Codes\"},"
      + "{\"description\":[\"%s\"],"
      + "\"links\":[{\"href\":\"%s\",\"type\":\"text/html\"}],"
      + "\"title\":\"%s\"}]";

  public ResponseValidation2Dot11Test() throws Throwable {
    super("rdapResponseProfile_2_11_Validation", NOTICE_VALUE, 2);
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot11(jsonObject.toString(), results, queryType);
  }
}
