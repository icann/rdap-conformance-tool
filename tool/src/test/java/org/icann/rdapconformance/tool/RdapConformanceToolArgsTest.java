package org.icann.rdapconformance.tool;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.testng.annotations.Test;
import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.MutuallyExclusiveArgsException;

public class RdapConformanceToolArgsTest {


  @Test
  public void testNoConfigArg_IsRequired() {
    String[] args = "http://example.org".split(" ");

    assertThatExceptionOfType(MissingParameterException.class).isThrownBy(
        () -> new CommandLine(new RdapConformanceTool()).parseArgs(args))
        .withMessage("Missing required option: '--config=<configurationFile>'");
  }

  @Test
  public void testNoURL_IsRequired() {
    String[] args = "--config=/tmp/test".split(" ");

    assertThatExceptionOfType(MissingParameterException.class).isThrownBy(
        () -> new CommandLine(new RdapConformanceTool()).parseArgs(args))
        .withMessage("Missing required parameter: 'RDAP_URI'");
  }

  @Test
  public void testUseRdapProfileArg_RequiresGtldRegistryOrGtldRegistrar() {
    String[] args = "--config=/tmp/test --use-rdap-profile-february-2019 http://example.org"
        .split(" ");

    assertThatExceptionOfType(MissingParameterException.class).isThrownBy(
        () -> new CommandLine(new RdapConformanceTool()).parseArgs(args))
        .withMessageStartingWith("Error: Missing required argument(s): ");
  }

  @Test
  public void testUseRdapProfileArg_WithGtldRegistry_isOK() {
    String[] args = "--config=/tmp/test --use-rdap-profile-february-2019 --gtld-registry http://example.org"
        .split(" ");

    assertThatCode(() -> new CommandLine(new RdapConformanceTool()).parseArgs(args))
        .doesNotThrowAnyException();

  }

  @Test
  public void testUseRdapProfileArg_WithGtldRegistrar_isOK() {
    String[] args = "--config=/tmp/test --use-rdap-profile-february-2019 --gtld-registrar http://example.org"
        .split(" ");

    assertThatCode(() -> new CommandLine(new RdapConformanceTool()).parseArgs(args))
        .doesNotThrowAnyException();
  }


  @Test
  public void testGtldRegistryAndGtldRegistrarArgs_AreExclusive() {
    String[] args = "--config=/tmp/test --gtld-registry --gtld-registrar http://example.org"
        .split(" ");

    assertThatExceptionOfType(MutuallyExclusiveArgsException.class).isThrownBy(
        () -> new CommandLine(new RdapConformanceTool()).parseArgs(args))
        .withMessageStartingWith("Error: ")
        .withMessageEndingWith("are mutually exclusive (specify only one)");
  }

  @Test
  public void testThinArg_RequiresGtldRegistry() {
    String[] args = "--config=/tmp/test --thin http://example.org".split(" ");

    assertThatExceptionOfType(MissingParameterException.class).isThrownBy(
        () -> new CommandLine(new RdapConformanceTool()).parseArgs(args))
        .withMessage("Error: Missing required argument(s): --gtld-registry");
  }

  @Test
  public void testThinArg_WithGtldRegistry_IsOk() {
    String[] args = "--config=/tmp/test --thin --gtld-registry http://example.org".split(" ");

    assertThatCode(() -> new CommandLine(new RdapConformanceTool()).parseArgs(args))
        .doesNotThrowAnyException();
  }

  @Test
  public void testGtldRegistryArg_IsOk() {
    String[] args = "--config=/tmp/test --gtld-registry http://example.org".split(" ");

    assertThatCode(() -> new CommandLine(new RdapConformanceTool()).parseArgs(args))
        .doesNotThrowAnyException();
  }

  @Test
  public void testGtldRegistrarArg_IsOk() {
    String[] args = "--config=/tmp/test --gtld-registrar http://example.org".split(" ");

    assertThatCode(() -> new CommandLine(new RdapConformanceTool()).parseArgs(args))
        .doesNotThrowAnyException();
  }
}