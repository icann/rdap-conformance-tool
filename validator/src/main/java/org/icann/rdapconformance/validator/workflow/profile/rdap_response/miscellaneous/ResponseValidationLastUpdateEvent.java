package org.icann.rdapconformance.validator.workflow.profile.rdap_response.miscellaneous;

import com.jayway.jsonpath.DocumentContext;
import java.util.List;
import java.util.Objects;
import org.icann.rdapconformance.validator.EventActions;
import org.icann.rdapconformance.validator.schema.JsonPointers;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class ResponseValidationLastUpdateEvent extends ProfileJsonValidation {

  private final RDAPQueryType queryType;

  public ResponseValidationLastUpdateEvent(String rdapResponse, RDAPValidatorResults results,
      RDAPQueryType queryType) {
    super(rdapResponse, results);
    this.queryType = queryType;
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_3_1_3_and_2_7_6_and_3_3_and_4_4_Validation";
  }

  @Override
  protected boolean doValidate() {
    DocumentContext jpath = getJPath();
    List<String> eventActionsPath = jpath.read("$.events[*].eventAction");
    for (String eventActionPath : eventActionsPath) {
      String jsonPointer = JsonPointers.fromJpath(eventActionPath);
      String eventAction = (String) jsonObject.query(jsonPointer);
      if (Objects.equals(EventActions.LAST_UPDATE_OF_RDAP_DATABASE, eventAction)) {
        return true;
      }
    }

    results.add(RDAPValidationResult.builder()
        .code(-43100)
        .value(jsonObject.get("events").toString())
        .message("An eventAction type last update of RDAP database does not "
            + "exists in the topmost events data structure. See section 2.3.1.3, 2.7.6, 3.3 and "
            + "4.4 of the RDAP_Response_Profile_2_1.")
        .build());
    return false;
  }

  @Override
  public boolean doLaunch() {
    return queryType.isLookupQuery();
  }
}
