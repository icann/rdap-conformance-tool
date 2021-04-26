package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.StatusJsonValues;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class SchemaValidatorStatusTest extends SchemaValidatorForArrayOfStringTest {

  public SchemaValidatorStatusTest() {
    super(
        "test_rdap_status.json",
        "/validators/status/valid.json");
    validationName = "stdRdapStatusValidation";
  }

  /**
   * 7.2.6.1.
   */
  @Test
  public void invalid() {
    invalid(-11000);
  }

  /**
   * 7.2.6.2.
   */
  @Test
  public void notListOfString() {
    notListOfString(-11001);
  }

  /**
   * 7.2.6.3.
   */
  @Test
  public void notListOfEnum() {
    doReturn(true).when(datasets.get(StatusJsonValues.class)).isInvalid(WRONG_ENUM_VALUE);
    notListOfEnumDataset(-11002,
        "The JSON string is not included as a Value with \nType=\"status\".");
  }

  @Override
  protected void validate(int errorCode, String value, String msg) {
    super.validate(errorCode, value, msg);
    assertThat(results.getGroupOk()).isEmpty();
    assertThat(results.getGroupErrorWarning()).containsExactly("stdRdapStatusValidation");
  }

  @AfterMethod
  public void tearDown() {
    if (results.isEmpty()) {
      assertThat(results.getGroupOk()).containsExactly("stdRdapStatusValidation");
    }
  }
}
