package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.testng.annotations.Test;

public class TigValidation3Dot3And3Dot4Test extends ProfileJsonValidationTestBase {

  public TigValidation3Dot3And3Dot4Test() {
    super(
        "/validators/profile/tig_section/notices/valid.json",
        "tigSection_3_3_and_3_4_Validation");
  }

  @Override
  public ProfileJsonValidation getProfileValidation() {
    return new TigValidation3Dot3And3Dot4(
        jsonObject.toString(),
        results,
        new SchemaValidator("test_rdap_notices.json", results, config, datasets));
  }

  /**
   * 8.1.7.
   */
  @Test
  public void tigSection_3_3_and_3_4_Validation() {
    jsonObject.getJSONArray("notices").getJSONObject(0).remove("links");
    validate(-20700, "#/notices:" + jsonObject.getJSONArray("notices"),
        "A links object was not found in the "
            + "notices object in the "
            + "topmost object. See section 3.3 and 3.4 of the "
            + "RDAP_Technical_Implementation_Guide_2_1.");
  }
}
