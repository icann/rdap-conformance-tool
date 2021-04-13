package org.icann.rdapconformance.validator;

import java.util.List;
import org.testng.annotations.Test;

public class SchemaValidatorErrorResponseTest extends SchemaValidatorObjectTest {

  public SchemaValidatorErrorResponseTest() {
    super(
        "error",
        "rdap_error_object.json",
        "/validators/error/valid.json",
        -12100, // 7.2.17.1
        -12101, // 7.2.17.2
        -12102, // 7.2.17.3
        List.of("description", "errorCode", "title"));
  }

  /**
   * 7.2.17.4
   */
  @Test
  public void errorCodeNotNumber() {
    validateIsNotANumber(-12103, "errorCode");
  }

  /**
   * 7.2.17.5
   */
  @Test
  public void titleNotString() {
    validateIsNotAJsonString(-12104, "title");
  }
}
