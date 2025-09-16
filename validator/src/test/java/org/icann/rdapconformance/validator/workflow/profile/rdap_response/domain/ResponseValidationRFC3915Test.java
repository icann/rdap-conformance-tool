package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import java.util.Set;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ResponseValidationRFC3915Test extends ResponseDomainValidationTestBase {

  public ResponseValidationRFC3915Test() {
    super("rdapResponseProfile_rfc3915_Validation");
  }

  @DataProvider(name = "invalidStatusRedemption")
  public static Object[][] invalidStatusRedemption() {
    return new Object[][]{
        {Set.of("redemption period", "any")}
    };
  }

  @DataProvider(name = "invalidStatusPendingRestore")
  public static Object[][] invalidStatusPendingRestore() {
    return new Object[][]{
            {Set.of("pending restore", "any")}
    };
  }

  @DataProvider(name = "validStatus")
  public static Object[][] validStatus() {
    return new Object[][]{{Set.of("redemption period", "any", "pending delete")},
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

  @Test(dataProvider = "invalidStatusRedemption")
  public void testValidate_InvalidStatusCombination_AddResults47001(Set<String> status) {
    replaceValue("status", status);
    validate(-47001, "#/status:[\"" + String.join("\",\"", status) + "\"]",
        "'redemption period' is only valid with a status of 'pending delete'");
  }

  @Test(dataProvider = "invalidStatusPendingRestore")
  public void testValidate_InvalidStatusCombination_AddResults47002(Set<String> status) {
    replaceValue("status", status);
    validate(-47002, "#/status:[\"" + String.join("\",\"", status) + "\"]",
            "'pending restore' is only valid with a status of 'pending delete'");
  }
}