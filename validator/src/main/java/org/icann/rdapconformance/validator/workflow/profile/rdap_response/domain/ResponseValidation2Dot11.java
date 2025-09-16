package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidation2Dot11 extends NoticesValidation {

  final static String TITLE = "RDDS Inaccuracy Complaint Form";
  final static String DESCRIPTION = "URL of the ICANN RDDS Inaccuracy Complaint Form: https://icann.org/wicf";
  final static String HREF = "https://icann.org/wicf";

  public ResponseValidation2Dot11(String rdapResponse,
      RDAPValidatorResults results,
      RDAPValidatorConfiguration config,
      RDAPQueryType queryType) {
    super(rdapResponse, results, queryType,
        TITLE, DESCRIPTION, HREF, -46700);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_11_Validation";
  }
}
