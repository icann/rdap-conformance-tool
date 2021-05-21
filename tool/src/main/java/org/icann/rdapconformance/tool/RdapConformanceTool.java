package org.icann.rdapconformance.tool;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.net.URI;
import java.util.concurrent.Callable;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.LocalFileSystem;
import org.icann.rdapconformance.validator.workflow.ValidatorWorkflow;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.file.RDAPFileValidator;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpValidator;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "rdap-conformance-tool", version = "0.1-alpha", mixinStandardHelpOptions = true)
public class RdapConformanceTool implements RDAPValidatorConfiguration, Callable<Integer> {

  @Parameters(paramLabel = "RDAP_URI", description = "The URI to be tested", index = "0")
  URI uri;
  private FileSystem fileSystem = new LocalFileSystem();
  @Option(names = {"-c", "--config"}, description = "Definition file", required = true)
  private URI configurationFile;
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
  private DependantRdapProfileGtld dependantRdapProfileGtld = new DependantRdapProfileGtld();
  @Option(names = {"--query-type"}, hidden = true)
  private RDAPQueryType queryType;
  @Option(names = {"-v", "--verbose"}, description = "display all logs")
  private boolean isVerbose = false;

  @Override
  public Integer call() throws Exception {
    if (!isVerbose) {
      Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
      root.setLevel(Level.INFO);
    }
    ValidatorWorkflow validator;
    if (uri.getScheme() != null && uri.getScheme().startsWith("http")) {
      validator = new RDAPHttpValidator(this, fileSystem);
    } else {
      validator = new RDAPFileValidator(this, fileSystem);
    }
    return validator.validate();
  }

  @Override
  public URI getConfigurationFile() {
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
  public boolean isGtldRegistrar() {
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
  public RDAPQueryType getQueryType() {
    return queryType;
  }

  @Override
  public URI getUri() {
    return this.uri;
  }

  @Override
  public void setUri(URI uri) {
    this.uri = uri;
  }

  private static class DependantRdapProfileGtld {

    @Option(names = {"--use-rdap-profile-february-2019"},
        description = "Use RDAP Profile February 2019", defaultValue = "false")
    boolean useRdapProfileFeb2019 = false;
    @ArgGroup(multiplicity = "1")
    ExclusiveGtldType exclusiveGtldType = new ExclusiveGtldType();
  }

  private static class ExclusiveGtldType {

    @Option(names = {"--gtld-registrar"},
        description = "Validate the response as coming from a gTLD registrar",
        defaultValue = "false")
    private boolean gtldRegistrar = false;
    @ArgGroup(exclusive = false)
    private DependantRegistryThin dependantRegistryThin = new DependantRegistryThin();
  }

  private static class DependantRegistryThin {

    @Option(names = {"--gtld-registry"},
        description = "Validate the response as coming from a gTLD registry",
        required = true)
    private boolean gtldRegistry;
    @Option(names = {"--thin"},
        description = "The TLD uses the thin model", defaultValue = "false")
    private boolean thin = false;
  }
}
