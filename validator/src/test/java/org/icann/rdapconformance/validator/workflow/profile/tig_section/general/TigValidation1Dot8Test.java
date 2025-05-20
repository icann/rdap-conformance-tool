package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot8.IPValidator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.icann.rdapconformance.validator.DNSCacheResolver;

import org.mockito.MockedStatic;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.CommonUtils.DASH;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;
import static org.mockito.Mockito.*;

public class TigValidation1Dot8Test {

  public static final String EXAMPLE_COM = "example.com";
  private RDAPValidatorResultsImpl results;
  private RDAPDatasetService datasetService;
  private RDAPValidatorConfiguration config;

  @BeforeMethod
  public void setUp() {
    results = RDAPValidatorResultsImpl.getInstance();
    results.clear();
    datasetService = mock(RDAPDatasetService.class);
    config = mock(RDAPValidatorConfiguration.class);
  }

  @Test
  public void testGetGroupName() {
    HttpResponse<String> response = mock(HttpResponse.class);
    TigValidation1Dot8 validation = new TigValidation1Dot8(response, results, datasetService, config);
    assertThat(validation.getGroupName()).isEqualTo("tigSection_1_8_Validation");
  }

  @Test
  public void testDoValidate_NullResponse() {
    TigValidation1Dot8 validation = new TigValidation1Dot8(null, results, datasetService, config);
    assertThat(validation.doValidate()).isTrue();
    assertThat(results.getAll()).isEmpty();
  }

  @Test
  public void testDoValidate_ValidHost_ValidAddresses() {
    HttpResponse<String> response = mock(HttpResponse.class);
    URI uri = URI.create("http://example.com");
    when(response.uri()).thenReturn(uri);
    when(response.previousResponse()).thenReturn(Optional.empty());
    when(config.isNoIpv4Queries()).thenReturn(false);
    when(config.isNoIpv6Queries()).thenReturn(false);

    try (MockedStatic<DNSCacheResolver> dnsMock = mockStatic(DNSCacheResolver.class)) {
      InetAddress ipv4 = mock(Inet4Address.class);
      when(ipv4.getHostAddress()).thenReturn("192.0.2.1");
      InetAddress ipv6 = mock(Inet6Address.class);
      when(ipv6.getHostAddress()).thenReturn("2001:db8::1");

      dnsMock.when(() -> DNSCacheResolver.getAllV4Addresses(EXAMPLE_COM))
             .thenReturn(List.of(ipv4));
      dnsMock.when(() -> DNSCacheResolver.getAllV6Addresses(EXAMPLE_COM))
             .thenReturn(List.of(ipv6));

      TigValidation1Dot8.IPValidator ipValidator = mock(TigValidation1Dot8.IPValidator.class);
      TigValidation1Dot8.ipValidator = ipValidator;
      when(ipValidator.isInvalid(any(), any())).thenReturn(false);

      TigValidation1Dot8 validation = new TigValidation1Dot8(response, results, datasetService, config);
      assertThat(validation.doValidate()).isTrue();
      assertThat(results.getAll()).isEmpty();
    }
  }

  @Test
  public void testDoValidate_InvalidIPv4Address() {
    HttpResponse<String> response = mock(HttpResponse.class);
    URI uri = URI.create("http://example.com");
    when(response.uri()).thenReturn(uri);
    when(response.previousResponse()).thenReturn(Optional.empty());
    when(config.isNoIpv4Queries()).thenReturn(false);
    when(config.isNoIpv6Queries()).thenReturn(true);

    try (MockedStatic<DNSCacheResolver> dnsMock = mockStatic(DNSCacheResolver.class)) {
      InetAddress ipv4 = mock(Inet4Address.class);
      when(ipv4.getHostAddress()).thenReturn("192.0.2.1");
      dnsMock.when(() -> DNSCacheResolver.getAllV4Addresses(EXAMPLE_COM))
             .thenReturn(List.of(ipv4));

      TigValidation1Dot8.IPValidator ipValidator = mock(TigValidation1Dot8.IPValidator.class);
      TigValidation1Dot8.ipValidator = ipValidator;
      when(ipValidator.isInvalid(ipv4, datasetService)).thenReturn(true);

      TigValidation1Dot8 validation = new TigValidation1Dot8(response, results, datasetService, config);
      assertThat(validation.doValidate()).isFalse();
      assertThat(results.getAll()).contains(
          RDAPValidationResult.builder()
                              .acceptHeader(DASH)
                              .queriedURI(DASH)
                              .httpMethod(DASH)
                              .serverIpAddress(DASH)
                              .httpStatusCode(ZERO)
                              .code(-20400)
                              .value(EXAMPLE_COM)
                              .message("The RDAP service is not provided over IPv4 or contains invalid addresses. See section 1.8 of the RDAP_Technical_Implementation_Guide_2_1.")
                              .build()
      );
    }
  }

