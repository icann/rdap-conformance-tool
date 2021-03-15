package org.icann.rdapconformance.validator.models.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.icann.rdapconformance.validator.RDAPValidatorTestContext;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NoticeAndRemarkTest {

  private RDAPValidatorTestContext context;

  @BeforeMethod
  public void setUp() {
    ConfigurationFile configurationFile = mock(ConfigurationFile.class);
    context = new RDAPValidatorTestContext(configurationFile);
  }

  @Test
  public void testValidate_TitleIsNotJsonString() {
    NoticeAndRemark noticeAndRemark = getValidNoticeAndRemark();
    noticeAndRemark.title = 0;
    assertThat(noticeAndRemark.validate()).isFalse();
    assertThat(noticeAndRemark.getContext().getResults())
        .hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", -10703)
        .hasFieldOrPropertyWithValue("value", "title/0")
        .hasFieldOrPropertyWithValue("message", "The JSON value is not a string.");
  }

  @Test
  public void testValidate_TypeIsNotJsonString() {
    NoticeAndRemark noticeAndRemark = getValidNoticeAndRemark();
    noticeAndRemark.type = 0;
    assertThat(noticeAndRemark.validate()).isFalse();
    assertThat(noticeAndRemark.getContext().getResults())
        .hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", -10705)
        .hasFieldOrPropertyWithValue("value", "type/0")
        .hasFieldOrPropertyWithValue("message", "The JSON value is not a string.");
  }

  @Test
  public void testValidate_TypeIsNotEnum() {
    NoticeAndRemark noticeAndRemark = getValidNoticeAndRemark();
    noticeAndRemark.type = "not part of the noticeType enum";
    assertThat(noticeAndRemark.validate()).isFalse();
    assertThat(noticeAndRemark.getContext().getResults()).filteredOn(r -> r.getCode() == -10706)
        .hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("value", "not part of the noticeType enum")
        .hasFieldOrPropertyWithValue("message",
            "The JSON string is not included as a Value with Type=\"noticeType\" in the "
                + "RDAPJSONValues dataset.");
  }

  @Test
  public void testValidate_DescriptionMissing() {
    NoticeAndRemark noticeAndRemark = getValidNoticeAndRemark();
    noticeAndRemark.description = null;
    assertThat(noticeAndRemark.validate()).isFalse();
    assertThat(noticeAndRemark.getContext().getResults()).filteredOn(r -> r.getCode() == -10707)
        .hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("message",
            "The description element does not exist.");
  }

  @Test
  public void testValidate_DescriptionNotJsonArray() {
    NoticeAndRemark noticeAndRemark = getValidNoticeAndRemark();
    noticeAndRemark.description = "not json array";
    assertThat(noticeAndRemark.validate()).isFalse();
    assertThat(noticeAndRemark.getContext().getResults()).filteredOn(r -> r.getCode() == -10708)
        .hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("value", "not json array")
        .hasFieldOrPropertyWithValue("message",
            "The description structure is not syntactically valid.");
  }

  @Test
  public void testValidate_DescriptionNotArrayOfString() {
    NoticeAndRemark noticeAndRemark = getValidNoticeAndRemark();
    noticeAndRemark.description = List.of(0);
    assertThat(noticeAndRemark.validate()).isFalse();
    assertThat(noticeAndRemark.getContext().getResults()).filteredOn(r -> r.getCode() == -10709)
        .hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("value", "description/[0]")
        .hasFieldOrPropertyWithValue("message",
            "The JSON value is not a string.");
  }

  private NoticeAndRemark getValidNoticeAndRemark() {
    NoticeAndRemark noticeAndRemark = new NoticeAndRemark(context);
    noticeAndRemark.description = List.of("test");
    noticeAndRemark.title = "test";
    noticeAndRemark.type = "result set truncated due to authorization";
    Link link = new Link();
    link.href = "https://www.example.com/test";
    link.type = "text/html";
    return noticeAndRemark;
  }
}