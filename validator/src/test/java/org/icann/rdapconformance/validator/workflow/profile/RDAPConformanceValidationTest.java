package org.icann.rdapconformance.validator.workflow.profile;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import java.util.List;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class RDAPConformanceValidationTest extends
    ProfileJsonValidationTestBase {

  protected RDAPConformanceValidationTest(String testGroupName) {
    super("/validators/profile/rdapConformance/valid.json", testGroupName);
  }

  @Override
  public abstract RDAPConformanceValidation getTigValidation();

  @Test
  public void testValidate_RDAPConformanceDoesNotContainsValue_AddErrorCode() {
    jsonObject.put("rdapConformance", List.of("rdap_level_0"));
    validate(getTigValidation().code, "#/rdapConformance:[\"rdap_level_0\"]",
        getTigValidation().message);
  }
}