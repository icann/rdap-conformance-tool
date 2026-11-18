package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SsrfHostBlocklistTest {

    @TempDir
    Path tmp;

    // ---------------------------------------------------------------- parser

    @Test
    public void of_acceptsExactAndWildcardEntries() {
        SsrfHostBlocklist list = SsrfHostBlocklist.of(List.of("icann.org", "*.svc.cluster.local"));
        assertThat(list.size()).isEqualTo(2);
    }

    @Test
    public void of_ignoresBlanksAndComments() {
        SsrfHostBlocklist list = SsrfHostBlocklist.of(List.of("", "   ", "# comment", "icann.org # inline"));
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.isBlocked("icann.org")).isTrue();
    }

    @Test
    public void of_nullOrEmptyGivesEmptyList() {
        assertThat(SsrfHostBlocklist.of(null).size()).isZero();
        assertThat(SsrfHostBlocklist.of(List.of()).size()).isZero();
        assertThat(SsrfHostBlocklist.empty().isBlocked("anything.example")).isFalse();
    }

    @Test
    public void of_normalisesCaseTrailingDotAndIdn() {
        SsrfHostBlocklist list = SsrfHostBlocklist.of(List.of("ICANN.ORG.", "*.München.Example"));
        assertThat(list.isBlocked("icann.org")).isTrue();
        assertThat(list.isBlocked("www.xn--mnchen-3ya.example")).isTrue();
        assertThat(list.isBlocked("www.münchen.example")).isTrue();
    }

    @Test
    public void of_rejectsWildcardInNonLeadingPosition() {
        for (String bad : List.of("foo*.bar", "*.*", "*", "a.*.b", "*foo.bar", "*.")) {
            assertThatThrownBy(() -> SsrfHostBlocklist.of(List.of(bad)))
                    .as(bad)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(bad);
        }
    }

    @Test
    public void of_rejectsIpLiterals() {
        for (String bad : List.of("10.0.0.1", "192.0.2.0", "::1", "[2001:db8::1]", "*.10.0.0.1")) {
            assertThatThrownBy(() -> SsrfHostBlocklist.of(List.of(bad)))
                    .as(bad)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CIDR blocklist");
        }
    }

    @Test
    public void of_rejectsInvalidDnsLabels() {
        for (String bad : List.of("-bad.example", "bad-.example", "under_score.example",
                "a..b", "sp ace.example", "x".repeat(64) + ".example")) {
            assertThatThrownBy(() -> SsrfHostBlocklist.of(List.of(bad)))
                    .as(bad)
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    public void of_rejectsNamesLongerThan253() {
        String label = "a".repeat(63);
        String tooLong = String.join(".", label, label, label, label, "abc");
        assertThatThrownBy(() -> SsrfHostBlocklist.of(List.of(tooLong)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("too long");
    }

    // --------------------------------------------------------------- matcher

    @Test
    public void isBlocked_exactEntryMatchesOnlyThatName() {
        SsrfHostBlocklist list = SsrfHostBlocklist.of(List.of("icann.org"));
        assertThat(list.isBlocked("icann.org")).isTrue();
        assertThat(list.isBlocked("ICANN.ORG.")).isTrue();
        assertThat(list.isBlocked("www.icann.org")).isFalse();
        assertThat(list.isBlocked("evilicann.org")).isFalse();
    }

    @Test
    public void isBlocked_wildcardCoversSubdomainsAtAnyDepthButNotApex() {
        SsrfHostBlocklist list = SsrfHostBlocklist.of(List.of("*.icann.org"));
        assertThat(list.isBlocked("www.icann.org")).isTrue();
        assertThat(list.isBlocked("a.b.c.icann.org")).isTrue();
        assertThat(list.isBlocked("WWW.ICANN.ORG")).isTrue();
        assertThat(list.isBlocked("www.icann.org.")).isTrue();
        assertThat(list.isBlocked("icann.org")).as("apex is not covered by the wildcard").isFalse();
    }

    @Test
    public void isBlocked_respectsLabelBoundaries() {
        SsrfHostBlocklist list = SsrfHostBlocklist.of(List.of("*.icann.org", "icann.org"));
        assertThat(list.isBlocked("evilicann.org")).isFalse();
        assertThat(list.isBlocked("www.evilicann.org")).isFalse();
        assertThat(list.isBlocked("icann.org.evil.com")).isFalse();
        assertThat(list.isBlocked("xicann.org")).isFalse();
    }

    @Test
    public void isBlocked_ipLiteralsAndGarbageNeverMatch() {
        SsrfHostBlocklist list = SsrfHostBlocklist.of(List.of("*.org", "org"));
        assertThat(list.isBlocked("10.0.0.1")).isFalse();
        assertThat(list.isBlocked("[::1]")).isFalse();
        assertThat(list.isBlocked(null)).isFalse();
        assertThat(list.isBlocked("")).isFalse();
        assertThat(list.isBlocked("   ")).isFalse();
    }

    @Test
    public void isBlocked_svcClusterLocalWildcard() {
        SsrfHostBlocklist list = SsrfHostBlocklist.of(List.of("*.svc.cluster.local"));
        assertThat(list.isBlocked("api.default.svc.cluster.local")).isTrue();
        assertThat(list.isBlocked("svc.cluster.local")).isFalse();
        assertThat(list.isBlocked("cluster.local")).isFalse();
    }

    // ------------------------------------------------------------ file loader

    @Test
    public void readHostFile_parsesEntriesSkippingCommentsAndBlanks() throws IOException {
        Path f = tmp.resolve("hosts.txt");
        Files.writeString(f, """
                # ICANN hostnames
                icann.org

                *.icann.org   # all subdomains
                *.svc.cluster.local
                """, StandardCharsets.UTF_8);

        List<String> entries = SsrfHostBlocklist.readHostFile(f);
        assertThat(entries).containsExactly("icann.org", "*.icann.org", "*.svc.cluster.local");
        assertThat(SsrfHostBlocklist.of(entries).isBlocked("rdap.icann.org")).isTrue();
    }

    @Test
    public void readHostFile_missingFileThrowsIOException() {
        assertThatThrownBy(() -> SsrfHostBlocklist.readHostFile(tmp.resolve("missing.txt")))
                .isInstanceOf(NoSuchFileException.class);
    }

    @Test
    public void readHostFile_malformedLineReportsFileAndLineNumber() throws IOException {
        Path f = tmp.resolve("bad.txt");
        Files.writeString(f, "icann.org\n# ok\nfoo*.bar\n", StandardCharsets.UTF_8);

        assertThatThrownBy(() -> SsrfHostBlocklist.readHostFile(f))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bad.txt:3:")
                .hasMessageContaining("foo*.bar");
    }
}
