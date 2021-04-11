package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.annotations.Test;

public class SchemaValidatorNoticeTest extends SchemaValidatorForArrayTest {

  public SchemaValidatorNoticeTest() {
    super("test_rdap_notices.json",
        "/validators/notices/valid.json");
  }

  /**
   * 7.2.3.1.
   */
  @Test
  public void invalid() {
    invalid(-10700);
  }

  /**
   * 7.2.3.2.1
   */
  @Test
  public void unauthorizedKey() {
    validateArrayAuthorizedKeys(-10701, List.of("description", "links", "title", "type"));
  }

  /**
   * 7.2.3.2.3
   */
  @Test
  public void titleNotJsonString() {
    arrayItemKeyIsNotString("title", -10703);
  }

  /**
   * 7.2.3.2.4
   */
  @Test
  public void linksViolatesLinksValidation() {
    linksViolatesLinksValidation(-10704);
  }

  /**
   * 7.2.3.2.5
   */
  @Test
  public void typeNotJsonString() {
    arrayItemKeyIsNotString("type", -10705);
  }

  /**
   * 7.2.3.2.6
   */
  @Test
  public void typeNotInEnum() {
    replaceArrayProperty("type", 0);
    validateNotEnum(-10706, "rdap_common.json#/definitions/noticeType/allOf/1", "#/notices/0/type:0");
  }

  /**
   * 7.2.3.2.7
   */
  @Test
  public void descriptionDoesNotExist() {
    keyDoesNotExistInArray("description", -10707);
  }

  /**
   * 7.2.3.2.8
   */
  @Test
  public void descriptionNotJsonArray() {
    replaceArrayProperty("description", 0);
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll()).filteredOn(r -> r.getCode() == -10708)
        .hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("value", "#/notices/0/description:0")
        .hasFieldOrPropertyWithValue("message",
            "The #/notices/0/description structure is not syntactically valid.");
  }

  /**
   * 7.2.3.2.9
   */
  @Test
  public void descriptionNotArrayOfString() {
    replaceArrayProperty("description", List.of(0));
    validateIsNotAJsonString(-10709, "#/notices/0/description/0:0");
  }
}
