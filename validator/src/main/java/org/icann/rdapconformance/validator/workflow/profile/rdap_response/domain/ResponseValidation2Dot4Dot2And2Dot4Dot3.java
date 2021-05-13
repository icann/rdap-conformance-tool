package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.RegistrarEntityPublicIdsValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarId;
import org.json.JSONObject;

public final class ResponseValidation2Dot4Dot2And2Dot4Dot3 extends
    RegistrarEntityPublicIdsValidation {

  private final RDAPDatasetService datasetService;

  public ResponseValidation2Dot4Dot2And2Dot4Dot3(String rdapResponse,
      RDAPValidatorResults results,
      RDAPDatasetService datasetService,
      RDAPQueryType queryType) {
    super(rdapResponse, results, queryType, -47400);
    this.datasetService = datasetService;
  }


  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_4_2_and_2_4_3_Validation";
  }

  @Override
  protected boolean checkEntity(String entityJsonPointer) {
    boolean isValid = super.checkEntity(entityJsonPointer);
    isValid &= checkHandle(entityJsonPointer);
    return isValid;
  }

  private boolean checkHandle(String entityJsonPointer) {
    JSONObject entity = (JSONObject) jsonObject.query(entityJsonPointer);
    String handle = entity.optString("handle", "");
    if (!isPositiveInteger(handle)) {
      results.add(RDAPValidationResult.builder()
          .code(-47402)
          .value(getResultValue(entityJsonPointer))
          .message("The handle of the entity with the registrar role is not a positive integer.")
          .build());
      return false;
    }
    RegistrarId registrarId = datasetService.get(RegistrarId.class);
    if (!registrarId.containsId(Integer.parseInt(handle))) {
      results.add(RDAPValidationResult.builder()
          .code(-47404)
          .value(getResultValue(entityJsonPointer))
          .message(
              "The handle references an IANA Registrar ID that does not exist in the registrarId.")
          .build());
      return false;
    }
    return true;
  }

  @Override
  protected boolean checkPublicId(JSONObject entity, String publicIdJsonPointer) {
    if (!super.checkPublicId(entity, publicIdJsonPointer)) {
      return false;
    }
    JSONObject publicId = (JSONObject) jsonObject.query(publicIdJsonPointer);
    String handle = entity.optString("handle", "");
    if (isPositiveInteger(handle)) {
      // we would have another error if handle is not a positive integer
      String identifier = publicId.optString("identifier", "");
      if (!handle.equals(identifier)) {
        results.add(RDAPValidationResult.builder()
            .code(-47403)
            .value(entity.toString())
            .message(
                "The identifier of the publicIds member of the entity with the registrar role "
                    + "is not equal to the handle member.")
            .build());
        return false;
      }
    }
    return true;
  }


  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
