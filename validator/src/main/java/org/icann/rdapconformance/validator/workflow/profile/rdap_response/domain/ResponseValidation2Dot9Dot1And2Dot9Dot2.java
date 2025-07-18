package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.json.JSONObject.NULL;

import java.util.HashSet;
import java.util.Set;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.HandleValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public final class ResponseValidation2Dot9Dot1And2Dot9Dot2 extends HandleValidation {

  private final RDAPValidatorConfiguration config;

  public ResponseValidation2Dot9Dot1And2Dot9Dot2(RDAPValidatorConfiguration config,
      String rdapResponse,
      RDAPValidatorResults results,
      RDAPDatasetService datasetService,
      RDAPQueryType queryType) {
    super(config, rdapResponse, results, datasetService, queryType, -47201, "nameserver");
    this.config = config;
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_9_1_and_2_9_2_Validation";
  }

  @Override
  protected boolean doValidate() {
    boolean isValid = true;
    Set<String> nsWithoutStatus = new HashSet<>();
    boolean oneWithStatus = false;
    Set<String> nsWithoutHandle = new HashSet<>();
    boolean oneWithHandle = false;
    Set<String> jsonPointers = getPointerFromJPath("$.nameservers[*]");

    for (String jsonPointer : jsonPointers) {
      JSONObject nameserver = (JSONObject) jsonObject.query(jsonPointer);
      if (NULL.equals(nameserver.opt("handle"))) {
        nsWithoutHandle.add(jsonPointer);
      } else {
        oneWithHandle = true;
      }
      if (NULL.equals(nameserver.opt("status"))) {
        nsWithoutStatus.add(jsonPointer);
      } else {
        oneWithStatus = true;
      }
      isValid &= checkLdhName(jsonPointer);
      isValid &= checkHandles(jsonPointer);
      isValid &= checkStatuses(jsonPointer);
    }

    if ((oneWithHandle && !nsWithoutHandle.isEmpty()) || (oneWithStatus && !nsWithoutStatus
        .isEmpty())) {
      nsWithoutHandle.addAll(nsWithoutStatus);
      nsWithoutHandle.forEach(jsonPointer ->
          results.add(RDAPValidationResult.builder()
              .code(-47203)
              .value(getResultValue(jsonPointer))
              .message("The handle or status in the nameserver object is not included.")
              .build()));
      isValid = false;
    }

    return isValid;
  }

  private boolean checkLdhName(String nameserverJsonPointer) {
    JSONObject nameserver = (JSONObject) jsonObject.query(nameserverJsonPointer);
    if (NULL.equals(nameserver.opt("ldhName"))) {
      results.add(RDAPValidationResult.builder()
          .code(-47200)
          .value(getResultValue(nameserverJsonPointer))
          .message("A nameserver object without ldhName was found.")
          .build());
      return false;
    }
    return true;
  }

  private boolean checkHandles(String nameserverJsonPointer) {
    boolean isValid = true;

    JSONObject nameserver = (JSONObject) jsonObject.query(nameserverJsonPointer);
    Set<String> jsonPointers = getPointerFromJPath(nameserver, "$.handle");
    for (String jsonPointer : jsonPointers) {
      isValid &= validateHandle(nameserverJsonPointer.concat(jsonPointer.substring(1)));
    }
    return isValid;
  }

  private boolean checkStatuses(String nameserverJsonPointer) {
    boolean isValid = true;

    JSONObject nameserver = (JSONObject) jsonObject.query(nameserverJsonPointer);
    Set<String> jsonPointers = getPointerFromJPath(nameserver, "$.status");
    for (String jsonPointer : jsonPointers) {
      isValid &= validateStatus(nameserverJsonPointer.concat(jsonPointer.substring(1)));
    }
    return isValid;
  }

  private boolean validateStatus(String statusJsonPointer) {
    Set<String> status = new HashSet<>();
    ((JSONArray) jsonObject.query(statusJsonPointer)).forEach(s -> status.add((String) s));

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
          .code(-47204)
          .value(getResultValue(statusJsonPointer))
          .message("The values of the status data structure does not comply with RFC5732.")
          .build());
      return false;
    }
    return true;
  }

  @Override
  public boolean doLaunch() {
     return config.isGtldRegistrar() && queryType.equals(RDAPQueryType.DOMAIN);
  }
}
