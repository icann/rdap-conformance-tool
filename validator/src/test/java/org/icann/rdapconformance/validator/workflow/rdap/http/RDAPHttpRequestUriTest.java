package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetAddress;
import java.net.URI;
import org.testng.annotations.Test;

public class RDAPHttpRequestUriTest {

    @Test
    public void testBuildIpUri_PreservesPercentEncodedSpace() throws Exception {
        // Regression: entity handles with spaces are percent-encoded as %20.
        // The multi-arg URI constructor previously re-encoded '%' to %25,
        // turning %20 into %2520 in the actual request.
        URI original = new URI(
                "https://ts-wire-mock.icann.org/rdap/v2/no_code/domain/Osir,%20Inc.");
        InetAddress ip = InetAddress.getByName("127.0.0.1");

        URI result = RDAPHttpRequest.buildIpUri(original, ip, 443);

        assertThat(result.getRawPath())
                .isEqualTo("/rdap/v2/no_code/domain/Osir,%20Inc.");
        assertThat(result.toASCIIString()).doesNotContain("%2520");
        assertThat(result.toASCIIString())
                .isEqualTo("https://127.0.0.1:443/rdap/v2/no_code/domain/Osir,%20Inc.");
    }

    @Test
    public void testBuildIpUri_PlainPathUnchanged() throws Exception {
        URI original = new URI("https://rdap.example.com/domain/example.com");
        InetAddress ip = InetAddress.getByName("93.184.216.34");

        URI result = RDAPHttpRequest.buildIpUri(original, ip, 443);

        assertThat(result.toASCIIString())
                .isEqualTo("https://93.184.216.34:443/domain/example.com");
    }

    @Test
    public void testBuildIpUri_Ipv6LiteralIsBracketed() throws Exception {
        URI original = new URI("https://example.com/domain/foo%20bar");
        InetAddress ip = InetAddress.getByName("2606:2800:220:1:248:1893:25c8:1946");

        URI result = RDAPHttpRequest.buildIpUri(original, ip, 443);

        assertThat(result.getHost())
                .isEqualTo("[2606:2800:220:1:248:1893:25c8:1946]");
        assertThat(result.getRawPath()).isEqualTo("/domain/foo%20bar");
    }

    @Test
    public void testBuildIpUri_PreservesQueryEncoding() throws Exception {
        URI original = new URI("https://example.com/domain/x?a=b%20c");
        InetAddress ip = InetAddress.getByName("127.0.0.1");

        URI result = RDAPHttpRequest.buildIpUri(original, ip, 80);

        assertThat(result.getRawQuery()).isEqualTo("a=b%20c");
        assertThat(result.toASCIIString()).doesNotContain("%2520");
    }
}