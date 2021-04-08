package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.annotations.Test;

public class SchemaValidatorNoticeTest extends SchemaValidatorObjectTest {

  public SchemaValidatorNoticeTest() {
    super("notice", "rdap_notice.json",
        "/validators/notice/valid.json",
        -10700,
        -10701,
        -10702,
        List.of("title", "type", "description", "links"));
  }

  @Test
  public void testValidate_NoticeTitleIsNotJsonString() {
    jsonObject.put("title", 0);
    validateIsNotAJsonString(-10703, "#/title:0");
  }

  @Test
  public void testValidate_InvalidLinks() {
    jsonObject.put("links", 0);
    validateSubValidation(-10704,
        "stdRdapLinksValidation", "#/links:0");
  }

  @Test
  public void testValidate_TypeIsNotJsonString() {
    jsonObject.put("type", 0);
    validateIsNotAJsonString(-10705, "#/type:0");
  }

  @Test
  public void testValidate_TypeIsNotEnum() {
    jsonObject.put("type", 0);
    validateNotEnum(-10706, "rdap_common.json#/definitions/noticeType/allOf/1", "#/type:0");
  }

  @Test
  public void testValidate_DescriptionMissing() {
    jsonObject.remove("description");
    validateKeyMissing("description", -10707);
  }

  @Test
  public void testValidate_DescriptionNotJsonArray() {
    jsonObject.put("description", 0);
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(context.getResults()).filteredOn(r -> r.getCode() == -10708)
        .hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("value", "#/description:0")
        .hasFieldOrPropertyWithValue("message",
            "The #/description structure is not syntactically valid.");
  }

  @Test
  public void testValidate_DescriptionNotArrayOfString() {
    jsonObject.put("description", List.of(0));
    validateIsNotAJsonString(-10709, "#/description/0:0");
  }
}
