package org.icann.rdapconformance.validator.customvalidator;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class HostNameInUriFormatValidatorTest extends FormatValidatorTest<HostNameInUriFormatValidator> {

  public HostNameInUriFormatValidatorTest(String formatName,
                                          HostNameInUriFormatValidator formatValidator) {
    super(formatName, formatValidator);
  }

  @Test
  public void valid() {
    assertThat(formatValidator.validate("https://rdap.now.cn:8443")).isPresent();
  }

}
