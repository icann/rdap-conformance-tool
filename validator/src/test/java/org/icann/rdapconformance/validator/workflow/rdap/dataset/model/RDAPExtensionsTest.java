package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Set;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RDAPExtensionsTest extends BaseUnmarshallingTest<RDAPExtensions> {

  private final RDAPExtensions rdapExtensions = spy(RDAPExtensions.class);
  private RDAPExtensions rdapExtensionMarshalled;

  @BeforeMethod
  public void setUp() {
    doReturn(Set.of("a value")).when(rdapExtensions).getValues();
    this.rdapExtensionMarshalled = unmarshal("/dataset/rdap-extensions.xml", RDAPExtensions.class);
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

  @Test
  public void givenRdapExtensions_whenUnmarshalling_XmlUnmarshalledWithValues() {
    assertThat(rdapExtensionMarshalled).isNotNull();
    assertThat(rdapExtensionMarshalled.getValues()).containsExactly("reverse_search",
            "redirect_with_content",
            "redacted",
            "artRecord",
            "subsetting",
            "paging",
            "arin_originas0",
            "icann_rdap_technical_implementation_guide_0",
            "icann_rdap_response_profile_0",
            "nro_rdap_profile_0",
            "nro_rdap_profile_asn_flat_0",
            "nro_rdap_profile_asn_hierarchical_0",
            "sorting",
            "cidr0",
            "farv1",
            "rdap_objectTag",
            "fred",
            "platformNS",
            "regType");
  }
}