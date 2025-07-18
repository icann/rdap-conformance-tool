package org.icann.rdapconformance.tool;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import java.io.File;
import java.net.URI;
import java.security.Security;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.SystemUtils;
import org.icann.rdapconformance.validator.workflow.rdap.*;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.ConnectionTracker;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.NetworkInfo;
import org.icann.rdapconformance.validator.ToolResult;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.LocalFileSystem;
import org.icann.rdapconformance.validator.workflow.ValidatorWorkflow;
import org.icann.rdapconformance.validator.workflow.rdap.file.RDAPFileValidator;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQueryTypeProcessor;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpValidator;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.icann.rdapconformance.validator.CommonUtils.*;


@Command(name = "rdap-conformance-tool", versionProvider = org.icann.rdapconformance.tool.VersionProvider.class, mixinStandardHelpOptions = true)
public class RdapConformanceTool implements RDAPValidatorConfiguration, Callable<Integer> {

  // Create a logger
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RdapConformanceTool.class);
  public static final int PRETTY_PRINT_INDENT = 2;

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

  // IP version queries group
  @ArgGroup(exclusive = true)
  private IpVersionQueriesOptions ipVersionOptions;

  // Default values when neither option is specified
  private boolean executeIPv4Queries = true;
  private boolean executeIPv6Queries = true;

  @Option(names = {"--additional-conformance-queries"}, description = "Additional queries '/help' and 'not-a-domain.invalid' to be issued")
  private boolean additionalConformanceQueries = false;

  @ArgGroup(exclusive = false)
  private DependantRdapProfileGtld dependantRdapProfileGtld = new DependantRdapProfileGtld();

  @Option(names = {"--query-type"}, hidden = true)
  RDAPQueryType queryType;

  @Option(names = {"-v", "--verbose"}, description = "display all logs")
  private boolean isVerbose = false;

  private boolean networkEnabled  = true;

  // IP version query options as mutually exclusive group
  private static class IpVersionQueriesOptions {
    @Option(names = {"--no-ipv4-queries"}, description = "No queries over IPv4 are to be issued")
    private boolean noIPv4Queries;

    @Option(names = {"--no-ipv6-queries"}, description = "No queries over IPv6 are to be issued")
    private boolean noIPv6Queries;
  }

public void setConfigurationFile(String configurationFile) {
    this.configurationFile = configurationFile;
}

public void setTimeout(int timeout) {
    this.timeout = timeout;
}

public void setMaxRedirects(int maxRedirects) {
    this.maxRedirects = maxRedirects;
}

public void setUseLocalDatasets(boolean useLocalDatasets) {
    this.useLocalDatasets = useLocalDatasets;
}

public void setResultsFile(String resultsFile) {
    this.resultsFile = resultsFile;
}

public void setExecuteIPv4Queries(boolean executeIPv4Queries) {
    this.executeIPv4Queries = executeIPv4Queries;
    // Ensure at least one protocol is enabled - if disabling IPv4, enable IPv6
    if (!executeIPv4Queries && !this.executeIPv6Queries) {
        this.executeIPv6Queries = true;
    }
}

public void setExecuteIPv6Queries(boolean executeIPv6Queries) {
    this.executeIPv6Queries = executeIPv6Queries;
    // Ensure at least one protocol is enabled - if disabling IPv6, enable IPv4
    if (!executeIPv6Queries && !this.executeIPv4Queries) {
        this.executeIPv4Queries = true;
    }
}

public void setAdditionalConformanceQueries(boolean additionalConformanceQueries) {
    this.additionalConformanceQueries = additionalConformanceQueries;
}

public void setUseRdapProfileFeb2024(boolean value) {
    this.dependantRdapProfileGtld.exclusiveRdapProfile.dependantRdapProfile.useRdapProfileFeb2024 = value;
}

public void setUseRdapProfileFeb2019(boolean value) {
    this.dependantRdapProfileGtld.exclusiveRdapProfile.dependantRdapProfile.useRdapProfileFeb2019 = value;
}

public void setGtldRegistry(boolean value) {
    this.dependantRdapProfileGtld.exclusiveRdapProfile.exclusiveGtldType.dependantRegistryThin.gtldRegistry = value;
}

public void setGtldRegistrar(boolean value) {
    this.dependantRdapProfileGtld.exclusiveRdapProfile.exclusiveGtldType.gtldRegistrar = value;
}

public void setThin(boolean value) {
    this.dependantRdapProfileGtld.exclusiveRdapProfile.exclusiveGtldType.dependantRegistryThin.thin = value;
}