  @Test
  public void testDoValidate_InvalidIPv6Address() {
    HttpResponse<String> response = mock(HttpResponse.class);
    URI uri = URI.create("http://example.com");
    when(response.uri()).thenReturn(uri);
    when(response.previousResponse()).thenReturn(Optional.empty());
    when(config.isNoIpv4Queries()).thenReturn(true);
    when(config.isNoIpv6Queries()).thenReturn(false);

    try (MockedStatic<DNSCacheResolver> dnsMock = mockStatic(DNSCacheResolver.class)) {
      InetAddress ipv6 = mock(Inet6Address.class);
      when(ipv6.getHostAddress()).thenReturn("2001:db8::1");
      dnsMock.when(() -> DNSCacheResolver.getAllV6Addresses(EXAMPLE_COM))
             .thenReturn(List.of(ipv6));

      TigValidation1Dot8.IPValidator ipValidator = mock(TigValidation1Dot8.IPValidator.class);
      TigValidation1Dot8.ipValidator = ipValidator;
      when(ipValidator.isInvalid(ipv6, datasetService)).thenReturn(true);

      TigValidation1Dot8 validation = new TigValidation1Dot8(response, results, datasetService, config);
      assertThat(validation.doValidate()).isFalse();
      assertThat(results.getAll()).contains(
          RDAPValidationResult.builder()
                              .acceptHeader(DASH)
                              .queriedURI(DASH)
                              .httpMethod(DASH)
                              .httpStatusCode(ZERO)
                              .serverIpAddress(DASH)
                              .code(-20401)
                              .value(EXAMPLE_COM)
                              .message("The RDAP service is not provided over IPv6 or contains invalid addresses. See section 1.8 of the RDAP_Technical_Implementation_Guide_2_1.")
                              .build()
      );
    }
  }

  @Test
  public void testDoValidate_EmptyIPv4AndIPv6() {
    HttpResponse<String> response = mock(HttpResponse.class);
    URI uri = URI.create("http://example.com");
    when(response.uri()).thenReturn(uri);
    when(response.previousResponse()).thenReturn(Optional.empty());
    when(config.isNoIpv4Queries()).thenReturn(false);
    when(config.isNoIpv6Queries()).thenReturn(false);

    try (MockedStatic<DNSCacheResolver> dnsMock = mockStatic(DNSCacheResolver.class)) {
      dnsMock.when(() -> DNSCacheResolver.getAllV4Addresses(EXAMPLE_COM))
             .thenReturn(Collections.emptyList());
      dnsMock.when(() -> DNSCacheResolver.getAllV6Addresses(EXAMPLE_COM))
             .thenReturn(Collections.emptyList());

      TigValidation1Dot8 validation = new TigValidation1Dot8(response, results, datasetService, config);
      assertThat(validation.doValidate()).isFalse();
      assertThat(results.getAll()).contains(
          RDAPValidationResult.builder()
                              .acceptHeader(DASH)
                              .queriedURI(DASH)
                              .httpMethod(DASH)
                              .httpStatusCode(ZERO)
                              .serverIpAddress(DASH)
                              .code(-20400)
                              .value(EXAMPLE_COM)
                              .message("The RDAP service is not provided over IPv4 or contains invalid addresses. See section 1.8 of the RDAP_Technical_Implementation_Guide_2_1.")
                              .build(),
          RDAPValidationResult.builder()
                              .acceptHeader(DASH)
                              .queriedURI(DASH)
                              .httpMethod(DASH)
                              .httpStatusCode(ZERO)
                              .serverIpAddress(DASH)
                              .code(-20401)
                              .value(EXAMPLE_COM)
                              .message("The RDAP service is not provided over IPv6 or contains invalid addresses. See section 1.8 of the RDAP_Technical_Implementation_Guide_2_1.")
                              .build()
      );
    }
  }

