package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidation2Dot11 extends NoticesValidation {

  static final String NOTICE_TITLE = "RDDS Inaccuracy Complaint Form";
  static final String NOTICE_DESCRIPTION = "URL of the ICANN RDDS Inaccuracy Complaint Form: https://icann.org/wicf";
  static final String NOTICE_HREF = "https://icann.org/wicf";

  public ResponseValidation2Dot11(QueryContext queryContext) {
    super(queryContext, NOTICE_TITLE, NOTICE_DESCRIPTION, NOTICE_HREF, -46700);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_11_Validation";
  }
}
