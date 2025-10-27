package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.EventAction;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.TopMostEventActionValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidation2Dot3Dot2_2024 extends TopMostEventActionValidation {

  public ResponseValidation2Dot3Dot2_2024(QueryContext qctx) {
    super(qctx.getRdapResponseData(), qctx.getResults(), qctx.getQueryType(),
        -65400,
        "An eventAction of type 'registrar expiration' is expected.",
        EventAction.REGISTRAR_EXPIRATION);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_3_2_Validation";
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
