package org.icann.rdapconformance.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import picocli.CommandLine;
import picocli.CommandLine.MutuallyExclusiveArgsException;
import org.testng.annotations.Test;

public class RdapConformanceToolGtldProfile3Test {

    private RdapConformanceTool parse(String... args) {
        RdapConformanceTool tool = new RdapConformanceTool();
        new CommandLine(tool).parseArgs(args);
        return tool;
    }

    @Test
    public void gtldProfile3Alone_enablesV3AndNot2024() {
        RdapConformanceTool tool = parse(
                "--gtldProfile3",
                "--gtld-registry",
                "-c", "config.json",
                "https://example.com/rdap/domain/example.com");

        assertThat(tool.useGtldProfileV3()).isTrue();
        assertThat(tool.useRdapProfileFeb2024()).isFalse();
    }

    @Test
    public void profile2024Alone_enables2024AndNotV3() {
        RdapConformanceTool tool = parse(
                "--use-rdap-profile-february-2024",
                "--gtld-registry",
                "-c", "config.json",
                "https://example.com/rdap/domain/example.com");

        assertThat(tool.useRdapProfileFeb2024()).isTrue();
        assertThat(tool.useGtldProfileV3()).isFalse();
    }

    @Test
    public void bothFlags_isCliError() {
        assertThatThrownBy(() -> parse(
                "--use-rdap-profile-february-2024",
                "--gtldProfile3",
                "--gtld-registry",
                "-c", "config.json",
                "https://example.com/rdap/domain/example.com"))
                .isInstanceOf(MutuallyExclusiveArgsException.class);
    }
}