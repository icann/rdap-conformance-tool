package org.icann.rdapconformance.tool;

import static org.icann.rdapconformance.validator.CommonUtils.HTTP;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;
import static org.icann.rdapconformance.validator.CommonUtils.addErrorToResultsFile;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.SystemUtils;
import org.icann.rdapconformance.validator.ConnectionTracker;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.NetworkInfo;
import org.icann.rdapconformance.validator.ToolResult;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParser;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParserImpl;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.LocalFileSystem;
import org.icann.rdapconformance.validator.workflow.ValidatorWorkflow;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResultFile;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.icann.rdapconformance.validator.workflow.rdap.file.RDAPFileValidator;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpValidator;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "rdap-conformance-tool", versionProvider = org.icann.rdapconformance.tool.VersionProvider.class, mixinStandardHelpOptions = true)
public class RdapConformanceTool implements RDAPValidatorConfiguration, Callable<Integer> {
  // Create a logger
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RdapConformanceTool.class);

  @Parameters(paramLabel = "RDAP_URI", description = "The URI to be tested", index = "0")
  URI uri;

  private FileSystem fileSystem = new LocalFileSystem();

  @Option(names = {"-c", "--config"}, description = "Definition file", required = true)
  String configurationFile;

  @Option(names = {"--timeout"},
      description = "Timeout for connecting to the server", defaultValue = "20")
  private int timeout = 20;

  @Option(names = {"--maximum-redirects"},
      description = "Maximum number of redirects to follow", defaultValue = "3")
  private int maxRedirects = 3;

  @Option(names = {"--use-local-datasets"},
      description = "Use locally-persisted datasets", defaultValue = "false")
  private boolean useLocalDatasets = false;

  @Option(names = {"--results-file"}, description = "File to store the validation results",  hidden = true)
  private String resultsFile;

  @Option(names = {"--no-ipv4-queries"}, description = "No queries over IPv4 are to be issued")
  private boolean executeIPv4Queries = true;

  @Option(names = {"--no-ipv6-queries"}, description = "No queries over IPv6 are to be issued")
  private boolean executeIPv6Queries = true;

  @Option(names = {"--additional-conformance-queries"}, description = "Additional queries '/help' and 'not-a-domain.invalid' to be issued")
  private boolean additionalConformanceQueries = false;

  @ArgGroup(exclusive = false)
  private DependantRdapProfileGtld dependantRdapProfileGtld = new DependantRdapProfileGtld();

  @Option(names = {"--query-type"}, hidden = true)
  private RDAPQueryType queryType;

  @Option(names = {"-v", "--verbose"}, description = "display all logs")
  private boolean isVerbose = false;

  private boolean networkEnabled  = true;

  @Override
  public Integer call() throws Exception {
    if (!isVerbose) {
      Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
      root.setLevel(Level.ERROR);
    }

    ValidatorWorkflow validator;
    if (uri.getScheme() != null && uri.getScheme().toLowerCase().startsWith(HTTP)) {
      validator = new RDAPHttpValidator(this, fileSystem);
    } else {
      networkEnabled = false;
      validator = new RDAPFileValidator(this, fileSystem);
    }

    RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();

    if (networkEnabled) {
      // Initialize our DNS lookups with this.
      DNSCacheResolver.initFromUrl(uri.toString());
      DNSCacheResolver.doZeroIPAddressesValidation(uri.toString(), executeIPv6Queries, executeIPv4Queries);

      // do v6
      if(executeIPv6Queries && DNSCacheResolver.hasV6Addresses(uri.toString())) {
        NetworkInfo.setStackToV6();
        NetworkInfo.setAcceptHeaderToApplicationJson();
        int v6ret = validator.validate();

        // set the header to RDAP+JSON and redo the validations
        NetworkInfo.setAcceptHeaderToApplicationRdapJson();
        int v6ret2 = validator.validate();
      }

      // do v4
      if(executeIPv4Queries && DNSCacheResolver.hasV4Addresses(uri.toString())) {
        NetworkInfo.setStackToV4();
        NetworkInfo.setAcceptHeaderToApplicationJson();
        int v4ret = validator.validate();

        // set the header to RDAP+JSON and redo the validations
        NetworkInfo.setAcceptHeaderToApplicationRdapJson();
        int v4ret2 = validator.validate();
      }

      // TODO: refactor this out into common utils code
      if(DNSCacheResolver.hasNoAddresses(DNSCacheResolver.getHostnameFromUrl(uri.toString()))) {
        ConfigurationFile configurationFileObj;
        try (InputStream is = fileSystem.uriToStream(this.getConfigurationFile())) {
          ConfigurationFileParser configParser = new ConfigurationFileParserImpl();
          configurationFileObj = configParser.parse(is);
        } catch (Exception e) {
          logger.error("Configuration is invalid", e);
          return ToolResult.CONFIG_INVALID.getCode();
        }

        resultFile.initialize(RDAPValidatorResultsImpl.getInstance(), this, configurationFileObj, fileSystem);
        logger.info("Unable to resolve an IP address endpoint using DNS for uri:  "  + DNSCacheResolver.getHostnameFromUrl(uri.toString()));
      }

      logger.info("ConnectionTracking: " + ConnectionTracker.getInstance().toString());
      String rez = RDAPValidatorResultsImpl.getInstance().analyzeResultsWithStatusCheck();
      System.out.println(rez);

      System.out.println("RDAP Validation Results Filtered: " + RDAPValidatorResultsImpl.getInstance().analyzeResults());
      System.out.println("RDAP Validation Results Unfiltered: " + RDAPValidatorResultsImpl.getInstance().prettyPrintCodesAndStatus());

      // Build the result file with a legacy zero exit code
      resultFile.build(ZERO);
      // now the results file is set, print the path
      logger.info("Results file: {}",  validator.getResultsPath());

      int exitCode = 0;

      // Return the exit code
      return exitCode;
    }

    return validateWithoutNetwork(resultFile, validator);

  }


  private int validateWithoutNetwork(RDAPValidationResultFile resultFile, ValidatorWorkflow validator) {
    // If network is not enabled or ipv4/ipv6 flags are off, validate and return
    int file_exit_code =  validator.validate();
    resultFile.build(ZERO);
    logger.info("Results file: {}",  validator.getResultsPath());
    return file_exit_code;
  }

  @Override
  public URI getConfigurationFile() {
    try {
      return URI.create(this.configurationFile);
    } catch (IllegalArgumentException ex) {
      // handle Windows uri without compromising remote file:
      if (SystemUtils.IS_OS_WINDOWS) {
        return new File(this.configurationFile).toURI();
      }

      throw ex;
    }
  }

  @Override
  public String getResultsFile() {
    return this.resultsFile;
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
  public boolean useRdapProfileFeb2019() {
    return this.dependantRdapProfileGtld.exclusiveRdapProfile.dependantRdapProfile.useRdapProfileFeb2019;}

  @Override
  public boolean useRdapProfileFeb2024() { return this.dependantRdapProfileGtld.exclusiveRdapProfile.dependantRdapProfile.useRdapProfileFeb2024; }


  @Override
  public boolean isGtldRegistrar() {
    return this.dependantRdapProfileGtld.exclusiveRdapProfile.exclusiveGtldType.gtldRegistrar;
  }

  @Override
  public boolean isGtldRegistry() {
    return this.dependantRdapProfileGtld.exclusiveRdapProfile.exclusiveGtldType.dependantRegistryThin.gtldRegistry;
  }

  @Override
  public boolean isThin() {
    return this.dependantRdapProfileGtld.exclusiveRdapProfile.exclusiveGtldType.dependantRegistryThin.thin;
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

  @Override
  public boolean isNetworkEnabled() {
    return networkEnabled;
  }

  @Override
  public boolean isNoIpv4Queries() {
    return !executeIPv4Queries;
  }

  @Override
  public boolean isNoIpv6Queries() {
    return !executeIPv6Queries;
  }
  @Override
  public boolean isAdditionalConformanceQueries() {
    return additionalConformanceQueries;
  }

  private static class DependantRdapProfileGtld {
    @ArgGroup(multiplicity = "1", exclusive = false)
    ExclusiveRdapProfile exclusiveRdapProfile = new ExclusiveRdapProfile();
  }

  private static class ExclusiveRdapProfile {

    @ArgGroup()
    private DependantRdapProfile dependantRdapProfile = new DependantRdapProfile();

    @ArgGroup(multiplicity = "1")
    ExclusiveGtldType exclusiveGtldType = new ExclusiveGtldType();

  }

  private static class DependantRdapProfile {
    @Option(names = {"--use-rdap-profile-february-2019"},
            description = "Use RDAP Profile February 2019", defaultValue = "false")
    boolean useRdapProfileFeb2019 = false;
    @Option(names = {"--use-rdap-profile-february-2024"},
            description = "Use RDAP Profile February 2024", defaultValue = "false", required = true)
    private boolean useRdapProfileFeb2024 = false;
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

