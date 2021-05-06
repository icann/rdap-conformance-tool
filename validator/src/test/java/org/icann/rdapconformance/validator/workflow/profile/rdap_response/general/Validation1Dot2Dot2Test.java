package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidationTestBase;
import org.testng.annotations.Test;

public class Validation1Dot2Dot2Test extends ProfileValidationTestBase {


  @Override
  @Test
  public void testValidate() throws IOException {
    String rdapResponse = getResource("/validators/domain/valid.json");

    Validation1Dot2Dot2 validation = new Validation1Dot2Dot2(rdapResponse, results);

    validateOk(validation);
  }

  @Test
  public void testValidate_ContainsJs_AddResults40100() {
    String rdapResponse = "{\"objectClassName\":\"domain\",\"handle\":\"2138514_DOMAIN_COM-EXMP\","
        + "\"ldhName\": \"<script>var val = 'test';</script>\"}";

    Validation1Dot2Dot2 validation = new Validation1Dot2Dot2(rdapResponse, results);

    validateNotOk(validation, -40100, rdapResponse,
        "The RDAP response contains browser executable code (e.g., JavaScript). "
            + "See section 1.2.2 of the RDAP_Response_Profile_2_1.");
  }
}