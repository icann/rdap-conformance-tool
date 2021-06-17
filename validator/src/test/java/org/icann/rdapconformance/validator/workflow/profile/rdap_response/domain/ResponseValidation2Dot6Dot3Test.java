package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;

public class ResponseValidation2Dot6Dot3Test extends NoticesValidationTest {

  private static final String NOTICE_VALUE = "#/notices:["
      + "{\"description\":[\"Service subject to Terms of Use.\"],"
      + "\"links\":[{"
      + "\"href\":\"https://www.example.com/domain-names/registration-data-access-protocol/terms-service/index.xhtml\","
      + "\"type\":\"text/html\"}],"
      + "\"title\":\"Terms of Use\"},"
      + "{\"description\":[\"%s\"],\"links\":[{\"href\":\"%s\",\"type\":\"text/html\"}],\"title\":\"%s\"},"
      + "{\"description\":[\"URL of the ICANN RDDS Inaccuracy Complaint Form: https://icann.org/wicf\"],"
      + "\"links\":[{\"href\":\"https://icann.org/wicf\",\"type\":\"text/html\"}],"
      + "\"title\":\"RDDS Inaccuracy Complaint Form\"}]";

  public ResponseValidation2Dot6Dot3Test() throws Throwable {
    super("rdapResponseProfile_2_6_3_Validation", NOTICE_VALUE, 1);
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot6Dot3(jsonObject.toString(), results, queryType);
  }
}
