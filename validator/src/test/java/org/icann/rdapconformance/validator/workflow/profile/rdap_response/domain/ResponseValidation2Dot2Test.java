package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EPPRoid;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot2Test extends ProfileJsonValidationTestBase {

  private RDAPDatasetService datasetService;
  private RDAPQueryType queryType;
  private EPPRoid eppRoid;


  public ResponseValidation2Dot2Test() {
    super("/validators/domain/valid.json", "rdapResponseProfile_2_1_Validation");
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    datasetService = mock(RDAPDatasetService.class);
    eppRoid = mock(EPPRoid.class);
    queryType = RDAPQueryType.DOMAIN;
    doReturn(eppRoid).when(datasetService).get(EPPRoid.class);
    doReturn(false).when(eppRoid).isInvalid("EXMP");
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot2(jsonObject.toString(), results, datasetService, queryType);
  }

  @Test
  public void testValidate_HandleFormatNotCompliant_AddResults46200() {
    replaceValue("handle", "ABCD");
    validate(-46200, "#/handle:ABCD",
        "The handle in the domain object does not comply with the format "
            + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
  }

  @Test
  public void testValidate_HandleNotInEpprRoid_AddResults46201() {
    doReturn(true).when(eppRoid).isInvalid("EXMP");
    validate(-46201, "#/handle:2138514_DOMAIN_COM-EXMP",
        "The globally unique identifier in the domain object handle is not registered "
            + "in EPPROID.");
  }

  @Test
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