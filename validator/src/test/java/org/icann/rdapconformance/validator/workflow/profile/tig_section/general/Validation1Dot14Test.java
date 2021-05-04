package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.util.List;
import org.testng.annotations.Test;

public class Validation1Dot14Test extends TigValidationTestBase {


  public Validation1Dot14Test() {
    super("test_rdap_conformance.json",
        "/validators/rdapConformance/valid.json",
        "tigSection_1_14_Validation");
  }

  @Test
  public void testValidate_ok() {
    testValidate_ok(
        new Validation1Dot14(
            jsonObject.toString(),
            datasets,
            results)
    );
  }

  /**
   * 8.1.6.
   */
  @Test
  public void tigSection_1_14_Validation() {
    List<String> listWithOnlyRdapLevel0 = List.of("rdap_level_0");
    jsonObject.put("rdapConformance", listWithOnlyRdapLevel0);
    Validation1Dot14 validation1Dot14 = new Validation1Dot14(jsonObject.toString(), datasets,
        results);
    validate(
        validation1Dot14,
        -20600,
        "#/rdapConformance:[\"rdap_level_0\"]",
        "The RDAP Conformance data structure does not include "
            + "icann_rdap_technical_implementation_guide_0. See section 1.14 of the "
            + "RDAP_Technical_Implementation_Guide_2_1."
    );
  }
}
