package org.icann.rdapconformance.validator;

import java.util.List;
import org.json.JSONObject;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class SchemaValidatorLinksTest extends SchemaValidatorObjectTest {

  public SchemaValidatorLinksTest() {
    super(
        "links",
        "test_rdap_links.json",
        "/validators/links/valid.json",
        -10600,
        -10601,
        -10602,
        List.of("value", "rel", "href", "hreflang", "title", "media", "type"));
  }

  @Override
  protected void insertForbiddenKey() {
    JSONObject value = new JSONObject();
    value.put("test", "value");
    JSONObject forbiddenElement = jsonObject.getJSONArray("links").getJSONObject(0).put("unknown",
        List.of(value));
    jsonObject.put("links", List.of(forbiddenElement));
  }

  protected void replaceProperty(String key, Object value) {
    jsonObject.put("links", List.of(jsonObject.getJSONArray("links").getJSONObject(0).put(key,
        value)));
  }

  /**
   * 7.2.2.2.3.
   */
  @Test
  public void mediaNotInEnum() {
    replaceProperty("media", "wrong enum value");
    validateNotEnum(-10603, "rdap_common.json#/definitions/link/properties/media/allOf/1",
        "#/links/0/media:wrong enum value");
  }

  /**
   * 7.2.2.2.4.
   */
  @Test
  public void relNotInEnum() {
    replaceProperty("rel", "wrong enum value");
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
    replaceProperty("type", "wrong enum value");
    validateNotEnum(-10605, "rdap_common.json#/definitions/link/properties/type",
        "#/links/0/type:wrong enum value");
  }

  /**
   * 7.2.2.2.6.
   */
  @Test
  public void titleNotJsonString() {
    replaceProperty("title", 0);
    validateIsNotAJsonString(-10606, "#/links/0/title:0");
  }

  /**
   * 7.2.2.2.7 string part.
   */
  @Test
  public void hreflangNotJsonString() {
    replaceProperty("hreflang", 0);
    validateIsNotAJsonString(-10607, "#/links/0/hreflang:0");
  }

  /**
   * 7.2.2.2.7 array part.
   */
  @Test
  public void hreflangNotArrayOfString() {
    replaceProperty("hreflang", List.of(0));
    validateIsNotAJsonString(-10607, "#/links/0/hreflang:[0]");
  }

  /**
   * 7.2.2.2.8.
   */
  @Test
  public void hreflangViolatesLanguageTagSyntax() {
    replaceProperty("hreflang", "000");
    validateRegex(-10608,
        "rdap_common.json#/definitions/link/properties/hreflang/oneOf/1/allOf/1",
        "#/links/0/hreflang:000");
  }

  /**
   * 7.2.2.2.9.
   */
  @Test
  public void valueViolatesWebUriValidation() {
    replaceProperty("value", 0);
    validateSubValidation(-10609, "webUriValidation", "#/links/0/value:0");
  }

  /**
   * 7.2.2.2.10.
   */
  @Test
  public void hrefDoesNotExist() {
    jsonObject.getJSONArray("links").getJSONObject(0).remove("href");
    validateKeyMissing(-10610, "href");
  }

  /**
   * 7.2.2.2.11.
   */
  @Test
  public void hrefViolatesWebUriValidation() {
    replaceProperty("href", 0);
    validateSubValidation(-10611, "webUriValidation", "#/links/0/href:0");
  }
}
