package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

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

  @Test
  public void mediaNotInEnum() {
    replaceProperty("media", "wrong enum value");
    validateNotEnum(-10603, "rdap_common.json#/definitions/link/properties/media/allOf/1",
        "#/links/0/media:wrong enum value");
  }

  @Test
  public void relNotInEnum() {
    replaceProperty("rel", "wrong enum value");
    validateNotEnum(-10604, "rdap_common.json#/definitions/link/properties/rel",
        "#/links/0/rel:wrong enum value");
  }

  /**
   * TODO when dataset handling will be done.
   */
  @Ignore
  @Test
  public void typeNotInEnum() {
    replaceProperty("type", "wrong enum value");
    validateNotEnum(-10605, "rdap_common.json#/definitions/link/properties/type",
        "#/links/0/type:wrong enum value");
  }

  @Test
  public void titleNotJsonString() {
    replaceProperty("title", 0);
    validateIsNotAJsonString(-10606, "#/links/0/title:0");
  }

  @Test
  public void hreflangNotJsonString() {
    replaceProperty("hreflang", 0);
    validateIsNotAJsonString(-10607, "#/links/0/hreflang:0");
  }

  @Test
  public void hreflangNotArrayOfString() {
    replaceProperty("hreflang", List.of(0));
    validateIsNotAJsonString(-10607, "#/links/0/hreflang:[0]");
  }

  @Test
  public void hreflangViolatesLanguageTagSyntax() {
    replaceProperty("hreflang", "000");
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(context.getResults())
        .filteredOn("code", -10608)
        .last()
        .hasFieldOrPropertyWithValue("value", "#/links/0/hreflang:000")
        .hasFieldOrPropertyWithValue("message",
            "The value of the JSON string data in the #/links/0/hreflang does not conform to rdap_common.json#/definitions/link/properties/hreflang/oneOf/1 syntax.");
  }
}
