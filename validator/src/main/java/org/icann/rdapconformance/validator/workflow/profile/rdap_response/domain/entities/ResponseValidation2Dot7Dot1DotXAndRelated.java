package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.util.Set;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class ResponseValidation2Dot7Dot1DotXAndRelated extends
    EntitiesWithinDomainProfileJsonValidation {

  public ResponseValidation2Dot7Dot1DotXAndRelated(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType,
      RDAPValidatorConfiguration config) {
    super(rdapResponse, results, queryType, config);
  }

  @Override
  protected boolean doValidateEntity(String jsonPointer, JSONObject entity) {
    Set<String> withRemarkTitleRedactedForPrivacy =
        getPointerFromJPath(entity, "$.remarks[?(@.title == 'REDACTED FOR PRIVACY')]");

    // 8.8.1.1:
    for (String remarkJsonPointer : withRemarkTitleRedactedForPrivacy) {
      JSONObject remark = (JSONObject) entity.query(remarkJsonPointer);
      if (!remark.has("type") || !remark.get("type").equals("object redacted due to "
          + "authorization")) {
        results.add(RDAPValidationResult.builder()
            .code(-52100)
            .value(getResultValue(jsonPointer))
            .message("An entity with the registrant, administrative, technical or "
                + "billing role with a remarks members with the title \"REDACTED FOR PRIVACY\" was "
                + "found, but the description and type does not contain the value in 2.7.4.3 of "
                + "the "
                + "RDAP_Response_Profile_2_1.")
            .build());
        return false;
      }
    }

    boolean isValid = true;
    if (withRemarkTitleRedactedForPrivacy.isEmpty()) {
      // 8.8.1.2:
      Set<String> properties = Set.of("fn", "adr", "tel", "email");
      for (String property : properties) {
        isValid &= validateVcardProperty(jsonPointer, entity, property);
      }
    }

    return isValid;
  }

  private boolean validateVcardProperty(String jsonPointer, JSONObject entity, String property) {
    Set<String> propertyPointers = getVcardPropertyPointers(entity, property);
    boolean isValid = true;
    if (propertyPointers.isEmpty()) {
      isValid &= log52101(jsonPointer);
    }
    return isValid;
  }

  private Set<String> getVcardPropertyPointers(JSONObject entity, String property) {
    return getPointerFromJPath(entity, "vcardArray[1][*][?(@ == '"+property+"')]");
  }

  private boolean log52101(String jsonPointer) {
    results.add(RDAPValidationResult.builder()
        .code(-52101)
        .value(getResultValue(jsonPointer))
        .message("An entity with the registrant, administrative, technical or "
            + "billing role with a remarks members with the title \"REDACTED FOR PRIVACY\" was "
            + "found, but the description and type does not contain the value in 2.7.4.3 of the"
            + " RDAP_Response_Profile_2_1.")
        .build());
    return false;
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_7_1_X_and_2_7_2_X_and_2_7_3_X_and_2_7_4_X_Validation";
  }

}
