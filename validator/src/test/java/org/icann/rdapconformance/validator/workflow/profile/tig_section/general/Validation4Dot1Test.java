package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class Validation4Dot1Test extends TigValidationTestBase {

  public Validation4Dot1Test() {
    super("rdap_domain.json",
        "/validators/domain/valid.json",
        "tigSection_4_1_Validation");
  }


  @Test
  public void testValidate_ok() {
    Validation4Dot1 validation4Dot1 =
        new Validation4Dot1(jsonObject.toString(), results);
    testValidate_ok(validation4Dot1);
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
    Validation4Dot1 validation4Dot1 =
        new Validation4Dot1(jsonObject.toString(), results);
    validate(validation4Dot1, -20800, "#/entities/0/vcardArray/1/2:[\"adr\",{},\"text\",[0]]",
        "An entity with a non-structured address was found. See section 4.1 of the TIG.");
  }
}
