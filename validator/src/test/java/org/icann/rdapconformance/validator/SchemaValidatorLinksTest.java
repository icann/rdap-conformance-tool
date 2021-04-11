package org.icann.rdapconformance.validator;

import java.util.List;
import org.testng.annotations.Ignore;
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
    replaceArrayProperty("media", "wrong enum value");
    validateNotEnum(-10603, "rdap_common.json#/definitions/link/properties/media/allOf/1",
        "#/links/0/media:wrong enum value");
  }

  /**
   * 7.2.2.2.4.
   */
  @Test
  public void relNotInEnum() {
    replaceArrayProperty("rel", "wrong enum value");
    validateNotEnum(-10604, "rdap_common.json#/definitions/link/properties/rel",
        "#/links/0/rel:wrong enum value");
  }

  /**
   * 7.2.2.2.5.
   * TODO when dataset handling will be done.
   */
  @Ignore
  @Test
  public void typeNotInEnum() {
    replaceArrayProperty("type", "wrong enum value");
    validateNotEnum(-10605, "rdap_common.json#/definitions/link/properties/type",
        "#/links/0/type:wrong enum value");
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
