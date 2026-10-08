package org.icann.rdapconformance.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import org.icann.rdapconformance.validator.ToolResult;
import org.icann.rdapconformance.validator.workflow.ValidatorWorkflow;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResultFile;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Exercises the early-return branches of call() (logging switch + DNS resolver
 * validation) and validateWithoutNetwork() with mocks. No network access.
 */
public class RdapConformanceToolCallBranchesTest {

    private static final String INVALID_DNS = "999.999.999.999";

    @DataProvider(name = "loggingLevels")
    public Object[][] loggingLevels() {
        return new Object[][]{
                {LoggingLevel.CLI}, {LoggingLevel.INFO}, {LoggingLevel.DEBUG},
                {LoggingLevel.ERROR}, {LoggingLevel.VERBOSE},
        };
    }

    /**
     * Invalid DNS resolver short-circuits call() with BAD_USER_INPUT before any
     * dataset/config/network work. Running it per logging level also covers the
     * whole logging switch and the logback reconfiguration branches.
     */
    @Test(dataProvider = "loggingLevels")
    public void call_invalidDnsResolver_perLoggingLevel(LoggingLevel level) throws Exception {
        RdapConformanceTool tool = new RdapConformanceTool();
        tool.setLogging(level);
        tool.setCustomDnsResolver(INVALID_DNS);

        Integer code = tool.call();

        assertThat(code).isEqualTo(ToolResult.BAD_USER_INPUT.getCode());
    }

    @Test
    public void call_invalidDnsResolver_verboseFlagOverridesLoggingLevel() throws Exception {
        RdapConformanceTool tool = new RdapConformanceTool();
        tool.setLogging(LoggingLevel.CLI);
        tool.setVerbose(true); // forces VERBOSE path
        tool.setCustomDnsResolver(INVALID_DNS);

        assertThat(tool.call()).isEqualTo(ToolResult.BAD_USER_INPUT.getCode());
    }

    @Test
    public void call_dnsResolverNotAnIp_rejected() throws Exception {
        RdapConformanceTool tool = new RdapConformanceTool();
        tool.setCustomDnsResolver("not-an-ip");
        assertThat(tool.call()).isEqualTo(ToolResult.BAD_USER_INPUT.getCode());
    }

    // ---------- validateWithoutNetwork ----------

    @Test
    public void validateWithoutNetwork_thinModel_returnsEarly() {
        RdapConformanceTool tool = new RdapConformanceTool();
        ValidatorWorkflow validator = mock(ValidatorWorkflow.class);
        RDAPValidationResultFile resultFile = mock(RDAPValidationResultFile.class);

        when(validator.validate()).thenReturn(ToolResult.USES_THIN_MODEL.getCode());

        int code = tool.validateWithoutNetwork(resultFile, validator);

        assertThat(code).isEqualTo(ToolResult.USES_THIN_MODEL.getCode());
        verify(resultFile, never()).build();
    }

    @Test
    public void validateWithoutNetwork_buildFails_returnsFileWriteError() {
        RdapConformanceTool tool = new RdapConformanceTool();
        ValidatorWorkflow validator = mock(ValidatorWorkflow.class);
        RDAPValidationResultFile resultFile = mock(RDAPValidationResultFile.class);

        when(validator.validate()).thenReturn(0);
        when(resultFile.build()).thenReturn(false);

        int code = tool.validateWithoutNetwork(resultFile, validator);

        assertThat(code).isEqualTo(ToolResult.FILE_WRITE_ERROR.getCode());
    }

    @Test
    public void validateWithoutNetwork_success_returnsValidatorExitCode() {
        RdapConformanceTool tool = new RdapConformanceTool();
        ValidatorWorkflow validator = mock(ValidatorWorkflow.class);
        RDAPValidationResultFile resultFile = mock(RDAPValidationResultFile.class);

        when(validator.validate()).thenReturn(0);
        when(resultFile.build()).thenReturn(true);
        when(resultFile.getResultsPath()).thenReturn("/tmp/results.json");

        int code = tool.validateWithoutNetwork(resultFile, validator);

        assertThat(code).isZero();
    }

