package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import java.util.Set;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;

public final class ResponseValidation2Dot4Dot1 extends ProfileJsonValidation {

  private final RDAPQueryType queryType;

  public ResponseValidation2Dot4Dot1(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType) {
    super(rdapResponse, results);
    this.queryType = queryType;
  }


  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_4_1_Validation";
  }

  public boolean doValidate() {
    boolean isValid = true;
    Set<String> registrarEntitiesJsonPointers = getPointerFromJPath(
        "$.entities[?(@.roles contains 'registrar')]");

    if (null == registrarEntitiesJsonPointers || registrarEntitiesJsonPointers.isEmpty()) {
      results.add(RDAPValidationResult.builder()
          .code(-47300)
          .value(jsonObject.toString())
          .message("An entity with the registrar role was not found in the domain topmost object.")
          .build());
      return false;
    }
    if (registrarEntitiesJsonPointers.size() > 1) {
      results.add(RDAPValidationResult.builder()
          .code(-47301)
          .value(jsonObject.toString())
          .message("More than one entities with the registrar role were found in the domain "
              + "topmost object.")
          .build());
      isValid = false;
    }
    Set<String> vcardJsonPointers = getPointerFromJPath(
        "$.entities[?(@.roles contains 'registrar')]..vcardArray");
    for (String jsonPointer : vcardJsonPointers) {
      isValid &= checkVcard(jsonPointer);
    }

    return isValid;
  }

  private boolean checkVcard(String vcardJsonPointer) {
    JSONArray vcardArray = (JSONArray) jsonObject.query(vcardJsonPointer);
    for (Object vcardElement : vcardArray) {
      if (vcardElement instanceof JSONArray) {
        JSONArray vcardElementArray = (JSONArray) vcardElement;
        for (Object categoryArray : vcardElementArray) {
          JSONArray categoryJsonArray = ((JSONArray) categoryArray);
          String category = categoryJsonArray.getString(0);
          if (category.equals("fn")) {
            return true;
          }
        }
      }
    }
    results.add(RDAPValidationResult.builder()
        .code(-47302)
        .value(getResultValue(vcardJsonPointer))
        .message("An fn member was not found in one or more vcard objects of the entity with the "
            + "registrar role.")
        .build());
    return false;
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
