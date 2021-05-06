package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import org.icann.rdapconformance.validator.workflow.profile.tig_section.TigJsonValidation;
import org.testng.annotations.Test;

public class Validation3Dot3And3Dot4Test extends TigValidationFromSchemaTestBase {

  public Validation3Dot3And3Dot4Test() {
    super("test_rdap_notices.json",
        "/validators/notices/valid.json",
        "tigSection_3_3_and_3_4_Validation");
  }

  @Override
  public TigJsonValidation getTigValidation() {
    return new Validation3Dot3And3Dot4(jsonObject.toString(), results, schemaValidator);
  }

  /**
   * 8.1.7.
   */
  @Test
  public void tigSection_3_3_and_3_4_Validation() {
    jsonObject.getJSONArray("notices").getJSONObject(0).remove("links");
    validate(-20700, jsonObject.getJSONArray("notices").toString(),
        "A links object was not found in the "
            + "notices object in the "
            + "topmost object. See section 3.3 and 3.4 of the "
            + "RDAP_Technical_Implementation_Guide_2_1.");
  }
}
