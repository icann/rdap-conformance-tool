package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EPPRoid;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class HandleValidationTest<T extends HandleValidation> extends
    ProfileJsonValidationTestBase {

  private final Class<T> validationClass;
  private RDAPDatasetService datasetService;
  private RDAPQueryType queryType;
  private EPPRoid eppRoid;

  public HandleValidationTest(String testGroupName, Class<T> validationClass) {
    super("/validators/domain/valid.json", testGroupName);
    this.validationClass = validationClass;
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
  public HandleValidation getProfileValidation() {
    try {
      return validationClass
          .getConstructor(String.class, RDAPValidatorResults.class, RDAPDatasetService.class,
              RDAPQueryType.class)
          .newInstance(jsonObject.toString(), results, datasetService, queryType);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Make handle invalid and return the result value for that handle
   */
  protected abstract String givenInvalidHandle();

  protected abstract String getValidValueWithRoidExmp();

  @Test
  public void testValidate_HandleFormatNotCompliant_AddErrorCode() {
    String value = givenInvalidHandle();
    HandleValidation validation = getProfileValidation();
    validate(validation.code, value,
        String.format("The handle in the %s object does not comply with the format "
            + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.", validation.objectName));
  }

  @Test
  public void testValidate_HandleNotInEpprRoid_AddErrorCode() {
    doReturn(true).when(eppRoid).isInvalid("EXMP");
    HandleValidation validation = getProfileValidation();
    validate(validation.code - 1, getValidValueWithRoidExmp(),
        String.format("The globally unique identifier in the %s object handle is not registered "
            + "in EPPROID.", validation.objectName));
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