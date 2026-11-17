package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock;
import org.icann.rdapconformance.validator.workflow.SchemaValidatorCache;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQuery;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TigValidation1Dot8Test {

  private static final String HOST = "rdap.example.com";
  private static final String MSG_V4 =
      "The RDAP service is not provided over IPv4 or contains invalid addresses. See section 1.8 of the "
          + "RDAP_Technical_Implementation_Guide_2_1.";
  private static final String MSG_V6 =
      "The RDAP service is not provided over IPv6 or contains invalid addresses. See section 1.8 of the "
          + "RDAP_Technical_Implementation_Guide_2_1.";

  private RDAPValidatorConfiguration config;
  private RDAPQuery query;
  private HttpResponse<String> httpResponse;
  private DNSCacheResolver dnsResolver;
  private RDAPDatasetServiceMock datasetService;
  private QueryContext queryContext;

  @BeforeEach
  public void setUp() throws UnknownHostException {
    SchemaValidatorCache.clearCache();
    config = mock(RDAPValidatorConfiguration.class);
    doReturn(URI.create("https://" + HOST + "/domain/test.example")).when(config).getUri();
    doReturn(false).when(config).isNoIpv4Queries();
    doReturn(false).when(config).isNoIpv6Queries();

    httpResponse = mock(HttpResponse.class);
    doReturn(config.getUri()).when(httpResponse).uri();
    doReturn(Optional.empty()).when(httpResponse).previousResponse();
    query = mock(RDAPQuery.class);
    doReturn(httpResponse).when(query).getRawResponse();

    dnsResolver = mock(DNSCacheResolver.class);
    doReturn(List.of(InetAddress.getByName("192.0.2.1"))).when(dnsResolver).getAllV4Addresses(anyString());
    doReturn(List.of(InetAddress.getByName("2001:db8::1"))).when(dnsResolver).getAllV6Addresses(anyString());

    datasetService = new RDAPDatasetServiceMock();
    queryContext = spy(QueryContext.forTesting(config, datasetService, query));
    doReturn(dnsResolver).when(queryContext).getDnsResolver();
  }

  @AfterEach
  public void tearDown() {
    SchemaValidatorCache.clearCache();
  }

  private List<RDAPValidationResult> results() {
    return queryContext.getResults().getAll().stream().toList();
  }

  @Test
  public void getGroupName() {
    assertThat(new TigValidation1Dot8(queryContext).getGroupName()).isEqualTo("tigSection_1_8_Validation");
  }

  @Test
  public void testValidate_NullResponse_IsOk() {
    doReturn(null).when(query).getRawResponse();

    assertThat(new TigValidation1Dot8(queryContext).validate()).isTrue();
    assertThat(results()).isEmpty();
  }

  @Test
  public void testValidate_ValidV4AndV6_IsOk() {
    assertThat(new TigValidation1Dot8(queryContext).validate()).isTrue();
    assertThat(results()).isEmpty();
  }

  @Test
  public void testValidate_NoV4Addresses_AddResult20400() {
    doReturn(List.of()).when(dnsResolver).getAllV4Addresses(anyString());

    assertThat(new TigValidation1Dot8(queryContext).validate()).isFalse();
    assertThat(results()).hasSize(1);
    assertThat(results().get(0))
        .hasFieldOrPropertyWithValue("code", -20400)
        .hasFieldOrPropertyWithValue("value", HOST)
        .hasFieldOrPropertyWithValue("message", MSG_V4);
  }

  @Test
  public void testValidate_NoV6Addresses_AddResult20401() {
    doReturn(List.of()).when(dnsResolver).getAllV6Addresses(anyString());

    assertThat(new TigValidation1Dot8(queryContext).validate()).isFalse();
    assertThat(results()).hasSize(1);
    assertThat(results().get(0))
        .hasFieldOrPropertyWithValue("code", -20401)
        .hasFieldOrPropertyWithValue("value", HOST)
        .hasFieldOrPropertyWithValue("message", MSG_V6);
  }

  @Test
  public void testValidate_NoAddressesAtAll_AddBothResults() {
    doReturn(List.of()).when(dnsResolver).getAllV4Addresses(anyString());
    doReturn(List.of()).when(dnsResolver).getAllV6Addresses(anyString());

    assertThat(new TigValidation1Dot8(queryContext).validate()).isFalse();
    assertThat(results()).extracting("code").containsExactlyInAnyOrder(-20400, -20401);
  }

  @Test
  public void testValidate_InvalidV4Address_AddResult20400() {
    // An InetAddress that is neither Inet4Address nor Inet6Address is rejected by IPValidator
    doReturn(List.of(mock(InetAddress.class))).when(dnsResolver).getAllV4Addresses(anyString());

    assertThat(new TigValidation1Dot8(queryContext).validate()).isFalse();
    assertThat(results()).extracting("code").containsExactly(-20400);
  }

  @Test
  public void testValidate_InvalidV6Address_AddResult20401() {
    doReturn(List.of(mock(InetAddress.class))).when(dnsResolver).getAllV6Addresses(anyString());

    assertThat(new TigValidation1Dot8(queryContext).validate()).isFalse();
    assertThat(results()).extracting("code").containsExactly(-20401);
  }

  @Test
  public void testValidate_NoIpv4Queries_SkipsV4Check() {
    doReturn(true).when(config).isNoIpv4Queries();
    doReturn(List.of()).when(dnsResolver).getAllV4Addresses(anyString());

    assertThat(new TigValidation1Dot8(queryContext).validate()).isTrue();
    assertThat(results()).isEmpty();
  }

  @Test
  public void testValidate_NoIpv6Queries_SkipsV6Check() {
    doReturn(true).when(config).isNoIpv6Queries();
    doReturn(List.of()).when(dnsResolver).getAllV6Addresses(anyString());

    assertThat(new TigValidation1Dot8(queryContext).validate()).isTrue();
    assertThat(results()).isEmpty();
  }

  @Test
  public void testValidate_UriWithoutHost_IsOk() {
    doReturn(URI.create("mailto:someone@example.com")).when(httpResponse).uri();
    doReturn(List.of()).when(dnsResolver).getAllV4Addresses(anyString());

    assertThat(new TigValidation1Dot8(queryContext).validate()).isTrue();
    assertThat(results()).isEmpty();
  }

  @Test
  public void testValidate_FollowsPreviousResponses() {
    HttpResponse<String> previous = mock(HttpResponse.class);
    doReturn(URI.create("https://old.example.com/domain/test.example")).when(previous).uri();
    doReturn(Optional.empty()).when(previous).previousResponse();
    doReturn(Optional.of(previous)).when(httpResponse).previousResponse();
    doReturn(List.of()).when(dnsResolver).getAllV6Addresses("old.example.com");

    assertThat(new TigValidation1Dot8(queryContext).validate()).isFalse();
    assertThat(results()).extracting("value").containsExactly("old.example.com");
  }

  @Test
  public void ipValidator_UnknownAddressType_IsInvalid() {
    InetAddress unknown = mock(InetAddress.class);

    assertThat(new TigValidation1Dot8.IPValidator().isInvalid(unknown, datasetService, queryContext)).isTrue();
  }

  @Test
  public void ipValidator_ValidV4_IsNotInvalid() throws UnknownHostException {
    InetAddress address = InetAddress.getByName("192.0.2.1");

    assertThat(new TigValidation1Dot8.IPValidator().isInvalid(address, datasetService, queryContext)).isFalse();
  }

  @Test
  public void ipSchema_paths() {
    assertThat(TigValidation1Dot8.IPSchema.V4.path()).isEqualTo("profile/tig_section/ipv4_address.json");
    assertThat(TigValidation1Dot8.IPSchema.V6.path()).isEqualTo("profile/tig_section/ipv6_address.json");
  }
}
