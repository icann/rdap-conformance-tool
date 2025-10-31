package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseDomainValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;

public abstract class ResponseValidation2Dot7Dot1DotXAndRelatedTest extends
    ResponseDomainValidationTestBase {

  RDAPValidatorConfiguration config;

  public ResponseValidation2Dot7Dot1DotXAndRelatedTest() {
    super("rdapResponseProfile_2_7_1_X_and_2_7_2_X_and_2_7_3_X_and_2_7_4_X_Validation");
  }

  @BeforeMethod
  public void setUp() throws java.io.IOException {
    super.setUp();
    config = mock(RDAPValidatorConfiguration.class);
    doReturn(true).when(config).isGtldRegistrar();
  }

  protected void remarkMemberIs(String key, String value) {
    if (jsonObject.query("#/entities/0/remarks") == null) {
      putValue("$['entities'][0]",
          "remarks",
          List.of(Map.of(key, value)));
    }

    putValue("$['entities'][0]['remarks'][0]", key, value);
    assertThat((String) getValue("$['entities'][0]['remarks'][0]['" + key + "']")).isEqualTo(value);
  }

  protected void entitiesWithRole(String role) {
    replaceValue("$['entities'][0]['roles'][0]", role);
    assertThat((String) getValue("$['entities'][0]['roles'][0]")).isEqualTo(role);
  }


  public void testDoLaunch() {
    super.testDoLaunch();
    queryContext.setQueryType(RDAPQueryType.DOMAIN);
    doReturn(false).when(config).isGtldRegistry();
    doReturn(false).when(config).isGtldRegistrar();
    assertThat(getProfileValidation().doLaunch()).isFalse();
    doReturn(true).when(config).isGtldRegistry();
    doReturn(true).when(config).isThin();
    assertThat(getProfileValidation().doLaunch()).isFalse();
    doReturn(false).when(config).isThin();
    assertThat(getProfileValidation().doLaunch()).isTrue();
  }
}