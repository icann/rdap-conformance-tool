package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils.givenChainedHttpRedirects;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpResponse;
import java.util.Set;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot8.DNSQuery;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot8.DNSQuery.DNSQueryResult;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot8.IPValidator;
import org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils.RedirectData;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class Validation1Dot8Test {

  private final DNSQuery dnsQuery = mock(DNSQuery.class);
  private final IPValidator ipValidator = mock(IPValidator.class);
  private final RDAPDatasetService datasetService = mock(RDAPDatasetService.class);


  private void givenV6Ok() throws UnknownHostException {
    InetAddress ipv6AddressValid = Inet6Address
        .getByAddress(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1});
    DNSQueryResult resultV6 = mock(DNSQueryResult.class);
    doReturn(false).when(resultV6).hasError();
    doReturn(resultV6).when(dnsQuery).makeRequest(any(Name.class), eq(Type.AAAA));
    doReturn(Set.of(ipv6AddressValid)).when(resultV6).getIPAddresses();
  }

  private void givenV4Ok() throws UnknownHostException {
    InetAddress ipv4AddressValid = Inet4Address.getByAddress(new byte[]{127, 0, 0, 1});
    DNSQueryResult resultV4 = mock(DNSQueryResult.class);
    doReturn(false).when(resultV4).hasError();
    doReturn(resultV4).when(dnsQuery).makeRequest(any(Name.class), eq(Type.A));
    doReturn(Set.of(ipv4AddressValid)).when(resultV4).getIPAddresses();
  }

  private void givenV4AddressError(URI uri) throws UnknownHostException, TextParseException {
    InetAddress ipv4AddressValid = Inet4Address.getByAddress(new byte[]{127, 0, 0, 1});
    InetAddress ipv4AddressInvalid = Inet4Address.getByAddress(new byte[]{127, 0, 0, 2});
    DNSQueryResult resultV4 = mock(DNSQueryResult.class);
    doReturn(resultV4).when(dnsQuery).makeRequest(Name.fromString(uri.getHost()), Type.A);
    doReturn(false).when(resultV4).hasError();
    doReturn(Set.of(ipv4AddressInvalid, ipv4AddressValid)).when(resultV4).getIPAddresses();
    doReturn(true).when(ipValidator).isInvalid(ipv4AddressInvalid, datasetService);
  }

  private void givenV4QueryError() throws UnknownHostException {
    InetAddress ipv4AddressValid = Inet4Address.getByAddress(new byte[]{127, 0, 0, 1});
    DNSQueryResult resultV4 = mock(DNSQueryResult.class);
    doReturn(resultV4).when(dnsQuery).makeRequest(any(Name.class), eq(Type.A));
    doReturn(true).when(resultV4).hasError();

    doReturn(Set.of(ipv4AddressValid)).when(resultV4).getIPAddresses();
  }

  private void givenV6AddressError(URI uri) throws UnknownHostException, TextParseException {
    InetAddress ipv6AddressValid = Inet6Address
        .getByAddress(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1});

    InetAddress ipv6AddressInvalid = Inet6Address
        .getByAddress(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2});

    DNSQueryResult resultV6 = mock(DNSQueryResult.class);
    doReturn(resultV6).when(dnsQuery).makeRequest(Name.fromString(uri.getHost()), Type.AAAA);
    doReturn(false).when(resultV6).hasError();
    doReturn(Set.of(ipv6AddressInvalid, ipv6AddressValid)).when(resultV6).getIPAddresses();
    doReturn(true).when(ipValidator).isInvalid(ipv6AddressInvalid, datasetService);
  }

  private void givenV6QueryError() throws UnknownHostException {
    InetAddress ipv6AddressValid = Inet6Address
        .getByAddress(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1});
    DNSQueryResult resultV6 = mock(DNSQueryResult.class);
    doReturn(resultV6).when(dnsQuery).makeRequest(any(Name.class), eq(Type.AAAA));
    doReturn(true).when(resultV6).hasError();

    doReturn(Set.of(ipv6AddressValid)).when(resultV6).getIPAddresses();
  }

  @BeforeMethod
  public void setUp() {
    Validation1Dot8.dnsQuery = dnsQuery;
    Validation1Dot8.ipValidator = ipValidator;
    doReturn(false).when(ipValidator).isInvalid(any(InetAddress.class), eq(datasetService));

  }

  @Test
  public void testValidate() throws UnknownHostException {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);

    HttpResponse<String> httpResponse = givenHttpResponse();
    givenV4Ok();
    givenV6Ok();

    assertThat(Validation1Dot8.validate(httpResponse, results, datasetService)).isTrue();
    verifyNoInteractions(results);
  }

  @Test
  public void testValidate_InvalidIPv4_AddResult20400()
      throws UnknownHostException, TextParseException {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);

    HttpResponse<String> httpResponse = givenHttpResponse();
    givenV4AddressError(httpResponse.uri());
    givenV6Ok();

    assertThat(Validation1Dot8.validate(httpResponse, results, datasetService)).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -20400)
        .hasFieldOrPropertyWithValue("value", "127.0.0.1, 127.0.0.2")
        .hasFieldOrPropertyWithValue("message",
            "The RDAP service is not provided over IPv4. See section 1.8 of the "
                + "RDAP_Technical_Implementation_Guide_2_1.");
  }

  @Test
  public void testValidate_InvalidIPv4DnsResponse_AddResult20400()
      throws UnknownHostException {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);

    HttpResponse<String> httpResponse = givenHttpResponse();
    givenV4QueryError();
    givenV6Ok();

    assertThat(Validation1Dot8.validate(httpResponse, results, datasetService)).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -20400)
        .hasFieldOrPropertyWithValue("value", "127.0.0.1")
        .hasFieldOrPropertyWithValue("message",
            "The RDAP service is not provided over IPv4. See section 1.8 of the "
                + "RDAP_Technical_Implementation_Guide_2_1.");
  }

  private HttpResponse<String> givenHttpResponse() {
    HttpResponse<String> httpsResponse = mock(HttpResponse.class);
    URI uri = URI.create("http://domain/test.example");

    doReturn(uri).when(httpsResponse).uri();
    return httpsResponse;
  }

  @Test
  public void testValidate_InvalidIPv4InRedirect_AddResult20400()
      throws UnknownHostException, TextParseException {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);

    RedirectData redirectData = givenChainedHttpRedirects();
    givenV4Ok();
    givenV4AddressError(URI.create(redirectData.endingPath));
    givenV6Ok();

    assertThat(Validation1Dot8.validate(redirectData.startingResponse, results, datasetService))
        .isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -20400)
        .hasFieldOrPropertyWithValue("value", "127.0.0.1, 127.0.0.2")
        .hasFieldOrPropertyWithValue("message",
            "The RDAP service is not provided over IPv4. See section 1.8 of the "
                + "RDAP_Technical_Implementation_Guide_2_1.");
  }

  @Test
  public void testValidate_InvalidIPv6_AddResult20401()
      throws UnknownHostException, TextParseException {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);

    HttpResponse<String> httpResponse = givenHttpResponse();
    givenV4Ok();
    givenV6AddressError(httpResponse.uri());

    assertThat(Validation1Dot8.validate(httpResponse, results, datasetService)).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -20401)
        .hasFieldOrPropertyWithValue("value", "0:0:0:0:0:0:0:1, 0:0:0:0:0:0:0:2")
        .hasFieldOrPropertyWithValue("message",
            "The RDAP service is not provided over IPv6. See section 1.8 of the "
                + "RDAP_Technical_Implementation_Guide_2_1.");
  }


  @Test
  public void testValidate_InvalidIPv6DnsResponse_AddResult20401() throws UnknownHostException {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);

    HttpResponse<String> httpResponse = givenHttpResponse();
    givenV4Ok();
    givenV6QueryError();

    assertThat(Validation1Dot8.validate(httpResponse, results, datasetService)).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -20401)
        .hasFieldOrPropertyWithValue("value", "0:0:0:0:0:0:0:1")
        .hasFieldOrPropertyWithValue("message",
            "The RDAP service is not provided over IPv6. See section 1.8 of the "
                + "RDAP_Technical_Implementation_Guide_2_1.");
  }

  @Test
  public void testValidate_InvalidIPv6InRedirect_AddResult20401()
      throws TextParseException, UnknownHostException {
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
        .forClass(RDAPValidationResult.class);

    RedirectData redirectData = givenChainedHttpRedirects();
    givenV4Ok();
    givenV6Ok();
    givenV6AddressError(URI.create(redirectData.endingPath));

    assertThat(Validation1Dot8.validate(redirectData.startingResponse, results, datasetService))
        .isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -20401)
        .hasFieldOrPropertyWithValue("value", "0:0:0:0:0:0:0:1, 0:0:0:0:0:0:0:2")
        .hasFieldOrPropertyWithValue("message",
            "The RDAP service is not provided over IPv6. See section 1.8 of the "
                + "RDAP_Technical_Implementation_Guide_2_1.");
  }


}