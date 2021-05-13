package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import java.util.Set;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ResponseValidationRFC3915Test extends ResponseDomainValidationTestBase {

  public ResponseValidationRFC3915Test() {
    super("rdapResponseProfile_rfc3915_Validation");
  }

  @DataProvider(name = "invalidStatus")
  public static Object[][] invalidStatus() {
    return new Object[][]{{Set.of("redemption period", "any")},
        {Set.of("pending restore", "any")},
        {Set.of("redemption period", "pending delete", "any")},
        {Set.of("pending restore", "pending delete", "any")}
    };
  }

  @DataProvider(name = "validStatus")
  public static Object[][] validStatus() {
    return new Object[][]{{Set.of("redemption period", "pending delete")},
        {Set.of("pending restore", "pending delete")}
    };
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidationRFC3915(jsonObject.toString(), results, queryType);
  }

  @Test(dataProvider = "validStatus")
  public void testValidate_ok(Set<String> status) {
    replaceValue("status", status);
    super.testValidate_ok();
  }

  @Test(dataProvider = "invalidStatus")
  public void testValidate_InvalidStatusCombination_AddResults47000(Set<String> status) {
    replaceValue("status", status);
    validate(-47000, "#/status:[\"" + String.join("\",\"", status) + "\"]",
        "The values of the status data structure does not comply with RFC3915.");
  }
}