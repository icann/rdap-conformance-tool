package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorRdapConformanceTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.testng.annotations.Test;

public class Validation1Dot14Test extends SchemaValidatorRdapConformanceTest {


  @Test
  public void testValidate_ok() {
    Validation1Dot14 validation1Dot14 = new Validation1Dot14(jsonObject.toString(), datasets,
        results);
    assertThat(validation1Dot14.validate()).isTrue();
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
    assertThat(validation1Dot14.validate()).isFalse();
    assertThat(results.getAll())
        .contains(RDAPValidationResult.builder()
            .code(-20600)
            .value("#/rdapConformance:[\"rdap_level_0\"]")
            .message("The RDAP Conformance data structure does not include "
                + "icann_rdap_technical_implementation_guide_0. See section 1.14 of the "
                + "RDAP_Technical_Implementation_Guide_2_1.")
            .build());
  }
}
