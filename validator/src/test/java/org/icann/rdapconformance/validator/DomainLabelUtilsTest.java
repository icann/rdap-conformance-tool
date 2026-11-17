package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DomainLabelUtilsTest {

  @Test
  public void hasMixedLabels_nullOrEmpty_returnsFalse() {
    assertThat(DomainLabelUtils.hasMixedLabels(null)).isFalse();
    assertThat(DomainLabelUtils.hasMixedLabels("")).isFalse();
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"example.com", "sub.example.com", "xn--bcher-kva.example", "XN--BCHER-KVA.xn--p1ai"})
  public void hasMixedLabels_pureAsciiOrOnlyALabels_returnsFalse(String domain) {
    assertThat(DomainLabelUtils.hasMixedLabels(domain)).isFalse();
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"bücher.example", "例え.テスト", "%E4%BE%8B.example"})
  public void hasMixedLabels_onlyULabels_returnsFalse(String domain) {
    assertThat(DomainLabelUtils.hasMixedLabels(domain)).isFalse();
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"xn--bcher-kva.例え", "例え.xn--p1ai", "XN--BCHER-KVA.bücher.example"})
  public void hasMixedLabels_unicodeAndALabel_returnsTrue(String domain) {
    assertThat(DomainLabelUtils.hasMixedLabels(domain)).isTrue();
  }

  @Test
  public void hasMixedLabels_percentEncodedULabelWithALabel_returnsTrue() {
    assertThat(DomainLabelUtils.hasMixedLabels("xn--bcher-kva.%E4%BE%8B")).isTrue();
    assertThat(DomainLabelUtils.hasMixedLabels("%E4%BE%8B.xn--p1ai")).isTrue();
  }

  @Test
  public void hasMixedLabels_incompleteUtf8Sequence_decodesToReplacementChar_returnsTrue() {
    // URLDecoder maps a lone %E4 to U+FFFD (non-ASCII) instead of throwing
    assertThat(DomainLabelUtils.hasMixedLabels("xn--bcher-kva.%E4")).isTrue();
  }

  @Test
  public void hasMixedLabels_malformedPercentEscapeWithNonAsciiByte_returnsTrue() {
    // "%4" is a malformed escape so URLDecoder throws; the raw scan must still spot %E4 (> 0x7F)
    assertThat(DomainLabelUtils.hasMixedLabels("xn--bcher-kva.%E4%4")).isTrue();
    assertThat(DomainLabelUtils.hasMixedLabels("%E4%4.xn--bcher-kva")).isTrue();
    assertThat(DomainLabelUtils.hasMixedLabels("xn--bcher-kva.a%zz%E4")).isTrue();
  }

  @Test
  public void hasMixedLabels_malformedPercentEscapeWithoutALabel_returnsFalse() {
    assertThat(DomainLabelUtils.hasMixedLabels("foo.%E4%4")).isFalse();
  }

  @Test
  public void hasMixedLabels_undecodablePercentWithAsciiByteOnly_returnsFalse() {
    // "%4" is malformed so decode fails; the only valid sequence (%41 = 'A') is ASCII
    assertThat(DomainLabelUtils.hasMixedLabels("xn--bcher-kva.%41%4")).isFalse();
    assertThat(DomainLabelUtils.hasMixedLabels("foo%4.bar")).isFalse();
  }

  @Test
  public void isAscii() {
    assertThat(DomainLabelUtils.isAscii("")).isTrue();
    assertThat(DomainLabelUtils.isAscii("example.com")).isTrue();
    assertThat(DomainLabelUtils.isAscii("\u007F")).isTrue();
    assertThat(DomainLabelUtils.isAscii("\u0080")).isFalse();
    assertThat(DomainLabelUtils.isAscii("bücher")).isFalse();
  }
}
