package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Set;
import org.assertj.core.api.AbstractBooleanAssert;
import org.testng.annotations.Test;

public class EnumDatasetTest {

  @Test
  public void testInvalid() {
    testInvalid("another value").isTrue();
  }

  @Test
  public void testValid() {
    testInvalid("an enum value").isFalse();
  }


  private AbstractBooleanAssert<?> testInvalid(String value) {
    EnumDataset enumDataset = spy(EnumDataset.class);
    doReturn(Set.of("an enum value")).when(enumDataset).getValues();
    return assertThat(enumDataset.isInvalid(value));
  }
}