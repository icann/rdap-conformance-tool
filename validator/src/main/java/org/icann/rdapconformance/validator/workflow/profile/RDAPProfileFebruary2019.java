package org.icann.rdapconformance.validator.workflow.profile;

import java.net.http.HttpResponse;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot13;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot2;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot3;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot6;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot8;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.Validation1Dot11Dot1;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class RDAPProfileFebruary2019 {

  private final RDAPValidatorConfiguration config;
  private final RDAPValidatorResults results;
  private final RDAPDatasetService datasetService;
  private final HttpResponse<String> rdapResponse;
  private final RDAPQueryType queryType;

  public RDAPProfileFebruary2019(RDAPValidatorConfiguration config,
      RDAPValidatorResults results, RDAPDatasetService datasetService,
      HttpResponse<String> rdapResponse, RDAPQueryType queryType) {
    this.config = config;
    this.results = results;
    this.rdapResponse = rdapResponse;
    this.datasetService = datasetService;
    this.queryType = queryType;
  }

  public boolean validate() {
    boolean result = true;
    result &= Validation1Dot2.validate(rdapResponse, config, results);
    result &= Validation1Dot3.validate(rdapResponse, config, results);
    result &= Validation1Dot6.validate(rdapResponse.statusCode(), config, results);
    result &= Validation1Dot8.validate(rdapResponse, results, datasetService);
    result &= Validation1Dot13.validate(rdapResponse, results);

    if (queryType.equals(RDAPQueryType.DOMAIN) && config.isGtldRegistry()) {
      result &= Validation1Dot11Dot1.validate(config,results, datasetService);
    }

    return result;
  }
}
