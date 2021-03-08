package org.icann.rdap.conformance.tool;

import java.io.File;
import java.net.URI;
import java.util.concurrent.Callable;
import org.icann.rdap.conformance.validator.RDAPValidator;
import org.icann.rdap.conformance.validator.configuration.RDAPValidatorConfiguration;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "rdap-conformance-tool", version = "0.1-alpha", mixinStandardHelpOptions = true)
public class RdapConformanceTool implements RDAPValidatorConfiguration, Callable<Integer> {

  @Option(names = {"-c", "--config"}, description = "Definition file", required = true)
  private File configurationFile;
  @Option(names = {"--timeout"},
      description = "Timeout for connecting to the server", defaultValue = "20")
  private int timeout = 20;
  @Option(names = {"--maximum-redirects"},
      description = "Maximum number of redirects to follow", defaultValue = "3")
  private int maxRedirects = 3;
  @Option(names = {"--use-local-datasets"},
      description = "Use locally-persisted datasets", defaultValue = "false")
  private boolean useLocalDatasets = false;
  @ArgGroup(exclusive = false)
  private DependantRdapProfileGtld dependantRdapProfileGtld;
  @Parameters(paramLabel = "RDAP_URI", description = "The URI to be tested", index = "0")
  URI uri;

  @Override
  public Integer call() throws Exception {
    RDAPValidator validator = new RDAPValidator(this);
    return validator.validate();
  }

  @Override
  public File getConfigurationFile() {
    return this.configurationFile;
  }

  @Override
  public int getTimeout() {
    return this.timeout;
  }

  @Override
  public int getMaxRedirects() {
    return this.maxRedirects;
  }

  @Override
  public boolean useLocalDatasets() {
    return this.useLocalDatasets;
  }

  @Override
  public boolean userRdapProfileFeb2019() {
    return this.dependantRdapProfileGtld.useRdapProfileFeb2019;
  }

  @Override
  public boolean isGltdRegistrar() {
    return this.dependantRdapProfileGtld.exclusiveGtldType.gtldRegistrar;
  }

  @Override
  public boolean isGtldRegistry() {
    return this.dependantRdapProfileGtld.exclusiveGtldType.dependantRegistryThin.gtldRegistry;
  }

  @Override
  public boolean isThin() {
    return this.dependantRdapProfileGtld.exclusiveGtldType.dependantRegistryThin.thin;
  }

  @Override
  public URI getUri() {
    return this.uri;
  }

  private static class DependantRdapProfileGtld {

    @Option(names = {"--use-rdap-profile-february-2019"},
        description = "Use RDAP Profile February 2019", defaultValue = "false")
    boolean useRdapProfileFeb2019 = false;
    @ArgGroup(multiplicity = "1")
    ExclusiveGtldType exclusiveGtldType;
  }

  private static class ExclusiveGtldType {

    @Option(names = {"--gtld-registrar"},
        description = "Validate the response as coming from a gTLD registrar",
        defaultValue = "false")
    boolean gtldRegistrar = false;
    @ArgGroup(exclusive = false)
    DependantRegistryThin dependantRegistryThin;
  }

  private static class DependantRegistryThin {

    @Option(names = {"--gtld-registry"},
        description = "Validate the response as coming from a gTLD registry",
        required = true)
    boolean gtldRegistry;
    @Option(names = {"--thin"},
        description = "The TLD uses the thin model", defaultValue = "false")
    boolean thin = false;
  }
}