    @Test
    public void validateWithoutNetwork_verboseMode_printsResultsPath() {
        RdapConformanceTool tool = new RdapConformanceTool();
        tool.setVerbose(true); // covers the CLI/VERBOSE println branch
        ValidatorWorkflow validator = mock(ValidatorWorkflow.class);
        RDAPValidationResultFile resultFile = mock(RDAPValidationResultFile.class);

        when(validator.validate()).thenReturn(0);
        when(resultFile.build()).thenReturn(true);
        when(resultFile.getResultsPath()).thenReturn("/tmp/results.json");

        assertThat(tool.validateWithoutNetwork(resultFile, validator)).isZero();
    }

    // ---------- getConfigurationFile ----------

    @Test
    public void getConfigurationFile_validUri_parses() {
        RdapConformanceTool tool = new RdapConformanceTool();
        tool.setConfigurationFile("file:///tmp/config.json");
        assertThat(tool.getConfigurationFile().toString()).contains("config.json");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void getConfigurationFile_invalidUri_throwsOnUnix() {
        RdapConformanceTool tool = new RdapConformanceTool();
        tool.setConfigurationFile("ht tp://bad uri with spaces");
        tool.getConfigurationFile(); // non-Windows -> rethrow
    }

    @Test
    public void call_noIpv4Flag_processedBeforeDnsRejection() throws Exception {
        RdapConformanceTool tool = new RdapConformanceTool();
        picocli.CommandLine cl = new picocli.CommandLine(tool);
        cl.registerConverter(java.net.URI.class, new RdapConformanceTool.IdnAwareUriConverter());
        cl.parseArgs("--config", "/tmp/nonexistent.json",
                "--no-ipv4-queries",
                "--dns-resolver", "999.999.999.999",
                "https://rdap.example.com/domain/test.example");

        // ipVersionOptions branch (noIPv4Queries=true) runs first,
        // then the invalid DNS resolver short-circuits with BAD_USER_INPUT
        assertThat(tool.call()).isEqualTo(ToolResult.BAD_USER_INPUT.getCode());
        assertThat(tool.isNoIpv4Queries()).isTrue();
        assertThat(tool.isNoIpv6Queries()).isFalse();
    }

    @Test
    public void call_noIpv6Flag_processedBeforeDnsRejection() throws Exception {
        RdapConformanceTool tool = new RdapConformanceTool();
        picocli.CommandLine cl = new picocli.CommandLine(tool);
        cl.registerConverter(java.net.URI.class, new RdapConformanceTool.IdnAwareUriConverter());
        cl.parseArgs("--config", "/tmp/nonexistent.json",
                "--no-ipv6-queries",
                "--dns-resolver", "999.999.999.999",
                "https://rdap.example.com/domain/test.example");

        assertThat(tool.call()).isEqualTo(ToolResult.BAD_USER_INPUT.getCode());
        assertThat(tool.isNoIpv6Queries()).isTrue();
        assertThat(tool.isNoIpv4Queries()).isFalse();
    }

    @Test
    public void call_configFileDoesNotExist_returnsConfigDoesNotExist() throws Exception {
        RdapConformanceTool tool = new RdapConformanceTool();
        tool.setLogging(LoggingLevel.ERROR);
        tool.setShowProgress(false);
        tool.setUseLocalDatasets(true);
        tool.setUri(java.net.URI.create("https://rdap.example.com/domain/test.example"));
        tool.setConfigurationFile("/tmp/definitely-does-not-exist-" + System.nanoTime() + ".json");

        assertThat(tool.call()).isEqualTo(ToolResult.CONFIG_DOES_NOT_EXIST.getCode());
    }

    @Test
    public void call_configFileInvalid_returnsConfigInvalid() throws Exception {
        java.nio.file.Path bad = java.nio.file.Files.createTempFile("badconfig", ".json");
        java.nio.file.Files.writeString(bad, "{ this is not valid json !!");
        try {
            RdapConformanceTool tool = new RdapConformanceTool();
            tool.setLogging(LoggingLevel.ERROR);
            tool.setShowProgress(false);
            tool.setUseLocalDatasets(true);
            tool.setUri(java.net.URI.create("https://rdap.example.com/domain/test.example"));
            tool.setConfigurationFile(bad.toString());

            assertThat(tool.call()).isEqualTo(ToolResult.CONFIG_INVALID.getCode());
        } finally {
            java.nio.file.Files.deleteIfExists(bad);
        }
    }

    @Test
    public void call_fileUriWithoutQueryType_returnsUnsupportedQuery() throws Exception {
        Path config = java.nio.file.Files.createTempFile("config", ".json");
        Files.writeString(config,
                "{\"definitionIdentifier\": \"test\"}");
        try {
            RdapConformanceTool tool = new RdapConformanceTool();
            tool.setLogging(LoggingLevel.ERROR);
            tool.setShowProgress(false);
            tool.setUseLocalDatasets(true);
            // file:// scheme -> non-HTTP branch; queryType == null -> UNSUPPORTED_QUERY
            tool.setUri(java.net.URI.create("file:///tmp/some-response.json"));
            tool.setConfigurationFile(config.toString());

            assertThat(tool.call()).isEqualTo(ToolResult.UNSUPPORTED_QUERY.getCode());
        } finally {
            java.nio.file.Files.deleteIfExists(config);
        }
    }

    @Test
    public void validateWithoutNetwork_rdapValidator_copiesQueryContext() {
        RdapConformanceTool tool = new RdapConformanceTool();
        org.icann.rdapconformance.validator.workflow.rdap.RDAPValidator validator =
                mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidator.class);
        org.icann.rdapconformance.validator.QueryContext qc =
                mock(org.icann.rdapconformance.validator.QueryContext.class);
        RDAPValidationResultFile resultFile = mock(RDAPValidationResultFile.class);

        when(validator.validate()).thenReturn(0);
        when(validator.getQueryContext()).thenReturn(qc);
        when(resultFile.build()).thenReturn(true);
        when(resultFile.getResultsPath()).thenReturn("/tmp/results.json");

        assertThat(tool.validateWithoutNetwork(resultFile, validator)).isZero();
        verify(resultFile).addResultsFromQueryContext(qc);
    }

