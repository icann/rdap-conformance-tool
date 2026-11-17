package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SsrfBlocklistTest {

    private static InetAddress ip(String s) throws Exception {
        return InetAddress.getByName(s);
    }

    @Test
    public void empty_blocksOnlyBuiltInRanges() throws Exception {
        SsrfBlocklist bl = SsrfBlocklist.empty();
        assertThat(bl.size()).isZero();
        assertThat(bl.isBlocked(ip("127.0.0.1"))).isTrue();
        assertThat(bl.isBlocked(ip("10.1.2.3"))).isTrue();
        assertThat(bl.isBlocked(ip("172.16.0.1"))).isTrue();
        assertThat(bl.isBlocked(ip("192.168.0.1"))).isTrue();
        assertThat(bl.isBlocked(ip("169.254.169.254"))).isTrue();
        assertThat(bl.isBlocked(ip("0.0.0.0"))).isTrue();
        assertThat(bl.isBlocked(ip("::1"))).isTrue();
        assertThat(bl.isBlocked(ip("fd00::1"))).isTrue();
        assertThat(bl.isBlocked(ip("fe80::1"))).isTrue();
        assertThat(bl.isBlocked(ip("8.8.8.8"))).isFalse();
        assertThat(bl.isBlocked(ip("2001:4860:4860::8888"))).isFalse();
        assertThat(bl.isInConfiguredRange(ip("8.8.8.8"))).isFalse();
    }

    @Test
    public void of_blocksPublicAddressesInsideConfiguredCidrs() throws Exception {
        SsrfBlocklist bl = SsrfBlocklist.of(List.of("192.0.2.0/24", "2001:db8::/32", "198.51.100.7"));
        assertThat(bl.size()).isEqualTo(3);

        assertThat(bl.isBlocked(ip("192.0.2.0"))).isTrue();
        assertThat(bl.isBlocked(ip("192.0.2.255"))).isTrue();
        assertThat(bl.isBlocked(ip("192.0.3.0"))).isFalse();

        assertThat(bl.isBlocked(ip("2001:db8::1"))).isTrue();
        assertThat(bl.isBlocked(ip("2001:db8:ffff:ffff:ffff:ffff:ffff:ffff"))).isTrue();
        assertThat(bl.isBlocked(ip("2001:db9::1"))).isFalse();

        // bare IP acts as /32
        assertThat(bl.isBlocked(ip("198.51.100.7"))).isTrue();
        assertThat(bl.isBlocked(ip("198.51.100.8"))).isFalse();
    }

    @Test
    public void of_hostBitsInPrefixAreNormalisedToNetwork() throws Exception {
        SsrfBlocklist bl = SsrfBlocklist.of(List.of("192.0.2.77/24"));
        assertThat(bl.isBlocked(ip("192.0.2.1"))).isTrue();
    }

    @Test
    public void of_ipv4MappedIpv6MatchesIpv4Cidr() throws Exception {
        SsrfBlocklist bl = SsrfBlocklist.of(List.of("192.0.2.0/24"));
        assertThat(bl.isInConfiguredRange(ip("::ffff:192.0.2.9"))).isTrue();
    }

    @Test
    public void of_ignoresBlankLinesAndComments() throws Exception {
        SsrfBlocklist bl = SsrfBlocklist.of(List.of("", "   ", "# comment", "192.0.2.0/24 # trailing"));
        assertThat(bl.size()).isEqualTo(1);
        assertThat(bl.isBlocked(ip("192.0.2.5"))).isTrue();
    }

    @Test
    public void of_rejectsInvalidEntries() {
        assertThatThrownBy(() -> SsrfBlocklist.of(List.of("not-a-cidr")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not-a-cidr");
        assertThatThrownBy(() -> SsrfBlocklist.of(List.of("192.0.2.0/33")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SsrfBlocklist.of(List.of("example.com")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void of_nullOrEmptyReturnsEmpty() {
        assertThat(SsrfBlocklist.of(null).size()).isZero();
        assertThat(SsrfBlocklist.of(List.of()).size()).isZero();
    }

    @Test
    public void readCidrFile_parsesLinesAndReportsBadLineNumber(@TempDir Path dir) throws Exception {
        Path ok = dir.resolve("ok.txt");
        Files.writeString(ok, "# org space\n192.0.2.0/24\n\n2001:db8::/32   # v6\n");
        assertThat(SsrfBlocklist.readCidrFile(ok)).containsExactly("192.0.2.0/24", "2001:db8::/32");

        Path bad = dir.resolve("bad.txt");
        Files.writeString(bad, "192.0.2.0/24\nbogus\n");
        assertThatThrownBy(() -> SsrfBlocklist.readCidrFile(bad))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bad.txt:2");
    }

    @Test
    public void isPrivateOrReserved_staticHelpers() throws Exception {
        assertThat(SsrfBlocklist.isPrivateOrReserved(null)).isFalse();
        assertThat(SsrfBlocklist.isIPv6UniqueLocalAddress(ip("fc00::1"))).isTrue();
        assertThat(SsrfBlocklist.isIPv6UniqueLocalAddress(ip("fd12::1"))).isTrue();
        assertThat(SsrfBlocklist.isIPv6UniqueLocalAddress(ip("fec0::1"))).isFalse();
        assertThat(SsrfBlocklist.isIPv6UniqueLocalAddress(ip("10.0.0.1"))).isFalse();
    }
}
