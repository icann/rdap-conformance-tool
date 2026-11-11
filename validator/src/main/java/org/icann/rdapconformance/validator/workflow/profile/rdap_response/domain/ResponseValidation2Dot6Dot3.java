package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidation2Dot6Dot3 extends NoticesValidation {

  static final String NOTICE_TITLE = "Status Codes";
  static final String NOTICE_DESCRIPTION = "For more information on domain status codes, please visit https://icann.org/epp";
  static final String NOTICE_HREF = "https://icann.org/epp";

  public ResponseValidation2Dot6Dot3(QueryContext queryContext) {
    super(queryContext, NOTICE_TITLE, NOTICE_DESCRIPTION, NOTICE_HREF, -46600);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_6_3_Validation";
  }
}
