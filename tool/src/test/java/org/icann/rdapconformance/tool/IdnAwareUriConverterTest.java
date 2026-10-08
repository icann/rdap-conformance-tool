package org.icann.rdapconformance.tool;

import java.net.URI;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.*;

public class IdnAwareUriConverterTest {

    private final RdapConformanceTool.IdnAwareUriConverter converter =
            new RdapConformanceTool.IdnAwareUriConverter();

    // --- convertIdnInPath tests ---

    @DataProvider(name = "idnPathConversions")
    public Object[][] idnPathConversions() {
        return new Object[][] {
                // Unicode domain in path → punycode
                { "/rdap/domain/nic.дети", "/rdap/domain/nic.xn--d1acj3b" },
                // Percent-encoded Unicode → punycode
                { "/rdap/domain/nic.%D0%B4%D0%B5%D1%82%D0%B8", "/rdap/domain/nic.xn--d1acj3b" },
                // Already punycode → unchanged
                { "/rdap/domain/nic.xn--d1acj3b", "/rdap/domain/nic.xn--d1acj3b" },
                // ASCII domain → unchanged
                { "/rdap/domain/example.com", "/rdap/domain/example.com" },
                // Nameserver with IDN
                { "/rdap/nameserver/ns1.дети", "/rdap/nameserver/ns1.xn--d1acj3b" },
                // Non-domain path → unchanged
                { "/rdap/help", "/rdap/help" },
                // Unicode TLD only
                { "/rdap/domain/test.münchen", "/rdap/domain/test.xn--mnchen-3ya" },
                { "/rdap/domain/reallylongdnslabelthatislongerthan63characterswegowithinvalid064.registryok",
                "/rdap/domain/reallylongdnslabelthatislongerthan63characterswegowithinvalid064.registryok" },
        };
    }

    @Test(dataProvider = "idnPathConversions")
    public void testConvertIdnInPath(String input, String expected) {
        String result = RdapConformanceTool.IdnAwareUriConverter.convertIdnInPath(input);
        assertThat(result).isEqualTo(expected);
    }

    // --- Full URI conversion tests ---

    @DataProvider(name = "idnUriConversions")
    public Object[][] idnUriConversions() {
        return new Object[][] {
                // Unicode domain in path
                {
                        "https://whois.nic.xn--d1acj3b/rdap/domain/nic.дети",
                        "https://whois.nic.xn--d1acj3b/rdap/domain/nic.xn--d1acj3b"
                },
                // Percent-encoded Unicode
                {
                        "https://whois.nic.xn--d1acj3b/rdap/domain/nic.%D0%B4%D0%B5%D1%82%D0%B8",
                        "https://whois.nic.xn--d1acj3b/rdap/domain/nic.xn--d1acj3b"
                },
                // Already ASCII — no change
                {
                        "https://rdap.example.com/rdap/domain/example.com",
                        "https://rdap.example.com/rdap/domain/example.com"
                },
                // Unicode in host AND path
                {
                        "https://whois.nic.дети/rdap/domain/nic.дети",
                        "https://whois.nic.xn--d1acj3b/rdap/domain/nic.xn--d1acj3b"
                },
                // Label > 63 chars in full URI — must not throw, passes through as-is
                {
                        "https://ts-wire-mock.icann.org/rdap/v2/code_10202/domain/reallylongdnslabelthatislongerthan63characterswegowithinvalid064.registryok",
                        "https://ts-wire-mock.icann.org/rdap/v2/code_10202/domain/reallylongdnslabelthatislongerthan63characterswegowithinvalid064.registryok"
                },
        };
    }

    @Test(dataProvider = "idnUriConversions")
    public void testConvertFullUri(String input, String expected) throws Exception {
        URI result = converter.convert(input);
        assertThat(result.toString()).isEqualTo(expected);
    }

    // --- Picocli integration test ---

    @Test
    public void testPicocliParsesIdnUri() {
        RdapConformanceTool tool = new RdapConformanceTool();
        CommandLine cmd = new CommandLine(tool);
        cmd.registerConverter(URI.class, new RdapConformanceTool.IdnAwareUriConverter());

        String[] args = {
                "https://whois.nic.xn--d1acj3b/rdap/domain/nic.дети",
                "--config=/tmp/test",
                "--gtld-registrar"
        };

        assertThatCode(() -> cmd.parseArgs(args)).doesNotThrowAnyException();
        assertThat(tool.getUri().toString())
                .isEqualTo("https://whois.nic.xn--d1acj3b/rdap/domain/nic.xn--d1acj3b");
    }

    // --- Edge cases ---

    @Test(expectedExceptions = Exception.class)
    public void testMissingSchemeFails() throws Exception {
        converter.convert("whois.nic.xn--d1acj3b/rdap/domain/nic.дети");
    }

