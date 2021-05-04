package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import org.testng.annotations.Test;

public class Validation3Dot3And3Dot4Test extends TigValidationTestBase {

  public Validation3Dot3And3Dot4Test() {
    super("test_rdap_notices.json",
        "/validators/notices/valid.json",
        "tigSection_3_3_and_3_4_Validation");
  }


  @Test
  public void testValidate_ok() {
    Validation3Dot3And3Dot4 validation3Dot3And3Dot4 =
        new Validation3Dot3And3Dot4(jsonObject.toString(), results, schemaValidator);
    testValidate_ok(validation3Dot3And3Dot4);
  }

  /**
   * 8.1.7.
   */
  @Test
  public void tigSection_3_3_and_3_4_Validation() {
    jsonObject.getJSONArray("notices").getJSONObject(0).remove("links");
    Validation3Dot3And3Dot4 validation3Dot3And3Dot4 =
        new Validation3Dot3And3Dot4(jsonObject.toString(), results, schemaValidator);
    validate(validation3Dot3And3Dot4, -20700, jsonObject.getJSONArray("notices").toString(),
        "A links object was not found in the "
            + "notices object in the "
            + "topmost object. See section 3.3 and 3.4 of the "
            + "RDAP_Technical_Implementation_Guide_2_1.");
  }
}
