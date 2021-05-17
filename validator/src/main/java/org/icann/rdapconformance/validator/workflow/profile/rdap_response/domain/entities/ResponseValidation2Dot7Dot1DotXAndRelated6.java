package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.util.Set;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 8.8.1.6
 */
public class ResponseValidation2Dot7Dot1DotXAndRelated6 extends
    ResponseValidation2Dot7Dot1DotXAndRelated {

  public ResponseValidation2Dot7Dot1DotXAndRelated6(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType,
      RDAPValidatorConfiguration config) {
    super(rdapResponse, results, queryType, config);
  }

  @Override
  protected boolean doValidateEntity(String jsonPointer, JSONObject entity) {
    if (!getPointerFromJPath(entity, "[?(@.roles contains 'registrant')]").isEmpty()) {
      Set<String> adrPointers = getPointerFromJPath(entity, "vcardArray[1][?(@[0] == 'adr')]");
      for (String adrPointer : adrPointers) {
        JSONArray adr = (JSONArray) entity.query(adrPointer);
        if (!adr.getJSONObject(1).has("cc")) {
          results.add(RDAPValidationResult.builder()
              .code(-52105)
              .value(getResultValue(jsonPointer))
              .message("An entity with the registrant role without the CC parameter "
                  + "was found. See section 2.7.4.1 of the RDAP_Response_Profile_2_1.")
              .build());
          return false;
        }
      }
    }

    return true;
  }
}
