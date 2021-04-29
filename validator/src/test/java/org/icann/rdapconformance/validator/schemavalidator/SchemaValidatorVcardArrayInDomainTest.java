package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class SchemaValidatorVcardArrayInDomainTest extends SchemaValidatorDomainTest {

  @Test
  public void testVcardWrongCategory() throws IOException {
    JSONArray vcardArrayWithWrongCategory = new JSONObject(getResource(
        "/validators/vcardArray/wrongCategory.json")).getJSONArray("vcardArray");
    jsonObject
        .getJSONArray("entities")
        .getJSONObject(0)
        .put("vcardArray", vcardArrayWithWrongCategory);
    // #/entities/0/vcardArray/1/1/0:
    validateWithoutGroupTests(-12305,
        "#/entities/0/vcardArray/1/3:wrong-category",
        "unknown vcard category: \"wrong-category\".");
    assertThat(results.getAll())
        .filteredOn("value", "version")
        .isEmpty();
  }

  /**
   * 8.1.8
   */
  @Test
  public void tigSection_4_1_Validation() {
    List<Object> addressArray = List.of(
        "adr",
        new JSONObject(),
        "text",
        List.of(0));
    JSONArray vcardArray =
        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray")
            .getJSONArray(1);
    vcardArray.put(2, addressArray);
    validateWithoutGroupTests(-20800, "#/entities/0/vcardArray/1/2:[\"adr\",{},\"text\",[0]]",
        "An entity with a non-structured address was found. See section 4.1 of the TIG.");
  }

  /**
   * 8.1.9
   */
  @Test
  public void tigSection_7_1_and_7_2_Validation() {
    // replace the type == voice/fax valid with a wrong value:
    Map<String, String> wrongType = Map.of("type", "not-voice-nor-fax");
    jsonObject = new JSONObject(JsonPath
        .parse(jsonObject.toString())
        .set("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", wrongType)
        .jsonString());
    validateWithoutGroupTests(-20900,
        "#/entities/0/entities/0/vcardArray/1/2:{\"type\":\"not-voice-nor-fax\"}",
        "An entity with a tel property without a voice or fax "
            + "type was found. See section 7.1 and 7.2 of the TIG.");
  }
}