    @Test
    public void testUriWithPort() throws Exception {
        URI result = converter.convert("https://example.com:8443/rdap/domain/nic.дети");
        assertThat(result.getPort()).isEqualTo(8443);
        assertThat(result.getPath()).isEqualTo("/rdap/domain/nic.xn--d1acj3b");
    }

    @Test
    public void testUriWithQueryString() throws Exception {
        URI result = converter.convert("https://example.com/rdap/domain/nic.дети?foo=bar");
        assertThat(result.getPath()).isEqualTo("/rdap/domain/nic.xn--d1acj3b");
        assertThat(result.getQuery()).isEqualTo("foo=bar");
    }

    /**
     * A domain label longer than 63 characters must pass through as-is
     * so that downstream validation can report -10300.
     * Before the fix, toASCII() threw IllegalArgumentException causing exit 25.
     **/
    @Test
    public void testConvertIdnInPath_LabelTooLong_PassesThrough() {
        String longLabel = "reallylongdnslabelthatislongerthan63characterswegowithinvalid064";
        String path = "/rdap/domain/" + longLabel + ".registryok";
        String result = RdapConformanceTool.IdnAwareUriConverter.convertIdnInPath(path);
        // Must not throw, must return the path unchanged
        assertThat(result).isEqualTo(path);
    }

    @Test
    public void testConvertUriWithSpacesInEntityHandle() throws Exception {
        // Regression: entity handles may contain spaces (RFC 9082). The converter
        // previously encoded only non-ASCII, so a literal space reached
        // java.net.URI and threw "Illegal character in path at index 52".
        URI result = converter.convert(
                "https://tld-rdap.verisign.com/verisign/v1/entity/CSC Corporate Domains, Inc");

        assertThat(result.toASCIIString())
                .isEqualTo("https://tld-rdap.verisign.com/verisign/v1/entity/CSC%20Corporate%20Domains,%20Inc");
    }

    @Test
    public void testConvertUriWithPreEncodedSpacesIsNotDoubleEncoded() throws Exception {
        URI result = converter.convert(
                "https://rdap.verisign.com/com/v1/entity/MarkMonitor%20Inc.");

        assertThat(result.toASCIIString())
                .isEqualTo("https://rdap.verisign.com/com/v1/entity/MarkMonitor%20Inc.");
    }

    @Test
    public void testConvertUriWithSpacesDoesNotThrow() {
        assertThatCode(() -> converter.convert(
                "https://rdap.verisign.com/com/v1/entity/NameCheap, Inc."))
                .doesNotThrowAnyException();
    }

    @Test
    public void convert_missingScheme_throws() {
        assertThatThrownBy(() -> new RdapConformanceTool.IdnAwareUriConverter()
                .convert("example.com/domain/test"))
                .isInstanceOf(java.net.URISyntaxException.class);
    }

    @Test
    public void convert_ipv6Literal_leftAsIs() throws Exception {
        URI uri = new RdapConformanceTool.IdnAwareUriConverter()
                .convert("https://[2001:db8::1]:8443/domain/example.com");
        assertThat(uri.getPort()).isEqualTo(8443);
    }

    @Test
    public void convert_hostWithPort_preservesPort() throws Exception {
        URI uri = new RdapConformanceTool.IdnAwareUriConverter()
                .convert("https://rdap.example.com:8080/domain/test.example");
        assertThat(uri.getPort()).isEqualTo(8080);
        assertThat(uri.getHost()).isEqualTo("rdap.example.com");
    }

    @Test
    public void convert_colonButNotPort_notTreatedAsPort() throws Exception {
        // "abc" after colon -> NumberFormatException branch -> treated as host
        URI uri = new RdapConformanceTool.IdnAwareUriConverter()
                .convert("https://rdap.example.com/domain/test.example?x=a:b");
        assertThat(uri.getHost()).isEqualTo("rdap.example.com");
    }

    @Test
    public void convert_unicodeHost_convertsToPunycode() throws Exception {
        URI uri = new RdapConformanceTool.IdnAwareUriConverter()
                .convert("https://nic.дети/domain/test.example");
        assertThat(uri.getHost()).isEqualTo("nic.xn--d1acj3b");
    }

    @Test
    public void convert_unicodeDomainInPath_convertsToPunycode() throws Exception {
        URI uri = new RdapConformanceTool.IdnAwareUriConverter()
                .convert("https://rdap.example.com/domain/пример.com");
        assertThat(uri.getPath()).contains("xn--");
    }

    @Test
    public void convert_nameserverPrefix_alsoConverted() throws Exception {
        URI uri = new RdapConformanceTool.IdnAwareUriConverter()
                .convert("https://rdap.example.com/nameserver/ns1.пример.com");
        assertThat(uri.getPath()).startsWith("/nameserver/");
        assertThat(uri.getPath()).contains("xn--");
    }

