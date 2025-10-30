package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class TigValidation1Dot14 extends RDAPConformanceValidation {


  public TigValidation1Dot14(QueryContext queryContext) {
    super(queryContext, "icann_rdap_technical_implementation_guide_0", -20600,
        "The RDAP Conformance data structure does not include icann_rdap_technical_implementation_guide_0. "
            + "See section 1.14 of the RDAP_Technical_Implementation_Guide_2_1.");
  }

  /**
   * @deprecated Use TigValidation1Dot14(QueryContext) instead
   * TODO: Migrate to QueryContext-only constructor
   */
  @Deprecated
  public TigValidation1Dot14(String rdapResponse, RDAPValidatorResults results) {
    super(rdapResponse, results, "icann_rdap_technical_implementation_guide_0", -20600,
        "The RDAP Conformance data structure does not include icann_rdap_technical_implementation_guide_0. "
            + "See section 1.14 of the RDAP_Technical_Implementation_Guide_2_1.");
  }

  @Override
  public String getGroupName() {
    return "tigSection_1_14_Validation";
  }
}
