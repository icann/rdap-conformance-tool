package org.icann.rdapconformance.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.net.URI;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import picocli.CommandLine;

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
}