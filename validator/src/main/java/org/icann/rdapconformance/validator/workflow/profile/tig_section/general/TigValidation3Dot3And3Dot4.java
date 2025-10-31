package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.util.Set;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class TigValidation3Dot3And3Dot4 extends ProfileJsonValidation {

  private final QueryContext queryContext;

  public TigValidation3Dot3And3Dot4(QueryContext queryContext) {
    super(queryContext.getRdapResponseData(), queryContext.getResults());
    this.queryContext = queryContext;
  }

  /**
   * @deprecated Use TigValidation3Dot3And3Dot4(QueryContext) instead
   * TODO: Migrate tests to QueryContext-only constructor
   */
  @Deprecated
  public TigValidation3Dot3And3Dot4(String rdapResponse, RDAPValidatorResults results, org.icann.rdapconformance.validator.SchemaValidator schemaValidator) {
    super(rdapResponse, results);
    this.queryContext = null; // Not available in deprecated constructor
    // schemaValidator is no longer used in current implementation
  }

  @Override
  public String getGroupName() {
    return "tigSection_3_3_and_3_4_Validation";
  }

  @Override
  public boolean doValidate() {
    Set<String> linksInTopMostNotices = getPointerFromJPath("$.notices[*].links");
    if (linksInTopMostNotices.isEmpty()) {
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
          .code(-20700)
          .value(getResultValue("#/notices"))
          .message("A links object was not found in the notices object in the "
              + "topmost object. See section 3.3 and 3.4 of the "
              + "RDAP_Technical_Implementation_Guide_2_1.");

      if (queryContext != null) {
        results.add(builder.build(queryContext));
      } else {
        results.add(builder.build()); // Fallback for deprecated constructor
      }
      return false;
    }
    return true;
  }
}
