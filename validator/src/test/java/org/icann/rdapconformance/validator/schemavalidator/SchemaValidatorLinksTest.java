package org.icann.rdapconformance.validator.schemavalidator;

import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.LinkRelations;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.MediaTypes;
import org.testng.annotations.Test;

public class SchemaValidatorLinksTest extends SchemaValidatorForArrayTest {

  public SchemaValidatorLinksTest() {
    super("test_rdap_links.json",
        "/validators/links/valid.json");
  }

  /**
   * 7.2.2.1.
   */
  @Test
  public void invalid() {
    invalid(-10600);
  }

  /**
   * 7.2.2.2.
   */
  @Test
  public void unauthorizedKey() {
    validateArrayAuthorizedKeys(-10601,
        List.of("href"
            , "hreflang"
            , "media"
            , "rel"
            , "title"
            , "type"
            , "value"));
  }

  /**
   * 7.2.2.2.3.
   */
  @Test
  public void mediaNotInEnum() {
    replaceArrayProperty("media", WRONG_ENUM_VALUE);
    validateNotEnum(-10603, "rdap_common.json#/definitions/link/properties/media/allOf/1",
        "#/links/0/media:" + WRONG_ENUM_VALUE);
  }

  /**
   * 7.2.2.2.4.
   */
  @Test
  public void relNotInEnum() {
    doReturn(true).when(datasetService.get(LinkRelations.class)).isInvalid(WRONG_ENUM_VALUE);
    validate(-10604, replaceArrayProperty("rel", WRONG_ENUM_VALUE),
        "The JSON value is not included as a Relation Name in linkRelations.");
  }

  /**
   * 7.2.2.2.5.
   */
  @Test
  public void typeNotInEnum() {
    doReturn(true).when(datasetService.get(MediaTypes.class)).isInvalid(WRONG_ENUM_VALUE);
    validate(-10605, replaceArrayProperty("type", WRONG_ENUM_VALUE),
        "The JSON value is not included as a Name in mediaTypes.");
  }

  /**
   * 7.2.2.2.6.
   */
  @Test
  public void titleNotJsonString() {
    arrayItemKeyIsNotString("title", -10606);
  }

  /**
   * 7.2.2.2.7 string part.
   */
  @Test
  public void hreflangNotJsonString() {
    arrayItemKeyIsNotString("hreflang", -10607);
  }

  /**
   * 7.2.2.2.7 array part.
   */
  @Test
  public void hreflangNotArrayOfString() {
    replaceArrayProperty("hreflang", List.of(0));
    validateIsNotAJsonString(-10607, "#/links/0/hreflang:[0]");
  }

  /**
   * 7.2.2.2.8.
   */
  @Test
  public void hreflangViolatesLanguageTagSyntax() {
    replaceArrayProperty("hreflang", "000");
    validateRegex(-10608,
        "rdap_common.json#/definitions/link/properties/hreflang/oneOf/1/allOf/1",
        "#/links/0/hreflang:000");
  }

  /**
   * 7.2.2.2.9.
   */
  @Test
  public void valueViolatesWebUriValidation() {
    arrayItemKeySubValidation("value", "webUriValidation", -10609);
  }

  /**
   * 7.2.2.2.10.
   */
  @Test
  public void hrefDoesNotExist() {
    keyDoesNotExistInArray("href", -10610);
  }

  /**
   * 7.2.2.2.11.
   */
  @Test
  public void hrefViolatesWebUriValidation() {
    arrayItemKeySubValidation("href", "webUriValidation", -10611);
  }
}
