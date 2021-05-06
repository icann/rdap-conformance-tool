package org.icann.rdapconformance.validator.workflow.profile.tig_section.registrar;

import com.jayway.jsonpath.DocumentContext;
import java.util.List;
import org.icann.rdapconformance.validator.schema.JsonPointers;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.TigJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class Validation1Dot12Dot1 extends TigJsonValidation {

  public Validation1Dot12Dot1(String rdapResponse,
      RDAPValidatorResults results) {
    super(rdapResponse, results);
  }

  @Override
  protected String getGroupName() {
    return "tigSection_1_12_1_Validation";
  }

  @Override
  protected boolean doValidate() {
    DocumentContext jpath = getJPath();
    List<String> publicIdsPaths = jpath.read(
        "$.entities[?(@.roles contains 'registrar')]..publicIds.*");
    boolean isValid = true;
    for (String publicIdsPath : publicIdsPaths) {
      String jsonPointer = JsonPointers.fromJpath(publicIdsPath);
      isValid &= checkPublicId(jsonPointer, (JSONObject) jsonObject.query(jsonPointer));
    }
    
    return isValid;
  }

  private boolean checkPublicId(String jsonPointer, JSONObject publicId) {
      if (!publicId.has("identifier")) {
        results.add(RDAPValidationResult.builder()
            .code(-26100)
            .value(jsonPointer + ":" + publicId)
            .message("An identifier in the publicIds within the entity data "
                + "structure with the registrar role was not found. See section 1.12.1 of the "
                + "RDAP_Technical_Implementation_Guide_2_1.")
            .build());
        return false;
      }
    return true;
  }
}
