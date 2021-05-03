package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.net.URI;
import java.util.Set;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.BootstrapDomainNameSpace;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Validation1Dot11Dot1Test {

  private final RDAPDatasetService rdapDatasetService = mock(RDAPDatasetService.class);
  private final BootstrapDomainNameSpace dataset = mock(BootstrapDomainNameSpace.class);
  private final RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);

  @BeforeMethod
  public void setUp() {
    doReturn(dataset).when(rdapDatasetService).get(BootstrapDomainNameSpace.class);
    doReturn(URI.create("https://domain.test/rdap/test.example")).when(config).getUri();
  }

  @Test
  public void testValidate() {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);

    doReturn(true).when(dataset).tldExists("example");
    doReturn(Set.of("https://domain.abc/rdap", "https://domain.test/rdap")).when(dataset)
        .getUrlsForTld("example");

    assertThat(new Validation1Dot11Dot1(config, results, rdapDatasetService).validate()).isTrue();
    verify(results).addGroup("tigSection_1_11_1_Validation", false);
    verifyNoMoreInteractions(results);
  }

  @Test
  public void testValidate_TldNotInBootstrap_AddResults23100() {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);

    doReturn(false).when(dataset).tldExists("example");
    doReturn(Set.of("abc", "test")).when(dataset).getTlds();

    assertThat(new Validation1Dot11Dot1(config, results, rdapDatasetService).validate()).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -23100)
        .hasFieldOrPropertyWithValue("value", "example\n/\nabc, test")
        .hasFieldOrPropertyWithValue("message",
            "The TLD is not included in the bootstrapDomainNameSpace. "
                + "See section 1.11.1 of the RDAP_Technical_Implementation_Guide_2_1.");
    verify(results).addGroup("tigSection_1_11_1_Validation", true);
  }

  @Test
  public void testValidate_UrlNotInBootstrap_AddResults23101() {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);

    doReturn(true).when(dataset).tldExists("example");
    doReturn(Set.of("https://domain.abc/rdap", "https://abc.test/rdap")).when(dataset)
        .getUrlsForTld("example");

    assertThat(new Validation1Dot11Dot1(config, results, rdapDatasetService).validate()).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -23101)
        .hasFieldOrPropertyWithValue("value", "https://abc.test/rdap, https://domain.abc/rdap")
        .hasFieldOrPropertyWithValue("message",
            "The TLD entry in bootstrapDomainNameSpace does not contain a base URL. "
                + "See section 1.11.1 of the RDAP_Technical_Implementation_Guide_2_1.");
    verify(results).addGroup("tigSection_1_11_1_Validation", true);
  }

  @Test
  public void testValidate_BootstrapUrlNotHttps_AddResults23102() {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);

    doReturn(true).when(dataset).tldExists("example");
    doReturn(Set.of("http://domain.abc/rdap", "https://domain.test/rdap")).when(dataset)
        .getUrlsForTld("example");

    assertThat(new Validation1Dot11Dot1(config, results, rdapDatasetService).validate()).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -23102)
        .hasFieldOrPropertyWithValue("value", "http://domain.abc/rdap, https://domain.test/rdap")
        .hasFieldOrPropertyWithValue("message",
            "One or more of the base URLs for the TLD contain a schema different from "
                + "https. See section 1.2 of the RDAP_Technical_Implementation_Guide_2_1.");
    verify(results).addGroup("tigSection_1_11_1_Validation", true);
  }
}