    @Test
    public void convert_percentEncodedUnicodeInPath_decodedThenConverted() throws Exception {
        URI uri = new RdapConformanceTool.IdnAwareUriConverter()
                .convert("https://rdap.example.com/domain/nic.%D0%B4%D0%B5%D1%82%D0%B8");
        assertThat(uri.getPath()).contains("xn--d1acj3b");
    }

    @Test
    public void convert_spacesAndIllegalChars_percentEncoded() throws Exception {
        URI uri = new RdapConformanceTool.IdnAwareUriConverter()
                .convert("https://rdap.example.com/entity/CSC Corporate Domains, Inc");
        assertThat(uri.getRawPath()).contains("CSC%20Corporate");
        assertThat(uri.getPath()).contains("CSC Corporate");
    }

    @Test
    public void convert_pathWithoutRdapPrefix_returnedDecoded() throws Exception {
        URI uri = new RdapConformanceTool.IdnAwareUriConverter()
                .convert("https://rdap.example.com/help");
        assertThat(uri.getPath()).isEqualTo("/help");
    }

    @Test
    public void convert_noPath_hostOnly() throws Exception {
        URI uri = new RdapConformanceTool.IdnAwareUriConverter()
                .convert("https://rdap.example.com");
        assertThat(uri.getHost()).isEqualTo("rdap.example.com");
    }

    @Test
    public void convertIdnInPath_mixedLabels_percentEncodedNotPunycode() {
        // Mix of A-label (xn--...) and U-label (дети) in the same domain name
        String result = RdapConformanceTool.IdnAwareUriConverter
                .convertIdnInPath("/domain/xn--d1acj3b.дети");
        assertThat(result).contains("%");                 // U-label percent-encoded
        assertThat(result).startsWith("/domain/xn--d1acj3b.");
    }

    @Test
    public void convertIdnInPath_overlongLabel_leftAsIs() {
        String longLabel = "a".repeat(64) + ".com"; // label > 63 chars -> toASCII throws
        String result = RdapConformanceTool.IdnAwareUriConverter
                .convertIdnInPath("/domain/" + longLabel);
        assertThat(result).isEqualTo("/domain/" + longLabel);
    }

    @Test
    public void convert_ipv6LiteralWithoutPort() throws Exception {
        URI uri = new RdapConformanceTool.IdnAwareUriConverter()
                .convert("https://[2001:db8::1]/domain/example.com");
        assertThat(uri.getHost()).contains("2001:db8::1");
    }

    @Test
    public void convertIdnInPath_invalidPercentSequence_fallsBackToRawPath() {
        // Malformed %-encoding triggers the decode() catch branch
        String result = RdapConformanceTool.IdnAwareUriConverter
                .convertIdnInPath("/domain/bad%ZZencoding.com");
        assertThat(result).isNotNull();
    }

    @Test
    public void convert_queryAndFragmentPreserved() throws Exception {
        URI uri = new RdapConformanceTool.IdnAwareUriConverter()
                .convert("https://rdap.example.com/domain/test.example?a=b#frag");
        assertThat(uri.getQuery()).isEqualTo("a=b");
        assertThat(uri.getFragment()).isEqualTo("frag");
    }

    @Test
    public void convert_hostLabelTooLong_keptAsIs() throws Exception {
        String longHost = "a".repeat(64) + ".example.com";
        URI uri = new RdapConformanceTool.IdnAwareUriConverter()
                .convert("https://" + longHost + "/help");
        assertThat(uri).isNotNull(); // toASCII IllegalArgumentException branch
    }

    @Test
    public void convert_colonSuffixNotNumeric_treatedAsHost() throws Exception {
        // ":notaport" -> NumberFormatException branch -> whole string treated as host
        URI uri = new RdapConformanceTool.IdnAwareUriConverter()
                .convert("https://rdap.example.com:notaport/help");
        assertThat(uri).isNotNull();
    }

    @Test
    public void convert_eachIllegalAsciiChar_percentEncoded() throws Exception {
        String[] paths = {
                "a\"b", "a<b", "a>b", "a\\b", "a^b", "a`b", "a{b", "a|b", "a}b",
        };
        for (String p : paths) {
            URI uri = new RdapConformanceTool.IdnAwareUriConverter()
                    .convert("https://rdap.example.com/entity/" + p);
            assertThat(uri.getRawPath()).contains("%"); // each char hits a different || branch
        }
    }

    @Test
    public void convert_controlChars_percentEncoded() throws Exception {
        // \u0001 -> c < 0x20 branch; \u007F -> c == 0x7F branch
        URI uri1 = new RdapConformanceTool.IdnAwareUriConverter()
                .convert("https://rdap.example.com/entity/a\u0001b");
        assertThat(uri1.getRawPath()).contains("%01");

        URI uri2 = new RdapConformanceTool.IdnAwareUriConverter()
                .convert("https://rdap.example.com/entity/a\u007Fb");
        assertThat(uri2.getRawPath()).contains("%7F");
    }
}