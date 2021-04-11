package org.icann.rdapconformance.validator;

import java.util.List;
import org.testng.annotations.Test;

public class SchemaValidatorIpAddressTest extends SchemaValidatorTest {

  public SchemaValidatorIpAddressTest() {
    super(
        "test_rdap_ipAddress.json",
        "/validators/ipAddress/valid.json");
  }

  /**
   * 7.2.10.1
   */
  @Test
  public void invalid() {
    invalid(-11400);
  }

  /**
   * 7.2.10.2
   */
  @Test
  public void unauthorizedKey() {
    validateAuthorizedKeys(-11401, List.of("v6"));
  }
}
