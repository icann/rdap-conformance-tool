package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.util.List;
import org.icann.rdapconformance.validator.schema.JsonPointers;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Ignore;
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
    validateWithoutGroupTests(-999, "wrong-category", "unknown vcard category: \"wrong-category\".");
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
        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
    vcardArray.put(2, addressArray);
    validateWithoutGroupTests(-20800, "#/entities/0/vcardArray/1/2:[\"adr\",{},\"text\",[0]]",
        "An entity with a non-structured address was found. See section 4.1 of the TIG.");
  }
}