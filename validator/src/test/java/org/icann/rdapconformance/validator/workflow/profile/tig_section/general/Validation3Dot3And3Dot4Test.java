package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorNoticesTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.testng.annotations.Test;

public class Validation3Dot3And3Dot4Test extends SchemaValidatorNoticesTest {

  @Test
  public void testValidate_ok() {
    Validation3Dot3And3Dot4 validation3Dot3And3Dot4 =
        new Validation3Dot3And3Dot4(jsonObject.toString(), results, schemaValidator);
    assertThat(validation3Dot3And3Dot4.validate()).isTrue();
    assertThat(results.getGroupOk()).containsExactly("tigSection_3_3_and_3_4_Validation");
  }

  /**
   * 8.1.7.
   */
  @Test
  public void tigSection_3_3_and_3_4_Validation() {
    removeKey("links");
    Validation3Dot3And3Dot4 validation3Dot3And3Dot4 =
        new Validation3Dot3And3Dot4(jsonObject.toString(), results, schemaValidator);
    validation3Dot3And3Dot4.validate();
    assertThat(results.getAll())
        .contains(RDAPValidationResult.builder()
            .code(-20700)
            .value(jsonObject.getJSONArray("notices").toString())
            .message("A links object was not found in the "
                + "notices object in the "
                + "topmost object. See section 3.3 and 3.4 of the "
                + "RDAP_Technical_Implementation_Guide_2_1.")
            .build());
    assertThat(results.getGroupErrorWarning()).containsExactly("tigSection_3_3_and_3_4_Validation");
  }
}
