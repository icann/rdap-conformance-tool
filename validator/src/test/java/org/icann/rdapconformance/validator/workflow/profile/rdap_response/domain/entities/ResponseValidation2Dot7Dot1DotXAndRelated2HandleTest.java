package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot1DotXAndRelated2HandleTest extends
    ResponseValidation2Dot7Dot1DotXAndRelatedTest {

  @Override
  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    // Load JSON that has entity with all vcard fields but missing handle
    String jsonResponse = getResource(
        "/validators/profile/rdap_response/domain/entities/missing_handle_no_redacted_remark.json");
    jsonObject = new JSONObject(jsonResponse);
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot7Dot1DotXAndRelated2(jsonObject.toString(), results,
        queryType, config);
  }

  @Test
  @Override
  public void testValidate_ok() {
    // This test should fail because our JSON is missing handle field
    // This demonstrates the bug fix is working correctly
    ProfileValidation validation = getProfileValidation();
    assertThat(validation.validate()).isFalse();
  }

  @Test
  public void withoutHandleWithoutRedactedForPrivacyTitle() {
    // JSON resource already has entity missing handle field - this should trigger -52101 error
    validate(-52101, "#/entities/0:" + jsonObject.query("#/entities/0"),
            "An entity without a remark titled \"REDACTED FOR PRIVACY\" " +
                    "does not have all the necessary information of handle, fn, adr, tel, street and city.");
  }
}