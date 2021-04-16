package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Set;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RDAPExtensionsTest {

  RDAPExtensions rdapExtensions = spy(RDAPExtensions.class);

  @BeforeMethod
  public void setUp() {
    doReturn(Set.of("a value")).when(rdapExtensions).getValues();
  }

  @Test
  public void test_rdap_level_0() {
    assertThat(rdapExtensions.isInvalid("rdap_level_0")).isFalse();
  }

  @Test
  public void testEnumValidation() {
    assertThat(rdapExtensions.isInvalid("a value")).isFalse();
  }

  @Test
  public void testInvalidity() {
    assertThat(rdapExtensions.isInvalid("another value")).isTrue();
  }
}