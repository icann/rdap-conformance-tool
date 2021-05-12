package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NoticesValidationTest<T extends NoticesValidation> extends
    ProfileJsonValidationTestBase {

  private final String TITLE;
  private final String DESCRIPTION;
  private final String HREF;
  private final String noticeValue;
  private final int noticeIndex;
  private final String message;
  private final Class<T> validationClass;
  private RDAPQueryType queryType;

  public NoticesValidationTest(String testGroupName,
      String noticeValue, int noticeIndex, Class<T> validationClass) throws Throwable {
    super("/validators/domain/valid.json", testGroupName);
    this.noticeValue = noticeValue;
    this.noticeIndex = noticeIndex;
    this.validationClass = validationClass;
    TITLE = (String) validationClass.getDeclaredField("TITLE").get(String.class);
    DESCRIPTION = (String) validationClass.getDeclaredField("DESCRIPTION").get(String.class);
    HREF = (String) validationClass.getDeclaredField("HREF").get(String.class);
    message = String.format("The notice for %s was not found.", HREF);
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    queryType = RDAPQueryType.DOMAIN;
  }

  @Override
  public NoticesValidation getProfileValidation() {
    try {
      return validationClass.getConstructor(String.class, RDAPValidatorResults.class,
          RDAPQueryType.class).newInstance(jsonObject.toString(), results, queryType);
    } catch (Exception e) {
      return null;
    }
  }

  @Test
  public void testValidate_NoNoticeMatchingWithTitle_AddErrorCode() {
    String title = "TEST";
    replaceValue(String.format("$['notices'][%d]['title']", noticeIndex), title);
    validate(getProfileValidation().code, String.format(noticeValue, DESCRIPTION, HREF, title),
        message);
  }

  @Test
  public void testValidate_NoNoticeMatchingWithDescription_AddErrorCode() {
    String description = "TEST";
    replaceValue(String.format("$['notices'][%d]['description'][0]", noticeIndex), description);
    validate(getProfileValidation().code, String.format(noticeValue, description, HREF, TITLE),
        message);
  }

  @Test
  public void testValidate_NoNoticeMatchingWithLinksHref_AddErrorCode() {
    String href = "http://test.example";
    replaceValue(String.format("$['notices'][%d]['links'][0]['href']", noticeIndex), href);
    validate(getProfileValidation().code, String.format(noticeValue, DESCRIPTION, href, TITLE),
        message);
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
