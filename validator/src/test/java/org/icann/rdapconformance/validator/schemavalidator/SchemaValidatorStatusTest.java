package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

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
    notListOfEnum(-11002, "rdap_status.json#/definitions/statusValue/allOf/1");
  }
}
