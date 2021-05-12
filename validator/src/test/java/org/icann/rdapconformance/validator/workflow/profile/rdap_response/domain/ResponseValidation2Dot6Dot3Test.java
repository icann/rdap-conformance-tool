package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot6Dot3Test extends ProfileJsonValidationTestBase {

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
  private RDAPQueryType queryType;

  public ResponseValidation2Dot6Dot3Test() {
    super("/validators/domain/valid.json", "rdapResponseProfile_2_6_3_Validation");
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    queryType = RDAPQueryType.DOMAIN;
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot6Dot3(jsonObject.toString(), results, queryType);
  }

  @Test
  public void testValidate_NoNoticeMatchingWithTitle_AddResults46600() {
    String title = "TEST";
    replaceValue("$['notices'][1]['title']", title);
    validate(-46600, String.format(NOTICE_VALUE, ResponseValidation2Dot6Dot3.DESCRIPTION,
        ResponseValidation2Dot6Dot3.HREF, title),
        "The notice for https://icann.org/epp was not found.");
  }

  @Test
  public void testValidate_NoNoticeMatchingWithDescription_AddResults46600() {
    String description = "TEST";
    replaceValue("$['notices'][1]['description'][0]", description);
    validate(-46600, String.format(NOTICE_VALUE, description, ResponseValidation2Dot6Dot3.HREF,
        ResponseValidation2Dot6Dot3.TITLE), "The notice for https://icann.org/epp was not found.");
  }

  @Test
  public void testValidate_NoNoticeMatchingWithLinksHref_AddResults46600() {
    String href = "http://test.example";
    replaceValue("$['notices'][1]['links'][0]['href']", href);
    validate(-46600, String.format(NOTICE_VALUE, ResponseValidation2Dot6Dot3.DESCRIPTION, href,
        ResponseValidation2Dot6Dot3.TITLE), "The notice for https://icann.org/epp was not found.");
  }

  @Test
  public void testDoLaunch() {
    queryType = RDAPQueryType.HELP;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVERS;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVER;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.ENTITY;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.DOMAIN;
    assertThat(getProfileValidation().doLaunch()).isTrue();
  }
}
