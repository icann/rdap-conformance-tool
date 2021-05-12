package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import java.util.Set;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ResponseValidationRFC5731Test extends ResponseDomainValidationTestBase {

  public ResponseValidationRFC5731Test() {
    super("rdapResponseProfile_rfc5731_Validation");
  }

  @DataProvider(name = "invalidStatus")
  public static Object[][] invalidStatus() {
    return new Object[][]{{Set.of("active", "any")},
        {Set.of("pending delete", "client delete prohibited")},
        {Set.of("pending delete", "server delete prohibited")},
        {Set.of("pending renew", "client renew prohibited")},
        {Set.of("pending renew", "server renew prohibited")},
        {Set.of("pending transfer", "client transfer prohibited")},
        {Set.of("pending transfer", "server transfer prohibited")},
        {Set.of("pending update", "client update prohibited")},
        {Set.of("pending update", "server update prohibited")},
        {Set.of("pending create", "pending delete")},
        {Set.of("pending create", "pending renew")},
        {Set.of("pending create", "pending transfer")},
        {Set.of("pending create", "pending update")},
        {Set.of("pending delete", "pending renew")},
        {Set.of("pending delete", "pending transfer")},
        {Set.of("pending delete", "pending update")},
        {Set.of("pending renew", "pending transfer")},
        {Set.of("pending renew", "pending update")},
        {Set.of("pending transfer", "pending update")},
    };
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidationRFC5731(jsonObject.toString(), results, queryType);
  }

  @Test(dataProvider = "invalidStatus")
  public void testValidate_InvalidStatusCombination_AddResults46900(Set<String> status) {
    replaceValue("status", status);
    validate(-46900, "#/status:[\"" + String.join("\",\"", status) + "\"]",
        "The values of the status data structure does not comply with RFC5731.");
  }
}