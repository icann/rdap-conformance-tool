package org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver;

import java.util.Set;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.RegistrarEntityValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public final class ResponseValidation4Dot3 extends RegistrarEntityValidation {

  public ResponseValidation4Dot3(String rdapResponse,
      RDAPValidatorResults results,
       RDAPValidatorConfiguration config,
      RDAPDatasetService datasetService,
      RDAPQueryType queryType) {
    super(rdapResponse, results, config, datasetService, queryType, -49200);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_4_3_Validation";
  }

  protected boolean checkEntity(String entityJsonPointer) {
    JSONObject entity = (JSONObject) jsonObject.query(entityJsonPointer);
    String handle = entity.optString("handle", "");
    if (!handle.equals("not applicable")) {
      return super.checkEntity(entityJsonPointer);
    } else {
      Set<String> publicIdsJsonPointers = getPointerFromJPath(entity, "$.publicIds[*]");
      if (!publicIdsJsonPointers.isEmpty()) {
        results.add(RDAPValidationResult.builder()
            .code(-49205)
            .value(getResultValue(entityJsonPointer))
            .message("A publicIds member is included in the entity with the registrar role.")
            .build());
        return false;
      }
      return true;
    }
  }


  @Override
  public boolean doLaunch() {
    return getRegistrarEntitiesJsonPointers().size() > 0 && queryType
        .equals(RDAPQueryType.NAMESERVER);
  }
}
