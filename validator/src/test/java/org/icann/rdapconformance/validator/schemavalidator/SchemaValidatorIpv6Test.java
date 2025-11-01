package org.icann.rdapconformance.validator.schemavalidator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv6AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv6Addresses;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorIpv6Test extends SchemaValidatorTest {

  public SchemaValidatorIpv6Test() {
    super(
        "test_rdap_general_tests.json",
        "/validators/ipv6/valid.json");
    validationName = "IPv6Validation";
  }

  @BeforeMethod
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
  public void v6NotAllocatedNorLegacy() {
    doReturn(true).when(datasets.get(Ipv6AddressSpace.class)).isInvalid(any());
    validate(-10201, "#/ipv6:0:0:0:0:0:0:0:1",
        "The IPv6 address is not included in a "
            + "prefix categorized as Global Unicast in the Internet Protocol Version 6 Address Space. Dataset: ipv6AddressSpace");
  }

  /**
   * 7.1.2.3
   */
  @Test
  public void v6PartOfSpecialv6Addresses() {
    doReturn(true).when(datasets.get(SpecialIPv6Addresses.class)).isInvalid(any());
    validate(-10202, "#/ipv6:0:0:0:0:0:0:0:1",
        "The IPv6 address is included in the IANA "
            + "IPv6 Special-Purpose Address Registry. Dataset: specialIPv6Addresses");
  }
}
