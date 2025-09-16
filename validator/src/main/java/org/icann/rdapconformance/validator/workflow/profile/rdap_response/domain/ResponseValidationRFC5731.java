package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import java.util.HashSet;
import java.util.Set;
import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidationRFC5731 extends ProfileJsonValidation {

  private final RDAPQueryType queryType;

  public ResponseValidationRFC5731(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType) {
    super(rdapResponse, results);
    this.queryType = queryType;
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_rfc5731_Validation";
  }

  @Override
  protected boolean doValidate() {
    Set<String> status = new HashSet<>();
    jsonObject.optJSONArray("status").forEach(s -> status.add((String) s));

    if ((status.contains("active") && status.stream().anyMatch(s -> !Set.of("active", "add period", "auto renew period", "renew period", "transfer period").contains(s))) ||
        (status.containsAll(Set.of("pending delete", "client delete prohibited")) ||
            status.containsAll(Set.of("pending delete", "server delete prohibited"))) ||
        (status.containsAll(Set.of("pending renew", "client renew prohibited")) ||
            status.containsAll(Set.of("pending renew", "server renew prohibited"))) ||
        (status.containsAll(Set.of("pending transfer", "client transfer prohibited")) ||
            status.containsAll(Set.of("pending transfer", "server transfer prohibited"))) ||
        (status.containsAll(Set.of("pending update", "client update prohibited")) ||
            status.containsAll(Set.of("pending update", "server update prohibited"))) ||
        (status.stream().filter(s -> Set
            .of("pending create", "pending delete", "pending renew", "pending transfer",
                "pending update").contains(s))
            .count() > CommonUtils.ONE)) {
      results.add(RDAPValidationResult.builder()
          .code(-46900)
          .value(getResultValue("#/status"))
          .message("The values of the status data structure does not comply with RFC5731.")
          .build());
      return false;
    }
    return true;
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
