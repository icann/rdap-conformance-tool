package org.icann.rdapconformance.validator.customvalidator;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class IpFormatValidatorTest extends CustomValidatorTest<IpFormatValidator> {

  public IpFormatValidatorTest(String ipAddress,
      IpFormatValidator formatValidator,
      String format,
      String invalidIp) {
    super(format, formatValidator);
    this.ipAddress = ipAddress;
    this.format = format;
    this.invalidIp = invalidIp;
    this.ipFormatValidator = formatValidator;
  }

  protected final String ipAddress;
  protected IpFormatValidator ipFormatValidator;
  protected final String format;
  protected final String invalidIp;

  @BeforeMethod
  public void setUp() {
    doReturn(false).when(ipFormatValidator.getIpAddressesValidator()).isInvalid(any());
    doReturn(false).when(ipFormatValidator.getSpecialIpAddresses()).isInvalid(any());
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
    doReturn(true).when(ipFormatValidator.getSpecialIpAddresses()).isInvalid(any());
    assertThat(ipFormatValidator.validate(ipAddress)).contains(ipFormatValidator.getPartOfSpecialAddressesSpaceError());
  }
}