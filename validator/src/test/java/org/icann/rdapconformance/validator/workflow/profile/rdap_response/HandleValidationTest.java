package org.icann.rdapconformance.validator.workflow.profile.rdap_response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
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
  protected RDAPQueryType baseQueryType;
  private RDAPDatasetService datasetService;
  private RDAPQueryType queryType;
  private EPPRoid eppRoid;

  public HandleValidationTest(String validJsonResourcePath, String testGroupName,
      RDAPQueryType baseQueryType,
      Class<T> validationClass) {
    super(validJsonResourcePath, testGroupName);
    this.baseQueryType = baseQueryType;
    this.validationClass = validationClass;
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    datasetService = mock(RDAPDatasetService.class);
    eppRoid = mock(EPPRoid.class);
    queryType = this.baseQueryType;
    doReturn(eppRoid).when(datasetService).get(EPPRoid.class);
    doReturn(false).when(eppRoid).isInvalid("EXMP");
  }

  @Override
  public HandleValidation getProfileValidation() {
    try {
      return validationClass
          .getConstructor(RDAPValidatorConfiguration.class, String.class, RDAPValidatorResults.class, RDAPDatasetService.class,
              RDAPQueryType.class)
          .newInstance(config, jsonObject.toString(), results, datasetService, queryType);
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
        String.format("The handle in the entity object does not comply with the format "
            + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730."));
  }

  @Test
  public void testValidate_HandleNotInEpprRoid_AddErrorCode() {
    doReturn(true).when(eppRoid).isInvalid("EXMP");
    HandleValidation validation = getProfileValidation();
    validate(validation.code - 1, getValidValueWithRoidExmp(),
        String.format("The globally unique identifier in the %s object handle is not registered "
            + "in EPPROID.", baseQueryType.name().toLowerCase()));
  }

  @Test
  public void testDoLaunch() {
    for (RDAPQueryType queryTypeBeingTested : List
        .of(RDAPQueryType.HELP, RDAPQueryType.NAMESERVERS, RDAPQueryType.NAMESERVER,
            RDAPQueryType.ENTITY, RDAPQueryType.DOMAIN)) {
      queryType = queryTypeBeingTested;
      if (queryType.equals(baseQueryType)) {
        assertThat(getProfileValidation().doLaunch()).isTrue();
      } else {
        assertThat(getProfileValidation().doLaunch()).isFalse();
      }
    }
  }
}