package org.icann.rdapconformance.validator.workflow.profile;

import java.net.http.HttpResponse;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.TigSectionGeneral;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class RDAPProfileFebruary2019 {

  private final RDAPValidatorConfiguration config;
  private final RDAPValidatorResults results;
  private final HttpResponse<String> rdapResponse;

  public RDAPProfileFebruary2019(RDAPValidatorConfiguration config,
      RDAPValidatorResults results, HttpResponse<String> rdapResponse) {
    this.config = config;
    this.results = results;
    this.rdapResponse = rdapResponse;
  }

  public boolean validate() {
    boolean result = true;
    result &= TigSectionGeneral.validate(rdapResponse, config, results);

    return result;
  }
}
