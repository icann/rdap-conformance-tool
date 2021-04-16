package org.icann.rdapconformance.validator.customvalidator;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv4AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv4Addresses;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Ipv4FormatValidatorTest {

  private final String ipAddress = "0.0.0.0";
  private final Ipv4AddressSpace ipv4AddressSpace = mock(Ipv4AddressSpace.class);
  private final SpecialIPv4Addresses specialIPv4Addresses = mock(SpecialIPv4Addresses.class);
  private Ipv4FormatValidator ipv4FormatValidator;

  @BeforeMethod
  public void setUp() {
    doReturn(false).when(ipv4AddressSpace).isInvalid(any());
    doReturn(false).when(specialIPv4Addresses).isInvalid(any());
    ipv4FormatValidator = new Ipv4FormatValidator(ipv4AddressSpace, specialIPv4Addresses);
  }

  @Test
  public void testFormatName() {
    assertThat(ipv4FormatValidator.formatName()).isEqualTo("ipv4-validation");
  }

  @Test
  public void testValidateOk() {
    assertThat(ipv4FormatValidator.validate(ipAddress)).isEmpty();
  }

  @Test
  public void validate_NotIpv4_ReturnError() {
    assertThat(ipv4FormatValidator.validate("0:0:0:0:0:0:0:1")).contains("[0:0:0:0:0:0:0:1] is not a valid ipv4 address");
  }

  @Test
  public void validate_isInSpecialRegistry() {
    doReturn(true).when(specialIPv4Addresses).isInvalid(any());
    assertThat(ipv4FormatValidator.validate(ipAddress)).contains(Ipv4FormatValidator.PART_OF_SPECIAL_ADDRESSES);
  }
}