  @Test
  public void testDoValidate_HostIsNull() {
    HttpResponse<String> response = mock(HttpResponse.class);
    URI uri = mock(URI.class);
    when(uri.getHost()).thenReturn(null);
    when(response.uri()).thenReturn(uri);
    when(response.previousResponse()).thenReturn(Optional.empty());

    TigValidation1Dot8 validation = new TigValidation1Dot8(response, results, datasetService, config);
    assertThat(validation.doValidate()).isTrue();
    assertThat(results.getAll()).isEmpty();
  }

  @Test
  public void testDoValidate_HostIsEmpty() {
    HttpResponse<String> response = mock(HttpResponse.class);
    URI uri = mock(URI.class);
    when(uri.getHost()).thenReturn("");
    when(response.uri()).thenReturn(uri);
    when(response.previousResponse()).thenReturn(Optional.empty());

    TigValidation1Dot8 validation = new TigValidation1Dot8(response, results, datasetService, config);
    assertThat(validation.doValidate()).isTrue();
    assertThat(results.getAll()).isEmpty();
  }

  @Test
  public void testDoValidate_WithPreviousResponse() {
    HttpResponse<String> response1 = mock(HttpResponse.class);
    HttpResponse<String> response2 = mock(HttpResponse.class);
    URI uri1 = URI.create("http://example.com");
    URI uri2 = URI.create("http://example.org");
    when(response1.uri()).thenReturn(uri1);
    when(response1.previousResponse()).thenReturn(Optional.of(response2));
    when(response2.uri()).thenReturn(uri2);
    when(response2.previousResponse()).thenReturn(Optional.empty());
    when(config.isNoIpv4Queries()).thenReturn(true);
    when(config.isNoIpv6Queries()).thenReturn(true);

    TigValidation1Dot8 validation = new TigValidation1Dot8(response1, results, datasetService, config);
    assertThat(validation.doValidate()).isTrue();
    assertThat(results.getAll()).isEmpty();
  }

  @Test
  public void testValidateHost_AllBranches() {
    // Host is null
    assertThat(TigValidation1Dot8.validateHost(
        URI.create("file:///"), results, datasetService, config)).isTrue();

    URI uri = mock(URI.class);
    when(uri.getHost()).thenReturn("");
    assertThat(TigValidation1Dot8.validateHost(uri, results, datasetService, config)).isTrue();

    // Valid host, no queries
    when(config.isNoIpv4Queries()).thenReturn(true);
    when(config.isNoIpv6Queries()).thenReturn(true);
    URI uri2 = URI.create("http://example.com");
    assertThat(TigValidation1Dot8.validateHost(uri2, results, datasetService, config)).isTrue();
  }

  @Test
  public void testIPValidator_isInvalid_UnknownType() {
    TigValidation1Dot8.IPValidator validator = new TigValidation1Dot8.IPValidator();
    InetAddress unknown = mock(InetAddress.class);
    assertThat(validator.isInvalid(unknown, datasetService)).isTrue();
  }

  @Test
  public void testIPValidator_isInvalid_UnknownTypeAgain() {
    // Test the first case where it's neither IPv4 nor IPv6
    InetAddress unknown = mock(InetAddress.class);
    assertThat(new IPValidator().isInvalid(unknown, datasetService)).isTrue();
  }

  @Test
  public void testIPValidator_isInvalid_ValidIPv4() throws Exception {
    Inet4Address ipv4 = (Inet4Address) InetAddress.getByName("192.0.2.1"); // Valid IPv4 from TEST-NET-1 block

    RDAPDatasetService realDatasetService = mock(RDAPDatasetService.class);
    // Test with the real validator
    IPValidator ipValidator = new IPValidator();
    assertThat(ipValidator.isInvalid(ipv4, realDatasetService)).isFalse();
  }

  @Test
  public void testIPValidator_isInvalid_ValidIPv6() throws Exception {
    Inet6Address ipv6 = (Inet6Address) InetAddress.getByName("2001:db8::1"); // Valid IPv6 from documentation block
    RDAPDatasetService realDatasetService = mock(RDAPDatasetService.class);

    // Test with real validator
    IPValidator ipValidator = new IPValidator();
    assertThat(ipValidator.isInvalid(ipv6, realDatasetService)).isFalse();
  }
}