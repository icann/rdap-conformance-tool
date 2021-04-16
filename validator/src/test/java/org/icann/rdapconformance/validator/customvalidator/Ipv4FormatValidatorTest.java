package org.icann.rdapconformance.validator.customvalidator;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv4AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv4AddressSpace.Record;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv4Addresses;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Ipv4FormatValidatorTest {

  private final String validIp = "111.0.0.0";
  private final Ipv4AddressSpace ipv4AddressSpace = mock(Ipv4AddressSpace.class);
  private final SpecialIPv4Addresses specialIPv4Addresses = mock(SpecialIPv4Addresses.class);

  @BeforeMethod
  public void setUp() {
    doReturn(List.of(new Record("111.0.0.0/8", "ALLOCATED"))).when(ipv4AddressSpace).getRecords();
    doReturn(List.of("0.0.0.0/8")).when(specialIPv4Addresses).getValues();
  }

  @Test
  public void testFormatName() {
    Ipv4FormatValidator ipv4FormatValidator = new Ipv4FormatValidator(ipv4AddressSpace, specialIPv4Addresses);
    assertThat(ipv4FormatValidator.formatName()).isEqualTo("ipv4-validation");
  }

  @Test
  public void testValidateOk() {
    Ipv4FormatValidator ipv4FormatValidator = new Ipv4FormatValidator(ipv4AddressSpace, specialIPv4Addresses);
    assertThat(ipv4FormatValidator.validate(validIp)).isEmpty();
  }

  @Test
  public void validate_NotIpv4_ReturnError() {
    Ipv4FormatValidator ipv4FormatValidator = new Ipv4FormatValidator(ipv4AddressSpace, specialIPv4Addresses);
    assertThat(ipv4FormatValidator.validate("0:0:0:0:0:0:0:1")).contains("[0:0:0:0:0:0:0:1] is not a valid ipv4 address");
  }

  @Test
  public void validate_isInSpecialRegistry() {
    Ipv4FormatValidator ipv4FormatValidator = new Ipv4FormatValidator(ipv4AddressSpace, specialIPv4Addresses);
    doReturn(List.of(new Record("0.0.0.0/8", "ALLOCATED"))).when(ipv4AddressSpace).getRecords();
    assertThat(ipv4FormatValidator.validate("0.0.0.1")).contains("IP address 0.0.0.1 is part of the specialIPv4Addresses");
  }
}