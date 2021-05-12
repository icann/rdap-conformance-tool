package org.icann.rdapconformance.validator.workflow.profile.rdap_response;

import java.util.Objects;
import java.util.Set;
import org.icann.rdapconformance.validator.EventAction;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public abstract class TopMostEventActionValidation extends ProfileJsonValidation {

  protected final RDAPQueryType queryType;
  final int code;
  final String message;
  private final EventAction eventAction;

  public TopMostEventActionValidation(String rdapResponse, RDAPValidatorResults results,
      RDAPQueryType queryType, int code, String message, EventAction eventAction) {
    super(rdapResponse, results);
    this.queryType = queryType;
    this.eventAction = eventAction;
    this.code = code;
    this.message = message;
  }

  @Override
  protected boolean doValidate() {
    Set<String> eventActionsPath = getPointerFromJPath("$.events[*].eventAction");
    for (String jsonPointer : eventActionsPath) {
      String eventAction = (String) jsonObject.query(jsonPointer);
      if (Objects.equals(this.eventAction.type, eventAction)) {
        return true;
      }
    }

    results.add(RDAPValidationResult.builder()
        .code(code)
        .value(jsonObject.get("events").toString())
        .message(message)
        .build());
    return false;
  }
}
