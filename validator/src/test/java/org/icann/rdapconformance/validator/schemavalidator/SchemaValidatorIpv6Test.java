package org.icann.rdapconformance.validator.schemavalidator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorIpv6Test extends SchemaValidatorTest {

  public SchemaValidatorIpv6Test() {
    super(
        "test_rdap_general_tests.json",
        "/validators/ipv6/valid.json");
  }

  @BeforeMethod
  @Override
  public void setUp() throws IOException {
    super.setUp();
    name = "ipv6";
  }

  /**
   * 7.1.2.1
   */
  @Test
  public void v6NotCanonical() {
    jsonObject.put("ipv6", "999");
    validate(-10200, "#/ipv6:999",
        "The IPv6 address is not syntactically valid.");
  }

  /**
   * 7.1.2.2
   */
  @Test
  public void v4NotAllocatedNorLegacy() {
    doReturn(true).when(datasetService.getIpv6AddressSpace()).isInvalid(any());
    validate(-10201, "#/ipv6:0:0:0:0:0:0:0:1",
        "The IPv6 address is not included in a "
            + "prefix categorized as Global Unicast in the Internet Protocol Version 6 Address Space. Dataset: ipv6AddressSpace");
  }

  /**
   * 7.1.2.3
   */
  @Test
  public void v4PartOfSpecialv6Addresses() {
    doReturn(true).when(datasetService.getSpecialIPv6Addresses()).isInvalid(any());
    validate(-10202, "#/ipv6:0:0:0:0:0:0:0:1",
        "The IPv6 address is included in the IANA "
            + "IPv6 Special-Purpose Address Registry. Dataset: specialIPv6Addresses");
  }
}
