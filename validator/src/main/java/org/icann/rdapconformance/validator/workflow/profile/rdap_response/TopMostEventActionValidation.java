package org.icann.rdapconformance.validator.workflow.profile.rdap_response;

import com.jayway.jsonpath.DocumentContext;
import java.util.List;
import java.util.Objects;
import org.icann.rdapconformance.validator.EventAction;
import org.icann.rdapconformance.validator.schema.JsonPointers;
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
    DocumentContext jpath = getJPath();
    List<String> eventActionsPath = jpath.read("$.events[*].eventAction");
    for (String eventActionPath : eventActionsPath) {
      String jsonPointer = JsonPointers.fromJpath(eventActionPath);
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
