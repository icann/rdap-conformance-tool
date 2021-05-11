package org.icann.rdapconformance.validator.workflow.profile.rdap_response;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class TopMostEventActionValidationTest<T extends TopMostEventActionValidation> extends
    ProfileJsonValidationTestBase {

  private final Class<T> validationClass;
  protected RDAPQueryType queryType;

  public TopMostEventActionValidationTest(String testGroupName,
      Class<T> validationClass) {
    super("/validators/domain/valid.json", testGroupName);
    this.validationClass = validationClass;
  }

  @Override
  @BeforeMethod
  public void setUp() throws java.io.IOException {
    super.setUp();
    queryType = RDAPQueryType.DOMAIN;
  }

  @Override
  public TopMostEventActionValidation getTigValidation() {
    try {
      return validationClass.getConstructor(String.class, RDAPValidatorResults.class,
          RDAPQueryType.class).newInstance(jsonObject.toString(), results, queryType);
    } catch (Exception e) {
      return null;
    }
  }

  @Test
  public void testValidate_EventsDoNotContainValue_AddErrorCode() {
    replaceValue("$.events[*].eventAction", "event");
    TopMostEventActionValidation validation = getTigValidation();
    validateNotOk(results, validation.code,
        "[{\"eventAction\":\"event\",\"eventDate\":\"1997-09-15T04:00:00Z\"},"
            + "{\"eventAction\":\"event\",\"eventDate\":\"2028-09-14T04:00:00Z\"},"
            + "{\"eventAction\":\"event\",\"eventDate\":\"2021-03-18T09:24:18Z\"}]",
        validation.message);
  }

  @Test
  public abstract void testDoLaunch();
}
