package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorIpv4Test extends SchemaValidatorTest {

  public SchemaValidatorIpv4Test() {
    super(
        "test_rdap_general_tests.json",
        "/validators/ipv4/valid.json");
  }

  @BeforeMethod
  @Override
  public void setUp() throws IOException {
    super.setUp();
    name = "ipv4";
  }

  /**
   * 7.1.1.1
   */
  @Test
  public void v4NotDotDecimal() {
    jsonObject.put("ipv4", "999");
    validate(-10100, "#/ipv4:999",
        "The IPv4 address is not syntactically valid in dot-decimal notation.");
  }
}
