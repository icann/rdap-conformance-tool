package org.icann.rdapconformance.validator.schemavalidator;

import static org.mockito.Mockito.doReturn;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.StatusJsonValues;
import org.testng.annotations.Test;

public class SchemaValidatorStatusTest extends SchemaValidatorForArrayOfStringTest {

  public SchemaValidatorStatusTest() {
    super(
        "test_rdap_status.json",
        "/validators/status/valid.json");
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
    doReturn(true).when(datasetService.get(StatusJsonValues.class)).isInvalid("wrong enum value");
    notListOfEnumDataset(-11002,
        "The JSON string is not included as a Value with \nType=\"status\".");
  }
}
