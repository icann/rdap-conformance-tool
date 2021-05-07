package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.util.Set;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.BootstrapDomainNameSpace;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TigValidation1Dot11Dot1Test extends ProfileValidationTestBase {

  private final static RDAPQueryType QUERY_TYPE = RDAPQueryType.DOMAIN;
  private final RDAPDatasetService rdapDatasetService = mock(RDAPDatasetService.class);
  private final BootstrapDomainNameSpace dataset = mock(BootstrapDomainNameSpace.class);
  private final RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);

  @BeforeMethod
  public void setUp() throws Throwable {
    super.setUp();
    doReturn(dataset).when(rdapDatasetService).get(BootstrapDomainNameSpace.class);
    doReturn(URI.create("https://domain.test/rdap/test.example")).when(config).getUri();
    doReturn(true).when(config).isGtldRegistry();
  }

  @Override
  @Test
  public void testValidate() {
    doReturn(true).when(dataset).tldExists("example");
    doReturn(Set.of("https://domain.abc/rdap", "https://domain.test/rdap")).when(dataset)
        .getUrlsForTld("example");

    validateOk(new TigValidation1Dot11Dot1(config, results, rdapDatasetService, QUERY_TYPE));
  }

  @Test
  public void testValidate_TldNotInBootstrap_AddResults23100() {
    doReturn(false).when(dataset).tldExists("example");
    doReturn(Set.of("abc", "test")).when(dataset).getTlds();

    validateNotOk(new TigValidation1Dot11Dot1(config, results, rdapDatasetService, QUERY_TYPE), -23100,
        "example\n/\nabc, test",
        "The TLD is not included in the bootstrapDomainNameSpace. "
            + "See section 1.11.1 of the RDAP_Technical_Implementation_Guide_2_1.");
  }

  @Test
  public void testValidate_UrlNotInBootstrap_AddResults23101() {
    doReturn(true).when(dataset).tldExists("example");
    doReturn(Set.of("https://domain.abc/rdap", "https://abc.test/rdap")).when(dataset)
        .getUrlsForTld("example");

    validateNotOk(new TigValidation1Dot11Dot1(config, results, rdapDatasetService, QUERY_TYPE), -23101,
        "https://abc.test/rdap, https://domain.abc/rdap",
        "The TLD entry in bootstrapDomainNameSpace does not contain a base URL. "
            + "See section 1.11.1 of the RDAP_Technical_Implementation_Guide_2_1.");
  }

  @Test
  public void testValidate_BootstrapUrlNotHttps_AddResults23102() {
    doReturn(true).when(dataset).tldExists("example");
    doReturn(Set.of("http://domain.abc/rdap", "https://domain.test/rdap")).when(dataset)
        .getUrlsForTld("example");

    validateNotOk(new TigValidation1Dot11Dot1(config, results, rdapDatasetService, QUERY_TYPE), -23102,
        "http://domain.abc/rdap, https://domain.test/rdap",
        "One or more of the base URLs for the TLD contain a schema different from "
            + "https. See section 1.2 of the RDAP_Technical_Implementation_Guide_2_1.");
  }

  @Test
  public void testDoLaunch_NotARegistry_IsFalse() {
    doReturn(false).when(config).isGtldRegistry();
    assertThat(new TigValidation1Dot11Dot1(config, results, rdapDatasetService, QUERY_TYPE).doLaunch())
        .isFalse();
  }

  @Test
  public void testDoLaunch_NotADomainQuery_IsFalse() {
    doReturn(true).when(config).isGtldRegistry();
    assertThat(
        new TigValidation1Dot11Dot1(config, results, rdapDatasetService, RDAPQueryType.NAMESERVER)
            .doLaunch()).isFalse();
  }
}