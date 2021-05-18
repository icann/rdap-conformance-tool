package org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity;

import static org.json.JSONObject.NULL;

import java.util.HashSet;
import java.util.Set;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;

public final class ResponseValidation3Dot1 extends ProfileJsonValidation {

  protected final RDAPValidatorConfiguration config;
  private final RDAPQueryType queryType;

  public ResponseValidation3Dot1(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType,
      RDAPValidatorConfiguration config) {
    super(rdapResponse, results);
    this.queryType = queryType;
    this.config = config;
  }


  @Override
  public String getGroupName() {
    return "rdapResponseProfile_3_1_Validation";
  }

  public boolean doValidate() {
    Set<String> registrarEntitiesJsonPointers = getPointerFromJPath(
        "$.[?(@.roles contains 'registrar')]");

    if (registrarEntitiesJsonPointers.isEmpty()) {
      results.add(RDAPValidationResult.builder()
          .code(-60100)
          .value(jsonObject.toString())
          .message("An entity with the registrar role was not found as the topmost object. "
              + "See section 3.1 of the RDAP_Response_Profile_2_1")
          .build());
      return false;
    }

    if (NULL.equals(jsonObject.opt("handle"))) {
        addResult60101();
        return false;
    }

    boolean isValid = true;

    Set<String> vcardJsonPointers = getPointerFromJPath("$.vcardArray");
    for (String jsonPointer : vcardJsonPointers) {
      isValid &= checkVcard(jsonPointer);
    }

    return isValid;
  }

  private boolean checkVcard(String vcardJsonPointer) {
    Set<String> requiredMembers = new HashSet<>(Set.of("fn", "adr", "tel", "email"));
    JSONArray vcardArray = (JSONArray) jsonObject.query(vcardJsonPointer);
    for (Object vcardElement : vcardArray) {
      if (vcardElement instanceof JSONArray) {
        JSONArray vcardElementArray = (JSONArray) vcardElement;
        for (Object categoryArray : vcardElementArray) {
          JSONArray categoryJsonArray = ((JSONArray) categoryArray);
          String category = categoryJsonArray.getString(0);
          requiredMembers.remove(category);
          if (category.equals("adr")) {
            Object address = categoryJsonArray.get(3);
            if (!(address instanceof JSONArray)) {
              addResult60101();
              return false;
            }
            JSONArray adr = (JSONArray) address;
            for (int idx : Set.of(2, 3, 6)) {
              if (((String) adr.get(idx)).isEmpty()) {
                addResult60101();
                return false;
              }
            }
          }
        }
      }
    }
    if (!requiredMembers.isEmpty()) {
      addResult60101();
      return false;
    }
    return true;
  }

  private void addResult60101() {
    results.add(RDAPValidationResult.builder()
        .code(-60101)
        .value(jsonObject.toString())
        .message("The required members for a registrar entity were not found. "
            + "See section 3.1 of the RDAP_Response_Profile_2_1.")
        .build());
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.ENTITY) && config.isGtldRegistry();
  }
}
