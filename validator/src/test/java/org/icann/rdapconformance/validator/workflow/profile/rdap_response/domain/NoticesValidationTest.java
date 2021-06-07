package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class NoticesValidationTest extends ResponseDomainValidationTestBase {

  private String TITLE;
  private String DESCRIPTION;
  private String HREF;
  private final String noticeValue;
  private final int noticeIndex;
  private String message;

  public NoticesValidationTest(String testGroupName,
      String noticeValue, int noticeIndex) throws Throwable {
    super(testGroupName);
    this.noticeValue = noticeValue;
    this.noticeIndex = noticeIndex;
  }

  @Override
  @BeforeMethod
  public void setUp() throws java.io.IOException {
    super.setUp();
    NoticesValidation validation = ((NoticesValidation)getProfileValidation());
    TITLE = validation.title;
    DESCRIPTION = validation.description;
    HREF = validation.href;
    message = String.format("The notice for %s was not found.", HREF);
  }

  @Test
  public void testValidate_NoNoticeMatchingWithTitle_AddErrorCode() {
    String title = "TEST";
    replaceValue(String.format("$['notices'][%d]['title']", noticeIndex), title);
    validate(((NoticesValidation)getProfileValidation()).code, String.format(noticeValue,
      DESCRIPTION, HREF, title),
        message);
  }

  @Test
  public void testValidate_NoNoticeMatchingWithDescription_AddErrorCode() {
    String description = "TEST";
    replaceValue(String.format("$['notices'][%d]['description'][0]", noticeIndex), description);
    validate(((NoticesValidation)getProfileValidation()).code, String.format(noticeValue, description, HREF, TITLE),
        message);
  }

  @Test
  public void testValidate_NoNoticeMatchingWithLinksHref_AddErrorCode() {
    String href = "http://test.example";
    replaceValue(String.format("$['notices'][%d]['links'][0]['href']", noticeIndex), href);
    validate(((NoticesValidation)getProfileValidation()).code, String.format(noticeValue, DESCRIPTION, href, TITLE),
        message);
  }
}
