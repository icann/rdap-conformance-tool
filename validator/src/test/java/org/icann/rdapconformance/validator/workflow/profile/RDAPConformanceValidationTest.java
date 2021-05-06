package org.icann.rdapconformance.validator.workflow.profile;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import java.util.List;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class RDAPConformanceValidationTest<T extends RDAPConformanceValidation> extends
    ProfileValidationTestBase {

  private final Class<T> validationClass;
  protected JSONObject jsonObject;

  protected RDAPConformanceValidationTest(Class<T> validationClass) {
    this.validationClass = validationClass;
  }

  @BeforeMethod
  public void setUp() throws Throwable {
    super.setUp();
    this.jsonObject = new JSONObject(getResource("/validators/profile/rdapConformance/valid.json"));
  }

  @Test
  @Override
  public void testValidate() throws Throwable {
    T validation = validationClass.getConstructor(String.class, RDAPValidatorResults.class)
        .newInstance(jsonObject.toString(), results);
    validateOk(validation);
  }

  @Test
  public void testValidate_RDAPConformanceDoesNotContainsValue_AddErrorCode() throws Throwable {
    jsonObject.put("rdapConformance", List.of("rdap_level_0"));

    T validation = validationClass.getConstructor(String.class, RDAPValidatorResults.class)
        .newInstance(jsonObject.toString(), results);
    validateNotOk(validation, validation.code, "#/rdapConformance:[\"rdap_level_0\"]",
        validation.message);
  }
}