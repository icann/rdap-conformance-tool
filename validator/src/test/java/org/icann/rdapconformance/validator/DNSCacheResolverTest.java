package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetAddress;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DNSCacheResolverTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {CommonUtils.LOCAL_IPV4, CommonUtils.LOCAL_IPV6_COMPRESSED, CommonUtils.LOCALHOST})
  public void customLocalhostDnsServer_skipsReachabilityCheck(String dnsServer) throws Exception {
    // A localhost DNS server (nothing listening) must not trigger the reachability health check
    DNSCacheResolver resolver = new DNSCacheResolver(dnsServer);

    assertThat(resolver.getAllV4Addresses(CommonUtils.LOCALHOST))
        .containsExactly(InetAddress.getByName(CommonUtils.LOCAL_IPV4));
  }

  @Test
  public void loopbackNames_areResolvedWithoutDns() throws Exception {
    DNSCacheResolver resolver = new DNSCacheResolver(CommonUtils.LOCAL_IPV4);

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
