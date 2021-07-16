package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.util.HashSet;
import java.util.Set;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class EntitiesWithinDomainProfileJsonValidation extends ProfileJsonValidation {

  protected final RDAPQueryType queryType;
  protected final RDAPValidatorConfiguration config;

  public EntitiesWithinDomainProfileJsonValidation(
      String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType,
      RDAPValidatorConfiguration config) {
    super(rdapResponse, results);
    this.queryType = queryType;
    this.config = config;
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
    Set<String> roles = new HashSet<>();
    for (String jsonPointer : entityJsonPointers) {
      JSONObject entity = (JSONObject) jsonObject.query(jsonPointer);

      if (entity.has("roles")) {
        for (Object role : entity.getJSONArray("roles")) {
          if (!roles.add(role.toString())) {
            results.add(RDAPValidationResult.builder()
                .code(-52104)
                .value(getResultValue(jsonPointer))
                .message("More than one entity with the following roles were found: "
                    + "registrant, administrative, technical and billing.")
                .build());
            isValid = false;
          }
        }
      }

      isValid &= doValidateEntity(jsonPointer, entity);
    }
    return isValid;
  }

  protected abstract boolean doValidateEntity(String jsonPointer, JSONObject entity);

}
