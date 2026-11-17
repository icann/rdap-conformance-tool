package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xbill.DNS.Message;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.Section;

public class DNSCacheResolverTest {

  /** Replaces the real dnsjava resolver with one that answers immediately with an empty response. */
  private static Resolver stubEmptyDns(DNSCacheResolver resolver) throws Exception {
    Resolver dns = mock(Resolver.class);
    when(dns.send(any(Message.class))).thenAnswer(inv -> {
      Message query = inv.getArgument(0);
      Message response = new Message(query.getHeader().getID());
      response.addRecord(query.getQuestion(), Section.QUESTION);
      return response;
    });
    Field field = DNSCacheResolver.class.getDeclaredField("resolver");
    field.setAccessible(true);
    field.set(resolver, dns);
    return dns;
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {CommonUtils.LOCAL_IPV4, CommonUtils.LOCAL_IPV6_COMPRESSED, CommonUtils.LOCALHOST})
  public void customLocalhostDnsServer_skipsReachabilityCheck(String dnsServer) throws Exception {
    // A localhost DNS server (nothing listening) must not trigger the reachability health check
    DNSCacheResolver resolver = new DNSCacheResolver(dnsServer);
    Resolver dns = stubEmptyDns(resolver);

    assertThat(resolver.getAllV4Addresses(CommonUtils.LOCALHOST))
        .containsExactly(InetAddress.getByName(CommonUtils.LOCAL_IPV4));
    verify(dns, never()).send(any(Message.class));
  }

  @Test
  public void loopbackNames_areResolvedWithoutDns() throws Exception {
    DNSCacheResolver resolver = new DNSCacheResolver(CommonUtils.LOCAL_IPV4);
    Resolver dns = stubEmptyDns(resolver);

    assertThat(resolver.getAllV4Addresses(CommonUtils.LOCALHOST))
        .containsExactly(InetAddress.getByName(CommonUtils.LOCAL_IPV4));
    assertThat(resolver.getAllV6Addresses(CommonUtils.LOCALHOST))
        .containsExactly(InetAddress.getByName(CommonUtils.LOCAL_IPV6_COMPRESSED));
    assertThat(resolver.getFirstV4Address(CommonUtils.LOCAL_IPV4))
        .isEqualTo(InetAddress.getByName(CommonUtils.LOCAL_IPV4));
    assertThat(resolver.getFirstV6Address(CommonUtils.LOCAL_IPV6))
        .isEqualTo(InetAddress.getByName(CommonUtils.LOCAL_IPV6_COMPRESSED));
    assertThat(resolver.hasNoAddresses(CommonUtils.LOCALHOST)).isFalse();
    // Second lookup is served from cache
    assertThat(resolver.getAllV4Addresses(CommonUtils.LOCALHOST)).hasSize(1);
    // Only the opposite address family of each numeric literal goes to DNS (AAAA for 127.0.0.1, A for ::1)
    verify(dns, org.mockito.Mockito.times(2)).send(any(Message.class));
    assertThat(resolver.getAllV6Addresses(CommonUtils.LOCAL_IPV4)).isEmpty();
    assertThat(resolver.getAllV4Addresses(CommonUtils.LOCAL_IPV6)).isEmpty();
  }

  @Test
  public void unknownName_withEmptyDnsAnswer_hasNoAddresses() throws Exception {
    DNSCacheResolver resolver = new DNSCacheResolver(CommonUtils.LOCAL_IPV4);
    stubEmptyDns(resolver);

    assertThat(resolver.hasNoAddresses("nonexistent.example")).isTrue();
    assertThat(resolver.getFirstV4Address("nonexistent.example")).isNull();
    assertThat(resolver.getFirstV6Address("nonexistent.example")).isNull();
  }

  @Test
  public void ensureFQDN_appendsTrailingDotOnce() {
    assertThat(DNSCacheResolver.ensureFQDN("example.com")).isEqualTo("example.com.");
    assertThat(DNSCacheResolver.ensureFQDN("example.com.")).isEqualTo("example.com.");
  }

  @Test
  public void getHostnameFromUrl() {
    assertThat(DNSCacheResolver.getHostnameFromUrl("https://rdap.example.com:8443/domain/x"))
        .isEqualTo("rdap.example.com");
  }

  @Test
  public void getFirst_returnsNullWhenMissing() {
    assertThat(DNSCacheResolver.getFirst(java.util.Map.of(), "missing.")).isNull();
    assertThat(DNSCacheResolver.getFirst(java.util.Map.of("x.", List.of()), "x.")).isNull();
  }
}
