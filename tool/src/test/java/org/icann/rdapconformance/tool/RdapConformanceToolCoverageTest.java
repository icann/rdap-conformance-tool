package org.icann.rdapconformance.tool;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.testng.annotations.Test;

/**
 * Coverage-focused tests for the plain getters/setters and the
 * queryContext==null fallback branches of {@link RdapConformanceTool}.
 * None of these touch the network or datasets.
 */
public class RdapConformanceToolCoverageTest {

    private RdapConformanceTool newTool() {
        return new RdapConformanceTool();
    }

    @Test
    public void scalarSettersAndGettersRoundTrip() {
        RdapConformanceTool tool = newTool();

        tool.setTimeout(42);
        assertThat(tool.getTimeout()).isEqualTo(42);

        tool.setMaxRedirects(7);
        assertThat(tool.getMaxRedirects()).isEqualTo(7);

        tool.setResultsFile("/tmp/results.json");
        assertThat(tool.getResultsFile()).isEqualTo("/tmp/results.json");

        tool.setUseLocalDatasets(true);
        assertThat(tool.useLocalDatasets()).isTrue();

        URI uri = URI.create("https://rdap.example.com/domain/test.example");
        tool.setUri(uri);
        assertThat(tool.getUri()).isEqualTo(uri);
    }

    @Test
    public void profileFlagsRoundTrip() {
        RdapConformanceTool tool = newTool();

        tool.setUseRdapProfileFeb2024(true);
        assertThat(tool.useRdapProfileFeb2024()).isTrue();

        tool.setGtldRegistry(true);
        assertThat(tool.isGtldRegistry()).isTrue();

        tool.setGtldRegistrar(true);
        assertThat(tool.isGtldRegistrar()).isTrue();

        tool.setThin(true);
        assertThat(tool.isThin()).isTrue();

        tool.setAdditionalConformanceQueries(true);
        assertThat(tool.isAdditionalConformanceQueries()).isTrue();
    }

    @Test
    public void disablingIpv4_autoEnablesIpv6() {
        RdapConformanceTool tool = newTool();
        tool.setExecuteIPv6Queries(false);
        tool.setExecuteIPv4Queries(false); // both would be off -> IPv6 re-enabled
        assertThat(tool.isNoIpv4Queries()).isTrue();
        assertThat(tool.isNoIpv6Queries()).isFalse();
    }

    @Test
    public void disablingIpv6_autoEnablesIpv4() {
        RdapConformanceTool tool = newTool();
        tool.setExecuteIPv4Queries(false);
        tool.setExecuteIPv6Queries(false); // both would be off -> IPv4 re-enabled
        assertThat(tool.isNoIpv6Queries()).isTrue();
        assertThat(tool.isNoIpv4Queries()).isFalse();
    }

    @Test
    public void bothProtocolsEnabledByDefault() {
        RdapConformanceTool tool = newTool();
        tool.setExecuteIPv4Queries(true);
        tool.setExecuteIPv6Queries(true);
        assertThat(tool.isNoIpv4Queries()).isFalse();
        assertThat(tool.isNoIpv6Queries()).isFalse();
    }

    @Test
    public void verboseAndLoggingSettersDoNotThrow() {
        RdapConformanceTool tool = newTool();
        tool.setVerbose(true);
        tool.setVerbose(false);
        tool.setLogging(LoggingLevel.DEBUG);
        tool.setShowProgress(false);
    }

    @Test
    public void ssrfAndDnsResolverAccessors() {
        RdapConformanceTool tool = newTool();
        tool.setCustomDnsResolver("8.8.8.8");
        assertThat(tool.getCustomDnsResolver()).isEqualTo("8.8.8.8");
        assertThat(tool.getSsrfAllowedHosts()).isNotNull();
    }

    @Test
    public void resultAccessors_returnEmpty_whenNoValidationRun() {
        RdapConformanceTool tool = newTool();
        // queryContext == null path -> safe empty returns
        assertThat(tool.getErrors()).isEmpty();
        assertThat(tool.getAllResults()).isEmpty();
        assertThat(tool.getErrorCount()).isZero();
    }

    @Test
    public void jsonAccessors_returnFallback_whenNoValidationRun() {
        RdapConformanceTool tool = newTool();

        // getErrorsAsJson / getWarningsAsJson -> empty array "[]"
        assertThat(tool.getErrorsAsJson()).isEqualTo("[]");
        assertThat(tool.getWarningsAsJson()).isEqualTo("[]");

        // getAllResultsAsJson -> fallback object with the 4 keys
        String all = tool.getAllResultsAsJson();
        assertThat(all).contains("error").contains("warning")
                .contains("ignore").contains("notes");
    }

    @Test
    public void clean_isSafe_beforeQueryContextCreated() {
        RdapConformanceTool tool = newTool();
        tool.clean(); // queryContext == null branch
    }

    @Test
    public void queryTypeAndNetworkDefaults() {
        RdapConformanceTool tool = newTool();
        // just exercise the getters; values may be null/default
        tool.getQueryType();
        tool.isNetworkEnabled();
    }
}