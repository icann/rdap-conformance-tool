package org.icann.rdapconformance.validator.workflow.profile;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot13;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot14;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot2;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot3;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot6;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot8;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.Validation1Dot11Dot1;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;

public class RDAPProfileFebruary2019 {

  private final RDAPValidatorConfiguration config;
  private final RDAPQueryType queryType;
  private final Validation1Dot2 validation1Dot2;
  private final Validation1Dot3 validation1Dot3;
  private final Validation1Dot6 validation1Dot6;
  private final Validation1Dot8 validation1Dot8;
  private final Validation1Dot13 validation1Dot13;
  private final Validation1Dot11Dot1 validation1Dot11Dot1;
  private final Validation1Dot14 validation1Dot14;

  public RDAPProfileFebruary2019(
      RDAPValidatorConfiguration config,
      RDAPQueryType queryType,
      Validation1Dot2 validation1Dot2,
      Validation1Dot3 validation1Dot3,
      Validation1Dot6 validation1Dot6,
      Validation1Dot8 validation1Dot8,
      Validation1Dot13 validation1Dot13,
      Validation1Dot11Dot1 validation1Dot11Dot1,
      Validation1Dot14 validation1Dot14) {
    this.config = config;
    this.queryType = queryType;
    this.validation1Dot2 = validation1Dot2;
    this.validation1Dot3 = validation1Dot3;
    this.validation1Dot6 = validation1Dot6;
    this.validation1Dot8 = validation1Dot8;
    this.validation1Dot13 = validation1Dot13;
    this.validation1Dot11Dot1 = validation1Dot11Dot1;
    this.validation1Dot14 = validation1Dot14;
  }

  public boolean validate() {
    boolean result = true;
    result &= validation1Dot2.validate();
    result &= validation1Dot3.validate();
    result &= validation1Dot6.validate();
    result &= validation1Dot8.validate();
    result &= validation1Dot13.validate();
    result &= validation1Dot14.validate();

    if (queryType.equals(RDAPQueryType.DOMAIN) && config.isGtldRegistry()) {
      result &= validation1Dot11Dot1.validate();
    }

    return result;
  }
}
