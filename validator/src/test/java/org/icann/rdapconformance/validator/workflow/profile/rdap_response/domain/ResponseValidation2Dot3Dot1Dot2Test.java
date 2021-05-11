package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.workflow.profile.rdap_response.TopMostEventActionValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;

public class ResponseValidation2Dot3Dot1Dot2Test extends
    TopMostEventActionValidationTest<ResponseValidation2Dot3Dot1Dot2> {

  public ResponseValidation2Dot3Dot1Dot2Test() {
    super("rdapResponseProfile_2_3_1_2_Validation", ResponseValidation2Dot3Dot1Dot2.class);
  }

  @Override
  public void testDoLaunch() {
    queryType = RDAPQueryType.HELP;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVERS;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVER;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.ENTITY;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.DOMAIN;
    assertThat(getProfileValidation().doLaunch()).isTrue();
  }
}