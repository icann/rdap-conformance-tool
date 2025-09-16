package org.icann.rdapconformance.validator.workflow.profile.rdap_response;

import java.util.HashSet;
import java.util.Set;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;

public abstract class NameserverStatusValidation extends ProfileJsonValidation {

  protected final RDAPQueryType queryType;
  final int code;

  public NameserverStatusValidation(String rdapResponse,
                                    RDAPValidatorResults results, RDAPValidatorConfiguration config, RDAPQueryType queryType, int code) {
    super(rdapResponse, results, config);
    this.queryType = queryType;
    this.code = code;
  }

  public boolean validateStatus(String statusJsonPointer) {
    Set<String> status = new HashSet<>();
    JSONArray statusJson = (JSONArray) jsonObject.query(statusJsonPointer);
    if (null == statusJson) {
      return true;
    }
    statusJson.forEach(s -> status.add((String) s));

    status.remove("associated");
    if ((status.contains("active") && status.size() > 1) ||
        (status.containsAll(Set.of("pending delete", "client delete prohibited")) ||
            status.containsAll(Set.of("pending delete", "server delete prohibited"))) ||
        (status.containsAll(Set.of("pending update", "client update prohibited")) ||
            status.containsAll(Set.of("pending update", "server update prohibited"))) ||
        (status.stream().filter(s -> Set
            .of("pending create", "pending delete", "pending renew", "pending transfer",
                "pending update").contains(s))
            .count() > 1)) {
      results.add(RDAPValidationResult.builder()
          .code(code)
          .value(getResultValue(statusJsonPointer))
          .message("The values of the status data structure does not comply with RFC5732.")
          .build());
      return false;
    }
    return true;
  }

}
