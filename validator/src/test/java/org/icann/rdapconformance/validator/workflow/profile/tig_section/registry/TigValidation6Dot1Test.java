package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.workflow.profile.RegistrarEntityPublicIdsValidation;
import org.icann.rdapconformance.validator.workflow.profile.RegistrarEntityPublicIdsValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.Test;

public class TigValidation6Dot1Test extends RegistrarEntityPublicIdsValidationTest {

  public TigValidation6Dot1Test() {
    super("/validators/domain/valid.json", "tigSection_6_1_Validation", RDAPQueryType.DOMAIN);
  }

  @Override
  public RegistrarEntityPublicIdsValidation getProfileValidation() {
    return new TigValidation6Dot1(jsonObject.toString(), results, config, queryType);
  }

  @Test
  public void testDoLaunch() {
    queryType = RDAPQueryType.HELP;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVERS;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.DOMAIN;
    assertThat(getProfileValidation().doLaunch()).isTrue();
    queryType = RDAPQueryType.NAMESERVER;
    assertThat(getProfileValidation().doLaunch()).isTrue();
    queryType = RDAPQueryType.ENTITY;
    assertThat(getProfileValidation().doLaunch()).isTrue();
  }

}