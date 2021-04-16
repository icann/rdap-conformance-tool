package org.icann.rdapconformance.validator.customvalidator;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.IpAddressSpace;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class IpFormatValidatorTest {

  public IpFormatValidatorTest(String ipAddress,
      IpAddressSpace ipAddressSpace,
      IpAddressSpace specialIpAddresses,
      String format,
      String invalidIp) {
    this.ipAddress = ipAddress;
    this.ipAddressSpace = ipAddressSpace;
    this.specialIpAddresses = specialIpAddresses;
    this.format = format;
    this.invalidIp = invalidIp;
  }

  protected final String ipAddress;
  protected final IpAddressSpace ipAddressSpace;
  protected final IpAddressSpace specialIpAddresses;
  protected IpFormatValidator ipFormatValidator;
  protected final String format;
  protected final String invalidIp;

  @BeforeMethod
  public void setUp() {
    doReturn(false).when(ipAddressSpace).isInvalid(any());
    doReturn(false).when(specialIpAddresses).isInvalid(any());
  }

  @Test
  public void testFormatName() {
    assertThat(ipFormatValidator.formatName()).isEqualTo(format);
  }

  @Test
  public void testValidateOk() {
    assertThat(ipFormatValidator.validate(ipAddress)).isEmpty();
  }

  @Test
  public void validate_NotIp_ReturnError() {
    assertThat(ipFormatValidator.validate(invalidIp)).contains("["+invalidIp+"] is not a valid " + format.replace("-validation", "")
        + " address");
  }

  @Test
  public void validate_isInSpecialRegistry() {
    doReturn(true).when(specialIpAddresses).isInvalid(any());
    assertThat(ipFormatValidator.validate(ipAddress)).contains(ipFormatValidator.getPartOfSpecialAddressesSpaceError());
  }
}