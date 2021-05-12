package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.util.Objects;
import java.util.Set;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public class ResponseValidation2Dot7Dot5Dot3 extends ProfileJsonValidation {

  public ResponseValidation2Dot7Dot5Dot3(String rdapResponse, RDAPValidatorResults results) {
    super(rdapResponse, results);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_7_5_3_Validation";
  }

  @Override
  protected boolean doValidate() {
    Set<String> entityJsonPointers = getPointerFromJPath("$..entities[?("
        + "@.roles contains 'registrant' || "
        + "@.roles contains 'administrative' || "
        + "@.roles contains 'technical' || "
        + "@.roles contains 'billing'"
        + ")]");

    boolean isValid = true;
    for (String jsonPointer : entityJsonPointers) {
      JSONObject entity = (JSONObject) jsonObject.query(jsonPointer);
      boolean emailOmitted = isEmailOmitted(entity);
      if (emailOmitted &&
          (getPointerFromJPath(entity, "$.remarks[?(@.title == 'EMAIL REDACTED FOR PRIVACY')]")
              .isEmpty() ||
              getPointerFromJPath(entity,
                  "$.remarks[?(@.type == 'object redacted due to authorization')]").isEmpty())
      ) {
        isValid = false;
        results.add(RDAPValidationResult.builder()
            .code(-55000)
            .value(jsonPointer + ":" + entity)
            .message("An entity with the administrative, technical, or billing role "
                + "without a valid \"EMAIL REDACTED FOR PRIVACY\" remark was found. See section 2.7.5.3 "
                + "of the RDAP_Response_Profile_2_1.")
            .build());
      }
    }
    return isValid;
  }

  private boolean isEmailOmitted(JSONObject entity) {
    JSONArray vcardArray = entity.getJSONArray("vcardArray");
    for (Object vcardElement : vcardArray) {
      if (vcardElement instanceof JSONArray) {
        JSONArray vcardElementArray = (JSONArray) vcardElement;
        for (Object categoryArray : vcardElementArray) {
          JSONArray categoryJsonArray = ((JSONArray) categoryArray);
          String category = categoryJsonArray.getString(0);
          if (Objects.equals(category, "email")) {
            return false;
          }
        }
      }
    }
    return true;
  }
}
