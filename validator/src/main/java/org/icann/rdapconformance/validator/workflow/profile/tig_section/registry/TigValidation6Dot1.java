package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import org.icann.rdapconformance.validator.workflow.profile.RegistrarEntityPublicIdsValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class TigValidation6Dot1 extends RegistrarEntityPublicIdsValidation {

  public TigValidation6Dot1(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType) {
    super(rdapResponse, results, queryType, -23300);
  }

  @Override
  public String getGroupName() {
    return "tigSection_6_1_Validation";
  }

  @Override
  public boolean doLaunch() {
    return queryType.isLookupQuery();
  }
}
