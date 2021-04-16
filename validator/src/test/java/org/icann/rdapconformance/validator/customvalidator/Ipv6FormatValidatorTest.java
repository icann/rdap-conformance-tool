package org.icann.rdapconformance.validator.customvalidator;

import static org.mockito.Mockito.mock;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv6AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv6Addresses;
import org.testng.annotations.BeforeMethod;

public class Ipv6FormatValidatorTest extends IpFormatValidatorTest {

  public Ipv6FormatValidatorTest() {
    super("0:0:0:0:0:0:0:1",
        mock(Ipv6AddressSpace.class),
        mock(SpecialIPv6Addresses.class),
        "ipv6-validation",
        "0.0.0.1");
  }

  @BeforeMethod
  public void setUp() {
    super.setUp();
    ipFormatValidator = new Ipv6FormatValidator(ipAddressSpace, specialIpAddresses);
  }
}