package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import java.util.Set;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ResponseValidationRFC5731Test extends ResponseDomainValidationTestBase {

  public ResponseValidationRFC5731Test() {
    super("rdapResponseProfile_rfc5731_Validation");
  }

  @DataProvider(name = "invalidStatus")
  public static Object[][] invalidStatus() {
    return new Object[][]{{Set.of("active", "client delete prohibited")},
        {Set.of("active", "pending delete")},
        {Set.of("active", "add period", "pending delete")},
        {Set.of("active", "inactive")},
        {Set.of("active", "server hold")},
        {Set.of("active", "redemption period")},
        {Set.of("active", "add period", "server hold")},
        {Set.of("active", "server delete prohibited")},
        {Set.of("active", "pending restore")},
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
    QueryContext domainContext = new QueryContext(
        queryContext.getQueryId(),
        queryContext.getConfig(),
        queryContext.getDatasetService(),
        queryContext.getQuery(),
        queryContext.getResults(),
        RDAPQueryType.DOMAIN
    );
    domainContext.setRdapResponseData(queryContext.getRdapResponseData());
    return new ResponseValidationRFC5731(domainContext);
  }

  @DataProvider(name = "validActiveStatus")
  public static Object[][] validActiveStatus() {
    return new Object[][]{{Set.of("active")},
        {Set.of("active", "add period")},
        {Set.of("active", "auto renew period")},
        {Set.of("active", "renew period")},
        {Set.of("active", "transfer period")},
        {Set.of("active", "add period", "renew period")},
        {Set.of("active", "add period", "auto renew period", "renew period", "transfer period")},
    };
  }

  @Test(dataProvider = "invalidStatus")
  public void testValidate_InvalidStatusCombination_AddResults46900(Set<String> status) {
    replaceValue("status", status);
    validate(-46900, "#/status:[\"" + String.join("\",\"", status) + "\"]",
        "The values of the status data structure does not comply with RFC5731.");
  }

  @Test(dataProvider = "validActiveStatus")
  public void testValidate_ValidActiveStatusCombination_ShouldPass(Set<String> status) {
    replaceValue("status", status);
    validate();
  }
}