package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.testng.annotations.Test;

public class ResponseValidation1Dot2Dot2Test extends ProfileJsonValidationTestBase {


  public ResponseValidation1Dot2Dot2Test() {
    super("/validators/domain/valid.json", "rdapResponseProfile_1_2_2_Validation");
  }

  @Override
  public ProfileJsonValidation getProfileValidation() {
    return new ResponseValidation1Dot2Dot2(jsonObject.toString(), results);
  }

  @Test
  public void testValidate_ContainsJs_AddResults40100() {
    replaceValue("ldhName", "<script>var val = 'test';</script>");
    validate(-40100, jsonObject.toString(),
        "The RDAP response contains browser executable code (e.g., JavaScript). "
            + "See section 1.2.2 of the RDAP_Response_Profile_2_1.");
  }

  @Test
  public void testValidate_ContainsHTMLNotJS_NotAddResults40100() {
    replaceValue("ldhName", "<b>var val = 'test';</b>");
  }

  @Test
  public void testValidate_ContainsJsCaseInsensitiveAndHTML_AddResults40100() {
    replaceValue("ldhName", "<ScriPt>var val = 'test';</ScriPt>");
    replaceValue("status", "<b>var val2 = 'test2';</b>");
    replaceValue("objectClassName", "<script>var val3 = 'test3';</script>");
    validate(-40100, jsonObject.toString(),
            "The RDAP response contains browser executable code (e.g., JavaScript). "
                    + "See section 1.2.2 of the RDAP_Response_Profile_2_1.");
  }
}