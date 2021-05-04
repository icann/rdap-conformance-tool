package org.icann.rdapconformance.validator.workflow.profile;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot13;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot14;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot2;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot3;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot6;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot8;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.Validation6Dot1;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.Validation1Dot11Dot1;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.Validation3Dot2;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RDAPProfileFebruary2019Test {

  private Validation1Dot2 validation1Dot2;
  private Validation1Dot3 validation1Dot3;
  private Validation1Dot6 validation1Dot6;
  private Validation1Dot8 validation1Dot8;
  private Validation1Dot13 validation1Dot13;
  private Validation1Dot11Dot1 validation1Dot11Dot1;
  private Validation1Dot14 validation1Dot14;
  private Validation3Dot2 validation3Dot2;
  private Validation6Dot1 validation6Dot1;
  private RDAPValidatorConfiguration config;
  private RDAPQueryType queryType;
  private RDAPProfileFebruary2019 rdapProfileFebruary2019;

  @BeforeMethod
  public void setUp() {
    config = mock(RDAPValidatorConfiguration.class);
    doReturn(true).when(config).isGtldRegistry();
    queryType = RDAPQueryType.DOMAIN;

    validation1Dot2 = mock(Validation1Dot2.class);
    validation1Dot3 = mock(Validation1Dot3.class);
    validation1Dot6 = mock(Validation1Dot6.class);
    validation1Dot8 = mock(Validation1Dot8.class);
    validation1Dot13 = mock(Validation1Dot13.class);
    validation1Dot11Dot1 = mock(Validation1Dot11Dot1.class);
    validation1Dot14 = mock(Validation1Dot14.class);
    validation3Dot2 = mock(Validation3Dot2.class);
    validation6Dot1 = mock(Validation6Dot1.class);
    rdapProfileFebruary2019 = new RDAPProfileFebruary2019(
        config,
        queryType,
        validation1Dot2,
        validation1Dot3,
        validation1Dot6,
        validation1Dot8,
        validation1Dot13,
        validation1Dot11Dot1,
        validation1Dot14,
        validation3Dot2,
        validation6Dot1);
  }

  @Test
  public void checkAllValidationsCalled() {
    rdapProfileFebruary2019.validate();
    verify(validation1Dot2).validate();
    verify(validation1Dot3).validate();
    verify(validation1Dot6).validate();
    verify(validation1Dot8).validate();
    verify(validation1Dot13).validate();
    verify(validation1Dot11Dot1).validate();
    verify(validation1Dot14).validate();
    verify(validation3Dot2).validate();
    verify(validation6Dot1).validate();
  }

  @Test
  public void checkValidation_GtldIsRegistry() {
    doReturn(false).when(config).isGtldRegistry();
    rdapProfileFebruary2019.validate();
    verifyNoInteractions(validation1Dot11Dot1);
    verifyNoInteractions(validation3Dot2);
    verifyNoInteractions(validation6Dot1);
  }
}