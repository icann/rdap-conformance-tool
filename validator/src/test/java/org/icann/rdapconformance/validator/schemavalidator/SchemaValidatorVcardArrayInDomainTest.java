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
}