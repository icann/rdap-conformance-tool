package org.icann.rdapconformance.validator.customvalidator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public abstract class DatasetValidatorTest<T extends DatasetValidator> extends FormatValidatorTest<T> {

  public DatasetValidatorTest(String formatName,
      T formatValidator) {
    super(formatName, formatValidator);
  }

  @Test
  public void valid() {
    doReturn(false).when(formatValidator.getDatasetValidatorModel()).isInvalid(any());
    Assertions.assertThat(formatValidator.validate("a string")).isEmpty();
  }

  @Test
  public void invalid() {
    doReturn(true).when(formatValidator.getDatasetValidatorModel()).isInvalid(any());
    Assertions.assertThat(formatValidator.validate("a string"))
        .contains(formatValidator.getErrorMsg());
  }

}
