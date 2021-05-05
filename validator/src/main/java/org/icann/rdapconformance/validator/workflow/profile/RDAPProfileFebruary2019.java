package org.icann.rdapconformance.validator.workflow.profile;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot13;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot14;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot2;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot3;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot6;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot8;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation3Dot3And3Dot4;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation4Dot1;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation7Dot1And7Dot2;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.Validation1Dot11Dot1;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.Validation3Dot2;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.Validation6Dot1;
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
  private final Validation3Dot2 validation3Dot2;
  private final Validation6Dot1 validation6Dot1;
  private final Validation3Dot3And3Dot4 validation3Dot3And3Dot4;
  private final Validation4Dot1 validation4Dot1;
  private final Validation7Dot1And7Dot2 validation7Dot1And7Dot2;

  public RDAPProfileFebruary2019(
      RDAPValidatorConfiguration config,
      RDAPQueryType queryType,
      Validation1Dot2 validation1Dot2,
      Validation1Dot3 validation1Dot3,
      Validation1Dot6 validation1Dot6,
      Validation1Dot8 validation1Dot8,
      Validation1Dot13 validation1Dot13,
      Validation1Dot11Dot1 validation1Dot11Dot1,
      Validation1Dot14 validation1Dot14,
      Validation3Dot2 validation3Dot2,
      Validation6Dot1 validation6Dot1,
      Validation3Dot3And3Dot4 validation3Dot3And3Dot4,
      Validation4Dot1 validation4Dot1,
      Validation7Dot1And7Dot2 validation7Dot1And7Dot2) {
    this.config = config;
    this.queryType = queryType;
    this.validation1Dot2 = validation1Dot2;
    this.validation1Dot3 = validation1Dot3;
    this.validation1Dot6 = validation1Dot6;
    this.validation1Dot8 = validation1Dot8;
    this.validation1Dot13 = validation1Dot13;
    this.validation1Dot11Dot1 = validation1Dot11Dot1;
    this.validation1Dot14 = validation1Dot14;
    this.validation3Dot2 = validation3Dot2;
    this.validation6Dot1 = validation6Dot1;
    this.validation3Dot3And3Dot4 = validation3Dot3And3Dot4;
    this.validation4Dot1 = validation4Dot1;
    this.validation7Dot1And7Dot2 = validation7Dot1And7Dot2;
  }

  public boolean validate() {
    boolean result = true;
    result &= validation1Dot2.validate();
    result &= validation1Dot3.validate();
    result &= validation1Dot6.validate();
    result &= validation1Dot8.validate();
    result &= validation1Dot13.validate();
    result &= validation1Dot14.validate();
    result &= validation3Dot3And3Dot4.validate();
    result &= validation4Dot1.validate();
    result &= validation7Dot1And7Dot2.validate();

    if (config.isGtldRegistry()) {
      if (queryType.equals(RDAPQueryType.DOMAIN)) {
        result &= validation1Dot11Dot1.validate();
        result &= validation6Dot1.validate();
      }
      result &= validation3Dot2.validate();
    }

    return result;
  }
}
