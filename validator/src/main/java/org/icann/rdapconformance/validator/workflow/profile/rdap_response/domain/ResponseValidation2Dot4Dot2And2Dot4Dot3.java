package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.RegistrarEntityValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidation2Dot4Dot2And2Dot4Dot3 extends
    RegistrarEntityValidation {

  public ResponseValidation2Dot4Dot2And2Dot4Dot3(QueryContext qctx) {
    super(qctx, qctx.getDatasetService(), -47400);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_4_2_and_2_4_3_Validation";
  }


  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
