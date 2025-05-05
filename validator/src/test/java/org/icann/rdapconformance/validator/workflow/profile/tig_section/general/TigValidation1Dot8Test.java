package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.util.List;

import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TigValidation1Dot8Test extends ProfileValidationTestBase {

  private RDAPDatasetService datasetService;
  private final RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    datasetService = mock(RDAPDatasetService.class);
  }

  @Override
  public TigValidation1Dot8 getProfileValidation() {
    return new TigValidation1Dot8(null, results, datasetService, config);
  }

  @Test
  public void testValidateHost_ValidIPv4AndIPv6() throws Exception {
    String host = "example.com";
    InetAddress ipv4Address = Inet4Address.getByName("192.0.2.1");
    InetAddress ipv6Address = Inet6Address.getByName("2001:db8::1");

    mockDNSCacheResolver(host, List.of(ipv4Address), List.of(ipv6Address));

    URI uri = new URI("http://" + host);
    boolean isValid = TigValidation1Dot8.validateHost(uri, results, datasetService, config);

    assertThat(isValid).isTrue();
    verifyNoInteractions(results);
  }

  @Test
  public void testValidateHost_InvalidIPv4() throws Exception {
    String host = "example.com";
    InetAddress ipv6Address = Inet6Address.getByName("2001:db8::1");

    try (var mockedStatic = mockStatic(DNSCacheResolver.class)) {
      mockedStatic.when(() -> DNSCacheResolver.getAllV4Addresses(host)).thenReturn(List.of());
      mockedStatic.when(() -> DNSCacheResolver.getAllV6Addresses(host)).thenReturn(List.of(ipv6Address));

      URI uri = new URI("http://" + host);
      boolean isValid = TigValidation1Dot8.validateHost(uri, results, datasetService, config);

      assertThat(isValid).isFalse();
      verify(results).add(argThat(result ->
          result.getCode() == -20400 &&
              result.getMessage().contains("The RDAP service is not provided over IPv4")
      ));
    }
  }

  @Test
  public void testValidateHost_InvalidIPv6() throws Exception {
    String host = "example.com";
    InetAddress ipv4Address = Inet4Address.getByName("192.0.2.1");

    try (var mockedStatic = mockStatic(DNSCacheResolver.class)) {
      mockedStatic.when(() -> DNSCacheResolver.getAllV4Addresses(host)).thenReturn(List.of(ipv4Address));
      mockedStatic.when(() -> DNSCacheResolver.getAllV6Addresses(host)).thenReturn(List.of());

      URI uri = new URI("http://" + host);
      boolean isValid = TigValidation1Dot8.validateHost(uri, results, datasetService, config);

      assertThat(isValid).isFalse();
      verify(results).add(argThat(result ->
          result.getCode() == -20401 &&
              result.getMessage().contains("The RDAP service is not provided over IPv6")
      ));
    }
  }

  @Test
  public void testValidateHost_InvalidIPv4AndIPv6() throws Exception {
    String host = "example.com";

    try (var mockedStatic = mockStatic(DNSCacheResolver.class)) {
      mockedStatic.when(() -> DNSCacheResolver.getAllV4Addresses(host)).thenReturn(List.of());
      mockedStatic.when(() -> DNSCacheResolver.getAllV6Addresses(host)).thenReturn(List.of());

      URI uri = new URI("http://" + host);
      boolean isValid = TigValidation1Dot8.validateHost(uri, results, datasetService, config);

      assertThat(isValid).isFalse();
      verify(results).add(argThat(result ->
          result.getCode() == -20400 &&
              result.getMessage().contains("The RDAP service is not provided over IPv4")
      ));
      verify(results).add(argThat(result ->
          result.getCode() == -20401 &&
              result.getMessage().contains("The RDAP service is not provided over IPv6")
      ));
    }
  }

  private void mockDNSCacheResolver(String host, List<InetAddress> ipv4Addresses, List<InetAddress> ipv6Addresses) {
    try (var mockedStatic = mockStatic(DNSCacheResolver.class)) {
      mockedStatic.when(() -> DNSCacheResolver.getAllV4Addresses(host)).thenReturn(ipv4Addresses);
      mockedStatic.when(() -> DNSCacheResolver.getAllV6Addresses(host)).thenReturn(ipv6Addresses);
    }
  }
}