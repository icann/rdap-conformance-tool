package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Loading of the hostname blocklist from configuration + {@code RDAPCT_SSRF_BLOCKED_HOSTS_FILE}.
 * The env var value is injected directly (the JVM cannot set real env vars in-process).
 */
public class QueryContextSsrfHostBlocklistLoadTest {

    @TempDir
    Path tmp;

    private RDAPValidatorConfiguration config;

    @BeforeEach
    public void setUp() {
        config = mock(RDAPValidatorConfiguration.class);
        doReturn(List.of()).when(config).getSsrfBlockedHosts();
    }

    @Test
    public void envVarUnset_configEmpty_givesEmptyBlocklist() {
        assertThat(QueryContext.loadSsrfHostBlocklist(config, null).size()).isZero();
        assertThat(QueryContext.loadSsrfHostBlocklist(config, "   ").size()).isZero();
    }

    @Test
    public void envVarUnset_configEntriesAreUsed() {
        doReturn(List.of("*.icann.org")).when(config).getSsrfBlockedHosts();
        SsrfHostBlocklist list = QueryContext.loadSsrfHostBlocklist(config, null);
        assertThat(list.isBlocked("rdap.icann.org")).isTrue();
    }

    @Test
    public void envVarSet_fileEntriesAreMergedWithConfig() throws IOException {
        Path f = tmp.resolve("hosts.txt");
        Files.writeString(f, "# from deployment\n*.svc.cluster.local\n");
        doReturn(List.of("*.icann.org")).when(config).getSsrfBlockedHosts();

        SsrfHostBlocklist list = QueryContext.loadSsrfHostBlocklist(config, f.toString());

        assertThat(list.size()).isEqualTo(2);
        assertThat(list.isBlocked("rdap.icann.org")).isTrue();
        assertThat(list.isBlocked("api.default.svc.cluster.local")).isTrue();
    }

    @Test
    public void envVarSet_missingFile_failsClosed() {
        assertThatThrownBy(() -> QueryContext.loadSsrfHostBlocklist(config, tmp.resolve("missing.txt").toString()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(SsrfHostBlocklist.HOSTS_FILE_ENV)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    public void envVarSet_malformedFile_failsClosed() throws IOException {
        Path f = tmp.resolve("bad.txt");
        Files.writeString(f, "icann.org\n10.0.0.1\n");

        assertThatThrownBy(() -> QueryContext.loadSsrfHostBlocklist(config, f.toString()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(SsrfHostBlocklist.HOSTS_FILE_ENV)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasStackTraceContaining("bad.txt:2:");
    }
}
