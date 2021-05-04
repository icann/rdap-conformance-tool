package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.util.Optional;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.schema.JsonPointers;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class Validation3Dot3And3Dot4 extends TigValidation {

  private final JSONObject jsonObject;
  private final RDAPValidatorResults results;
  private final SchemaValidator schemaValidator;

  public Validation3Dot3And3Dot4(String rdapResponse,
      RDAPValidatorResults results,
      SchemaValidator schemaValidator) {
    super(results);
    this.jsonObject = new JSONObject(rdapResponse);
    this.results = results;
    this.schemaValidator = schemaValidator;
  }

  @Override
  public String getGroupName() {
    return "tigSection_3_3_and_3_4_Validation";
  }

  @Override
  public boolean doValidate() {
    JsonPointers jsonPointers = schemaValidator.getSchemaRootNode().findJsonPointersBySchemaId(
        "rdap_notices.json"
        , jsonObject);
    boolean noLinksInTopMost = jsonPointers.getOnlyTopMosts()
        .stream()
        .map(j -> (JSONObject) jsonObject.query(j))
        .noneMatch(notice -> notice.has("links"));
    Optional<String> noticesArray = jsonPointers.getParentOfTopMosts();
    if (noLinksInTopMost && noticesArray.isPresent()) {
      results.add(RDAPValidationResult.builder()
          .code(-20700)
          .value(jsonObject.query(noticesArray.get()).toString())
          .message("A links object was not found in the notices object in the "
              + "topmost object. See section 3.3 and 3.4 of the "
              + "RDAP_Technical_Implementation_Guide_2_1.")
          .build());
      return false;
    }
    return true;
  }
}
