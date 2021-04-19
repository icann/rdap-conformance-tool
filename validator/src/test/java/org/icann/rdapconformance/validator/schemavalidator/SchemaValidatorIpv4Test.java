package org.icann.rdapconformance.validator.schemavalidator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv4AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv4Addresses;
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

  /**
   * 7.1.1.2
   */
  @Test
  public void v4NotAllocatedNorLegacy() {
    doReturn(true).when(datasetService.get(Ipv4AddressSpace.class)).isInvalid(any());
    validate(-10101, "#/ipv4:172.16.254.1",
        "The IPv4 address is not included in a prefix categorized as ALLOCATED or LEGACY in the IANA IPv4 Address Space Registry. Dataset: ipv4AddressSpace");
  }

  /**
   * 7.1.1.3
   */
  @Test
  public void v4PartOfSpecialv4Addresses() {
    doReturn(true).when(datasetService.get(SpecialIPv4Addresses.class)).isInvalid(any());
    validate(-10102, "#/ipv4:172.16.254.1",
        "The IPv4 address is included in the IANA IPv4 Special-Purpose  Address Registry. Dataset: specialIPv4Addresses");
  }
}
