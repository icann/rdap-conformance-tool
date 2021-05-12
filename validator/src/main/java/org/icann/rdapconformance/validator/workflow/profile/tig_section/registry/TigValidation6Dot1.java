package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import java.util.Set;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public final class TigValidation6Dot1 extends ProfileJsonValidation {

  private final RDAPQueryType queryType;

  public TigValidation6Dot1(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType) {
    super(rdapResponse, results);
    this.queryType = queryType;
  }


  @Override
  public String getGroupName() {
    return "tigSection_6_1_Validation";
  }

  public boolean doValidate() {
    boolean isValid = true;
    Set<String> registrarEntitiesJsonPointers = getPointerFromJPath(
        "$.entities[?(@.roles contains 'registrar')]");

    for (String jsonPointer : registrarEntitiesJsonPointers) {
      isValid &= checkEntity(jsonPointer);
    }

    return isValid;
  }

  private boolean checkEntity(String entityJsonPointer) {
    boolean isValid = true;

    JSONObject entity = (JSONObject) jsonObject.query(entityJsonPointer);
    Set<String> publicIdsJsonPointers = getPointerFromJPath(entity, "$.publicIds[*]");
    if (publicIdsJsonPointers.isEmpty()) {
      results.add(RDAPValidationResult.builder()
          .code(-23300)
          .value(getResultValue(entityJsonPointer))
          .message("A publicIds member is not included in the entity with the registrar role.")
          .build());
      return false;
    }
    for (String jsonPointer : publicIdsJsonPointers) {
      isValid &= checkPublicId(entityJsonPointer.concat(jsonPointer.substring(1)));
    }
    return isValid;
  }

  private boolean checkPublicId(String publicIdJsonPointer) {
    JSONObject publicId = (JSONObject) jsonObject.query(publicIdJsonPointer);
    String identifier = publicId.optString("identifier", "");
    try {
      int id = Integer.parseInt(identifier);
      if (id < 0) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException e) {
      results.add(RDAPValidationResult.builder()
          .code(-23301)
          .value(getResultValue(publicIdJsonPointer))
          .message("The identifier of the publicIds member of the entity with the registrar role "
              + "is not a positive integer.")
          .build());
      return false;
    }
    return true;
  }

  @Override
  public boolean doLaunch() {
    return queryType.isLookupQuery();
  }
}
