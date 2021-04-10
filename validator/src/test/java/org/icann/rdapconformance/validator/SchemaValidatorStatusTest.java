package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class SchemaValidatorStatusTest extends SchemaValidatorTest {

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
    arrayInvalid(-11000);
  }
}
