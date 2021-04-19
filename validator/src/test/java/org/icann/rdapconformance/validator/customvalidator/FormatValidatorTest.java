package org.icann.rdapconformance.validator.customvalidator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.everit.json.schema.FormatValidator;
import org.testng.annotations.Test;

public abstract class FormatValidatorTest<T extends FormatValidator> {

  protected final T formatValidator;
  protected final String formatName;

  public FormatValidatorTest(String formatName,
      T formatValidator) {
    this.formatName = formatName;
    this.formatValidator = formatValidator;
  }

  @Test
  public void testFormatName() {
    assertThat(formatValidator.formatName()).isEqualTo(formatName);
  }

}
