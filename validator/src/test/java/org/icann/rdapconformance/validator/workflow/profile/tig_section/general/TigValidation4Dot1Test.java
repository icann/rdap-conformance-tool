package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.util.List;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.TigValidationFromSchemaTestBase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class TigValidation4Dot1Test extends TigValidationFromSchemaTestBase {

  public TigValidation4Dot1Test() {
    super("rdap_domain.json",
        "/validators/domain/valid.json",
        "tigSection_4_1_Validation");
  }

  @Override
  public ProfileJsonValidation getTigValidation() {
    return new TigValidation4Dot1(jsonObject.toString(), results);
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
    validate(-20800, "#/entities/0/vcardArray/1/2:[\"adr\",{},\"text\",[0]]",
        "An entity with a non-structured address was found. See section 4.1 of the TIG.");
  }
}
