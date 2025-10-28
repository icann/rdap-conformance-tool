package org.icann.rdapconformance.validator.workflow.profile;

import java.util.List;
import org.testng.annotations.Test;

public abstract class RDAPConformanceValidationTest extends ProfileJsonValidationTestBase {

  protected RDAPConformanceValidationTest(String testGroupName) {
    super("/validators/profile/rdapConformance/valid.json", testGroupName);
  }

  public abstract RDAPConformanceValidation getProfileValidation();

  @Test
  public void testValidate_RDAPConformanceDoesNotContainsValue_AddErrorCode() {
    jsonObject.put("rdapConformance", List.of("rdap_level_0"));
    validate(getProfileValidation().code, "#/rdapConformance:[\"rdap_level_0\"]",
        getProfileValidation().message);
  }
}