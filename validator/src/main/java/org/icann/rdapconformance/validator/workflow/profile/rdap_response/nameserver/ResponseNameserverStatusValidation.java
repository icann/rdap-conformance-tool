package org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver;

import org.icann.rdapconformance.validator.workflow.profile.rdap_response.NameserverStatusValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseNameserverStatusValidation extends NameserverStatusValidation {

  public ResponseNameserverStatusValidation(String rdapResponse,
      RDAPValidatorResults results, RDAPQueryType queryType) {
    super(rdapResponse, results, queryType, -49300);
  }

  @Override
  protected boolean doValidate() {
    return validateStatus("#/status");
  }

  @Override
  public String getGroupName() {
    return "nameserver_status";
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.NAMESERVER);
  }
}
