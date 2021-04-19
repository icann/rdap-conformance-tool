package org.icann.rdapconformance.validator.customvalidator;

import static org.mockito.Mockito.mock;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv4AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv4Addresses;

public class Ipv4FormatValidatorTest extends IpFormatValidatorTest {

  public Ipv4FormatValidatorTest() {
    super("0.0.0.0",
        new Ipv4FormatValidator(mock(Ipv4AddressSpace.class), mock(SpecialIPv4Addresses.class)),
        "ipv4-validation",
        "0:0:0:0:0:0:0:1");
  }
}