public void setVerbose(boolean isVerbose) {
    this.isVerbose = isVerbose;
}
  @Override
  public Integer call() throws Exception {
    // these must be set before we do anything else
    System.setProperty("com.sun.net.ssl.checkRevocation", "true");
    System.setProperty("com.sun.security.enableCRLDP", "true");
    Security.setProperty("ocsp.enable", "true");
    System.setProperty("jdk.tls.client.enableSessionTicketExtension", "false");
    System.setProperty("jdk.tls.disableCompression", "true");

    // All these have proved useful at one time or another in the past - Turn on whatever level you need for debugging
    //  System.setProperty("javax.net.debug", "all");
    //  System.setProperty("javax.net.debug", "ssl");
    //  System.setProperty("javax.net.debug", "ssl:handshake:verbose");
    //  System.setProperty("java.net.debug", "all");

    if (!isVerbose) {
      Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
      root.setLevel(Level.ERROR);
    }

    // Update executeIP*Queries based on command line options if provided
    if (ipVersionOptions != null) {
      if (ipVersionOptions.noIPv4Queries) {
        executeIPv4Queries = false;
        executeIPv6Queries = true;
      }
      if (ipVersionOptions.noIPv6Queries) {
        executeIPv6Queries = false;
        executeIPv4Queries = true;
      }
    }

    // we should never reach this point ... paranoid check
    if (!executeIPv4Queries && !executeIPv6Queries) {
      logger.error(ToolResult.BAD_USER_INPUT.getDescription());
      return ToolResult.BAD_USER_INPUT.getCode();
    }

    // No matter which validator, we need to initialize the dataset service
    RDAPDatasetService datasetService = CommonUtils.initializeDataSet(this);
    // if we couldn't do it - exit
    if(datasetService == null) {
      logger.error(ToolResult.DATASET_UNAVAILABLE.getDescription());
      return ToolResult.DATASET_UNAVAILABLE.getCode();
    }

    // Setup the configuration file
    ConfigurationFile configFile = CommonUtils.verifyConfigFile(this, fileSystem);

    // Ensure the config file is valid, exit if invalid
    if( configFile == null) {
      logger.error(ToolResult.CONFIG_INVALID.getDescription());
      return ToolResult.CONFIG_INVALID.getCode();
    }

    // Get the queryType - bail out if it is not correct
    if (uri.getScheme() != null && uri.getScheme().toLowerCase().startsWith(HTTP)) {
      RDAPHttpQueryTypeProcessor queryTypeProcessor = RDAPHttpQueryTypeProcessor.getInstance(this);
      if (!queryTypeProcessor.check(datasetService)) {
        logger.error(ToolResult.UNSUPPORTED_QUERY.getDescription());
        return queryTypeProcessor.getErrorStatus().getCode();
      }
    } else {
      // we are not using HTTP, we should be using a file
      // apparently all types are allowed atm.
      if (this.queryType == null) {
        logger.error(ToolResult.UNSUPPORTED_QUERY.getDescription());
        return ToolResult.UNSUPPORTED_QUERY.getCode();
      }
    }

    // Determine which validator we are using
    ValidatorWorkflow validator;
    if (uri.getScheme() != null && uri.getScheme().toLowerCase().startsWith(HTTP)) {
      validator = new RDAPHttpValidator(this, datasetService);
    } else {
      networkEnabled = false;
      validator = new RDAPFileValidator(this, datasetService);
    }

    // get the results file ready
    clean();
    RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
    resultFile.initialize(RDAPValidatorResultsImpl.getInstance(), this, configFile, fileSystem);

    // Are we querying over the network or is this a file on our system?
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

      if(DNSCacheResolver.hasNoAddresses(DNSCacheResolver.getHostnameFromUrl(uri.toString()))) {
        logger.info("Unable to resolve an IP address endpoint using DNS for uri:  "  + DNSCacheResolver.getHostnameFromUrl(uri.toString()));
      }


      // Removing extra errors to avoid discrepancies between profiles when 404 status code is returned
      if(ConnectionTracker.getInstance().isResourceNotFoundNoteWarning(this)) {
        logger.info("All HEAD and Main queries returned a 404 Not Found response code.");
        resultFile.removeErrors();
        resultFile.removeResultGroups();
      } else {
        logger.info("At least one HEAD or Main query returned a non-404 Not Found response code.");
      }

      // Build the result file
       if(!resultFile.build()) {
          logger.error("Unable to write to results file: " + validator.getResultsPath());
          return ToolResult.FILE_WRITE_ERROR.getCode();
        }

      // now the results file is set, print the path
      logger.info("Results file: {}",  validator.getResultsPath());
      setResultsFile(validator.getResultsPath());


      // Having network issues? You WILL need this.
      logger.info("ConnectionTracking: " + ConnectionTracker.getInstance().toString());

      // if we made it to here, exit 0
      return ZERO;
    }

    // else we are validating a file
    return validateWithoutNetwork(resultFile, validator);
  }


  int validateWithoutNetwork(RDAPValidationResultFile resultFile, ValidatorWorkflow validator) {
    // If network is not enabled or ipv4 AND ipv6 flags are off, validate and return
    int file_exit_code =  validator.validate();

    // No creating results file if  "USES_THIN_MODEL" exit code is triggered
    if(ToolResult.USES_THIN_MODEL.getCode() == file_exit_code) {
      return ToolResult.USES_THIN_MODEL.getCode();
    }

    if(!resultFile.build()) {
      logger.error("Unable to write to results file: " + validator.getResultsPath());
      return ToolResult.FILE_WRITE_ERROR.getCode();
    }
    logger.info("Results file: {}",  validator.getResultsPath());
    setResultsFile(validator.getResultsPath());
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

  @Override
  public void clean() {
    var resultsImpl = RDAPValidatorResultsImpl.getInstance();
    var connectionTracker = ConnectionTracker.getInstance();
    RDAPValidationResultFile.reset();
    resultsImpl.clear();
    connectionTracker.reset();
  }

  /**
   * Get validation errors from the last run.
   * @return List of validation errors, or empty list if no validation has been run
   */
  public List<RDAPValidationResult> getErrors() {
    try {
      RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
      return resultFile.getErrors();
    } catch (Exception e) {
      // Return empty list if validation hasn't run yet or failed
      return new java.util.ArrayList<>();
    }
  }

  /**
   * Get all validation results from the last run.
   * @return List of all validation results, or empty list if no validation has been run
   */
  public List<RDAPValidationResult> getAllResults() {
    try {
      RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
      return resultFile.getAllResults();
    } catch (Exception e) {
      // Return empty list if validation hasn't run yet or failed
      return new java.util.ArrayList<>();
    }
  }

  /**
   * Get the count of validation errors from the last run.
   * @return Number of validation errors
   */
  public int getErrorCount() {
    try {
      RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
      return resultFile.getErrorCount();  
    } catch (Exception e) {
      // Return 0 if validation hasn't run yet or failed
      return 0;
    }
  }

  /**
   * Get validation errors from the last run as a JSON array string.
   * @return JSON array string of validation errors, or empty array if no validation has been run
   */
  public String getErrorsAsJson() {
    try {
      RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
      Map<String, Object> resultsMap = resultFile.createResultsMap();
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> errors = (List<Map<String, Object>>) resultsMap.get("error");
      JSONArray jsonArray = new JSONArray(errors);
      return jsonArray.toString(PRETTY_PRINT_INDENT);
    } catch (Exception e) {
      // Return empty JSON array if validation hasn't run yet or failed
      return new JSONArray().toString();
    }
  }

  /**
   * Get all validation results from the last run as a JSON object string.
   * @return JSON object string containing all validation results, or empty results if no validation has been run
   */
  public String getAllResultsAsJson() {
    try {
      RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
      Map<String, Object> resultsMap = resultFile.createResultsMap();
      JSONObject jsonObject = new JSONObject(resultsMap);
      return jsonObject.toString(PRETTY_PRINT_INDENT);
    } catch (Exception e) {
      // Return empty results structure if validation hasn't run yet or failed
      JSONObject fallbackObject = new JSONObject();
      fallbackObject.put("error", new JSONArray());
      fallbackObject.put("warning", new JSONArray());
      fallbackObject.put("ignore", new JSONArray());
      fallbackObject.put("notes", new JSONArray());
      return fallbackObject.toString(PRETTY_PRINT_INDENT);
    }
  }

  /**
   * Get validation warnings from the last run as a JSON array string.
   * @return JSON array string of validation warnings, or empty array if no validation has been run
   */
  public String getWarningsAsJson() {
    try {
      RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
      Map<String, Object> resultsMap = resultFile.createResultsMap();
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> warnings = (List<Map<String, Object>>) resultsMap.get("warning");
      JSONArray jsonArray = new JSONArray(warnings);
      return jsonArray.toString(PRETTY_PRINT_INDENT);
    } catch (Exception e) {
      // Return empty JSON array if validation hasn't run yet or failed
      return new JSONArray().toString();
    }
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