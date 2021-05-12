package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class ResponseValidation2Dot6Dot3 extends NoticesValidation {

  final static String TITLE = "Status Codes";
  final static String DESCRIPTION = "For more information on domain status codes, please visit https://icann.org/epp";
  final static String HREF = "https://icann.org/epp";

  public ResponseValidation2Dot6Dot3(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType) {
    super(rdapResponse, results, queryType,
        TITLE, DESCRIPTION, HREF, -46600);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_6_3_Validation";
  }
}
