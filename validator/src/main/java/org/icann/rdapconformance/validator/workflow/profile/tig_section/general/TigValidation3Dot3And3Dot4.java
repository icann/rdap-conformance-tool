package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.util.Set;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class TigValidation3Dot3And3Dot4 extends ProfileJsonValidation {

  private final SchemaValidator schemaValidator;

  public TigValidation3Dot3And3Dot4(String rdapResponse,
      RDAPValidatorResults results,
      SchemaValidator schemaValidator) {
    super(rdapResponse, results);
    this.schemaValidator = schemaValidator;
  }

  @Override
  public String getGroupName() {
    return "tigSection_3_3_and_3_4_Validation";
  }

  @Override
  public boolean doValidate() {
    Set<String> linksInTopMostNotices = getPointerFromJPath("$.notices[*].links");
    if (linksInTopMostNotices.isEmpty()) {
      results.add(RDAPValidationResult.builder()
          .code(-20700)
          .value(getResultValue("#/notices"))
          .message("A links object was not found in the notices object in the "
              + "topmost object. See section 3.3 and 3.4 of the "
              + "RDAP_Technical_Implementation_Guide_2_1.")
          .build());
      return false;
    }
    return true;
  }
}