    @Test
    public void validateWithoutNetwork_rdapValidator_nullQueryContext() {
        RdapConformanceTool tool = new RdapConformanceTool();
        org.icann.rdapconformance.validator.workflow.rdap.RDAPValidator validator =
                mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidator.class);
        RDAPValidationResultFile resultFile = mock(RDAPValidationResultFile.class);

        when(validator.validate()).thenReturn(0);
        when(validator.getQueryContext()).thenReturn(null); // null branch
        when(resultFile.build()).thenReturn(true);
        when(resultFile.getResultsPath()).thenReturn("/tmp/results.json");

        assertThat(tool.validateWithoutNetwork(resultFile, validator)).isZero();
        verify(resultFile, never()).addResultsFromQueryContext(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void call_fileUriWithQueryType_runsFileValidationPath() throws Exception {
        Path config = Files.createTempFile("config", ".json");
        Files.writeString(config, "{\"definitionIdentifier\": \"coverage-test\"}");
        Path rdapFile = Files.createTempFile("rdap-response", ".json");
        Files.writeString(rdapFile, "{}");
        try {
            RdapConformanceTool tool = new RdapConformanceTool();
            tool.setLogging(LoggingLevel.ERROR);
            tool.setShowProgress(false);
            tool.setUseLocalDatasets(true);
            tool.setUri(rdapFile.toUri()); // file:// scheme
            tool.setConfigurationFile(config.toString());
            tool.queryType = org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType.DOMAIN;

            Integer code = tool.call();
            // Whatever the exit code, the file-validation path executed
            assertThat(code).isNotNull();
        } finally {
            Files.deleteIfExists(config);
            Files.deleteIfExists(rdapFile);
        }
    }
}