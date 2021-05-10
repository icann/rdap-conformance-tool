package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.testng.annotations.Test;

public class ResponseValidation1Dot2Dot2Test extends ProfileJsonValidationTestBase {


  public ResponseValidation1Dot2Dot2Test() {
    super("/validators/domain/valid.json", "rdapResponseProfile_1_2_2_Validation");
  }

  @Override
  public ProfileJsonValidation getTigValidation() {
    return new ResponseValidation1Dot2Dot2(rdapContent, results);
  }

  @Test
  public void testValidate_ContainsJs_AddResults40100() {
    rdapContent = "{\"objectClassName\":\"domain\",\"handle\":\"2138514_DOMAIN_COM-EXMP\","
        + "\"ldhName\": \"<script>var val = 'test';</script>\"}";
    validate(-40100, rdapContent,
        "The RDAP response contains browser executable code (e.g., JavaScript). "
            + "See section 1.2.2 of the RDAP_Response_Profile_2_1.");
  }
}