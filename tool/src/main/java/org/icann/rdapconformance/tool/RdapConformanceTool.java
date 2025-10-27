package org.icann.rdapconformance.tool;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import java.io.File;
import java.net.URI;
import java.security.Security;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.SystemUtils;
import org.everit.json.schema.internal.IPV4Validator;
import org.everit.json.schema.internal.IPV6Validator;
import org.icann.rdapconformance.validator.workflow.rdap.*;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidator;
import org.slf4j.LoggerFactory;
import org.icann.rdapconformance.tool.progress.ProgressTracker;
import org.icann.rdapconformance.tool.progress.ProgressPhase;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.ConnectionTracker;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.ProgressCallback;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.ToolResult;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.LocalFileSystem;
import org.icann.rdapconformance.validator.workflow.ValidatorWorkflow;
import org.icann.rdapconformance.validator.workflow.rdap.file.RDAPFileValidator;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQueryTypeProcessor;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpValidator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetServiceImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.icann.rdapconformance.validator.CommonUtils.*;

/**
 * Main entry point and orchestrator for the RDAP Conformance Tool.
 *
 * <p>This class serves as the primary command-line interface and coordination hub for RDAP
 * (Registration Data Access Protocol) validation. It handles:</p>
 *
 * <ul>
 *   <li>Command-line argument parsing using picocli</li>
 *   <li>Configuration of logging levels and verbose mode</li>
 *   <li>Initialization of IANA datasets required for validation</li>
 *   <li>Coordination of IPv4 and IPv6 validation rounds</li>
 *   <li>Progress tracking and user feedback</li>
 *   <li>Results file generation and output</li>
 * </ul>
 *
 * <p>The tool can validate RDAP responses from live servers via HTTP or from local JSON files.
 * It supports both gTLD registry and registrar profiles, with options for different RDAP
 * profile versions (February 2019 and February 2024).</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * java -jar rdapct.jar -v -c config.json --gtld-registrar
 *      --use-rdap-profile-february-2024 https://rdap.example.com/domain/example.com
 * </pre>
 *
 * @see RDAPValidatorConfiguration
 * @see RDAPHttpValidator
 * @see RDAPFileValidator
 * @since 1.0.0
 */
@Command(name = "rdap-conformance-tool", versionProvider = org.icann.rdapconformance.tool.VersionProvider.class, mixinStandardHelpOptions = true)
public class RdapConformanceTool implements RDAPValidatorConfiguration, Callable<Integer> {

  // Static initializer to set default log level before any logger initialization
  static {
    // Set a fallback default that will be overridden by the call() method if needed
    if (System.getProperty("defaultLogLevel") == null) {
      System.setProperty("defaultLogLevel", "ERROR");
    }
  }

  // Create a logger
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RdapConformanceTool.class);
  public static final int PRETTY_PRINT_INDENT = 2;
  
  // Progress tracking constants
  private static final int DATASETS_COUNT = 13;
  private static final int OPERATIONS_PER_DATASET = 2;  // download + parse
  private static final int DATASET_TOTAL_STEPS = DATASETS_COUNT * OPERATIONS_PER_DATASET;
  private static final int ESTIMATED_VALIDATIONS_PER_ROUND = 75;
  private static final long THREAD_JOIN_TIMEOUT_MS = 1000;  // 1 second
  private static final long MAX_DATASET_WAIT_TIME_MS = 45000;  // 45 seconds
  private static final long MIN_STEP_DELAY_MS = 200;  // 200ms minimum per step

  @Parameters(paramLabel = "RDAP_URI", description = "The URI to be tested", index = "0")
  URI uri;

  private FileSystem fileSystem = new LocalFileSystem();

  // QueryContext for managing all validation components in a thread-safe manner
  private QueryContext queryContext;

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

  @Option(names = {"--dns-resolver"}, 
          description = "Custom DNS resolver IP address (e.g., 8.8.8.8 or 2001:4860:4860::8888)")
  private String customDnsResolver;

  // Progress tracking
  private ProgressTracker progressTracker;
  private boolean showProgress = true; // Default to true for CLI usage

  private boolean networkEnabled  = true;

  // IP version query options as mutually exclusive group
  private static class IpVersionQueriesOptions {
    @Option(names = {"--no-ipv4-queries"}, description = "No queries over IPv4 are to be issued")
    private boolean noIPv4Queries;

    @Option(names = {"--no-ipv6-queries"}, description = "No queries over IPv6 are to be issued")
    private boolean noIPv6Queries;
  }

/**
 * Sets the configuration file path for validation rules and settings.
 *
 * @param configurationFile the path to the configuration file
 */
public void setConfigurationFile(String configurationFile) {
    this.configurationFile = configurationFile;
}

/**
 * Sets the timeout for HTTP connections in seconds.
 *
 * @param timeout the connection timeout in seconds
 */
public void setTimeout(int timeout) {
    this.timeout = timeout;
}

/**
 * Sets the maximum number of HTTP redirects to follow.
 *
 * @param maxRedirects the maximum number of redirects (default: 3)
 */
public void setMaxRedirects(int maxRedirects) {
    this.maxRedirects = maxRedirects;
}

/**
 * Configures whether to use locally cached datasets instead of downloading from IANA.
 *
 * @param useLocalDatasets true to use local datasets, false to download fresh data
 */
public void setUseLocalDatasets(boolean useLocalDatasets) {
    this.useLocalDatasets = useLocalDatasets;
}

/**
 * Sets the path for the validation results file.
 *
 * @param resultsFile the file path where results will be written
 */
public void setResultsFile(String resultsFile) {
    this.resultsFile = resultsFile;
}

/**
 * Configures whether to execute validation queries over IPv4.
 * Automatically enables IPv6 if both protocols would be disabled.
 *
 * @param executeIPv4Queries true to enable IPv4 queries, false to disable
 */
public void setExecuteIPv4Queries(boolean executeIPv4Queries) {
    this.executeIPv4Queries = executeIPv4Queries;
    // Ensure at least one protocol is enabled - if disabling IPv4, enable IPv6
    if (!executeIPv4Queries && !this.executeIPv6Queries) {
        this.executeIPv6Queries = true;
    }
}

/**
 * Configures whether to execute validation queries over IPv6.
 * Automatically enables IPv4 if both protocols would be disabled.
 *
 * @param executeIPv6Queries true to enable IPv6 queries, false to disable
 */
public void setExecuteIPv6Queries(boolean executeIPv6Queries) {
    this.executeIPv6Queries = executeIPv6Queries;
    // Ensure at least one protocol is enabled - if disabling IPv6, enable IPv4
    if (!executeIPv6Queries && !this.executeIPv4Queries) {
        this.executeIPv4Queries = true;
    }
}

/**
 * Configures whether to execute additional conformance queries (/help and invalid domain).
 *
 * @param additionalConformanceQueries true to enable additional queries
 */
public void setAdditionalConformanceQueries(boolean additionalConformanceQueries) {
    this.additionalConformanceQueries = additionalConformanceQueries;
}

/**
 * Sets whether to use the RDAP Profile February 2024 specification.
 *
 * @param value true to use February 2024 profile
 */
public void setUseRdapProfileFeb2024(boolean value) {
    this.dependantRdapProfileGtld.exclusiveRdapProfile.dependantRdapProfile.useRdapProfileFeb2024 = value;
}

/**
 * Sets whether to use the RDAP Profile February 2019 specification.
 *
 * @param value true to use February 2019 profile
 */
public void setUseRdapProfileFeb2019(boolean value) {
    this.dependantRdapProfileGtld.exclusiveRdapProfile.dependantRdapProfile.useRdapProfileFeb2019 = value;
}

/**
 * Configures validation for gTLD registry responses.
 *
 * @param value true to validate as gTLD registry
 */
public void setGtldRegistry(boolean value) {
    this.dependantRdapProfileGtld.exclusiveRdapProfile.exclusiveGtldType.dependantRegistryThin.gtldRegistry = value;
}

/**
 * Configures validation for gTLD registrar responses.
 *
 * @param value true to validate as gTLD registrar
 */
public void setGtldRegistrar(boolean value) {
    this.dependantRdapProfileGtld.exclusiveRdapProfile.exclusiveGtldType.gtldRegistrar = value;
}

/**
 * Sets whether the TLD uses the thin registry model.
 *
 * @param value true if using thin model, false for thick model
 */
public void setThin(boolean value) {
    this.dependantRdapProfileGtld.exclusiveRdapProfile.exclusiveGtldType.dependantRegistryThin.thin = value;
}

/**
 * Configures verbose output mode. When enabled, shows DEBUG level logs and disables progress bar.
 *
 * @param isVerbose true to enable verbose output
 */
public void setVerbose(boolean isVerbose) {
    this.isVerbose = isVerbose;
}

/**
 * Sets whether to display progress information during validation.
 *
 * @param showProgress true to show progress bar/updates
 */
public void setShowProgress(boolean showProgress) {
    this.showProgress = showProgress;
}


  /**
   * Main execution method for the RDAP Conformance Tool.
   *
   * <p>This method orchestrates the complete validation workflow:</p>
   * <ol>
   *   <li>Configures logging based on verbose flag and system properties</li>
   *   <li>Initializes IANA datasets required for validation</li>
   *   <li>Performs DNS resolution for target hostnames</li>
   *   <li>Executes validation queries over IPv4 and/or IPv6</li>
   *   <li>Generates and writes validation results</li>
   * </ol>
   *
   * <p>The tool validates RDAP responses against the appropriate specification profile
   * (February 2019 or February 2024) and generates a comprehensive results file containing
   * errors, warnings, and validation details.</p>
   *
   * @return exit code: 0 for success, non-zero for various error conditions
   * @throws Exception if validation cannot be completed due to configuration or system errors
   * @see ToolResult for possible exit codes
   */

  public void setCustomDnsResolver(String customDnsResolver) {
      this.customDnsResolver = customDnsResolver;
  }

  public String getCustomDnsResolver() {
      return this.customDnsResolver;
  }

  @Override
  public Integer call() throws Exception {
    // Determine if user wants logging output vs progress bar
    boolean hasSystemLogProperty = (System.getProperty("logging.level.root") != null ||
                                   System.getProperty("logLevel") != null);


    if (isVerbose) {
      // Verbose mode always forces DEBUG level, overriding any system properties
      System.setProperty("defaultLogLevel", "DEBUG");
      // Clear any user-provided overrides when verbose is set
      System.clearProperty("logging.level.root");
      System.clearProperty("logLevel");
      // Disable progress bar when verbose
      showProgress = false;
    } else if (hasSystemLogProperty) {
      // System properties provided - show logs instead of progress bar
      showProgress = false;
      // Copy the system property to our defaultLogLevel and clear originals (same as verbose mode)
      String systemLevel = System.getProperty("logging.level.root");
      if (systemLevel == null) {
        systemLevel = System.getProperty("logLevel");
      }
      if (systemLevel != null) {
        System.setProperty("defaultLogLevel", systemLevel);
        // Clear the original properties so logback uses our defaultLogLevel
        System.clearProperty("logging.level.root");
        System.clearProperty("logLevel");
      }
    } else {
      // No flags - show progress bar with ERROR level only
      System.setProperty("defaultLogLevel", "ERROR");
      showProgress = true;
    }

    // Force logback reconfiguration to pick up the new property
    try {
      LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(context);
      context.reset();
      // Use the default logback.xml from classpath
      configurator.doConfigure(getClass().getClassLoader().getResourceAsStream("logback.xml"));

      // Also programmatically set the root logger level to ensure it takes effect
      Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
      String targetLevel = isVerbose ? "DEBUG" :
                          (hasSystemLogProperty ? System.getProperty("defaultLogLevel", "ERROR") : "ERROR");

      if ("DEBUG".equals(targetLevel)) {
        root.setLevel(Level.DEBUG);
      } else if ("INFO".equals(targetLevel)) {
        root.setLevel(Level.INFO);
      } else if ("WARN".equals(targetLevel)) {
        root.setLevel(Level.WARN);
      } else {
        root.setLevel(Level.ERROR);
      }

    } catch (Exception e) {
      // Fall back to programmatic setting if reconfiguration fails
      Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
      String targetLevel = isVerbose ? "DEBUG" :
                          (hasSystemLogProperty ? System.getProperty("defaultLogLevel", "ERROR") : "ERROR");

      if ("DEBUG".equals(targetLevel)) {
        root.setLevel(Level.DEBUG);
      } else if ("INFO".equals(targetLevel)) {
        root.setLevel(Level.INFO);
      } else if ("WARN".equals(targetLevel)) {
        root.setLevel(Level.WARN);
      } else {
        root.setLevel(Level.ERROR);
      }
    }

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

    // Only reset to ERROR if user provided no logging flags at all
    if (!isVerbose && !hasSystemLogProperty) {
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

    // Validate custom DNS resolver if provided
    if (customDnsResolver != null && !customDnsResolver.isEmpty() && !isValidIpAddress(customDnsResolver)) {
      logger.error(ToolResult.BAD_USER_INPUT.getDescription() + ": Invalid DNS resolver IP address format: " + customDnsResolver);
      return ToolResult.BAD_USER_INPUT.getCode();
    }

    // Initialize progress tracking if not in verbose mode (before DNS check so progress is visible)
    initializeProgressTracking();

    // DNS resolver initialization will be handled by QueryContext when it's created

    // No matter which validator, we need to initialize the dataset service
    RDAPDatasetService datasetService = initializeDataSetWithProgress();
    // if we couldn't do it - exit
    if(datasetService == null) {
      logger.error(ToolResult.DATASET_UNAVAILABLE.getDescription());
      return ToolResult.DATASET_UNAVAILABLE.getCode();
    }

    // First check if the configuration file exists
    if (!CommonUtils.configFileExists(this, fileSystem)) {
      logger.error(ToolResult.CONFIG_DOES_NOT_EXIST.getDescription());
      return ToolResult.CONFIG_DOES_NOT_EXIST.getCode();
    }

    // Setup the configuration file
    ConfigurationFile configFile = CommonUtils.verifyConfigFile(this, fileSystem);

    // Ensure the config file is valid, exit if invalid
    if( configFile == null) {
      logger.error(ToolResult.CONFIG_INVALID.getDescription());
      return ToolResult.CONFIG_INVALID.getCode();
    }

    // get the results file ready
    clean();

    // Create QueryContext as the central "world object" with all components
    // This includes DNS resolver initialization with any custom DNS server
    try {
      updateProgressPhase(ProgressPhase.DNS_RESOLUTION);
      queryContext = QueryContext.create(this, datasetService, new org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQuery(this), customDnsResolver);
      logger.debug("QueryContext created successfully with custom DNS server: {}", customDnsResolver);
    } catch (RuntimeException e) {
      logger.error("Failed to initialize QueryContext with DNS resolver: {}", e.getMessage());
      if (e.getMessage().contains("not responding")) {
          logger.error(ToolResult.BAD_USER_INPUT.getDescription() +
                       ": DNS server is not reachable: " + customDnsResolver);
      } else {
          logger.error(ToolResult.BAD_USER_INPUT.getDescription() +
                       ": Invalid DNS resolver IP address: " + customDnsResolver);
      }
      return ToolResult.BAD_USER_INPUT.getCode();
    }

    // Get the queryType - bail out if it is not correct
    RDAPHttpQueryTypeProcessor queryTypeProcessor = null;
    if (uri.getScheme() != null && uri.getScheme().toLowerCase().startsWith(HTTP)) {
      queryTypeProcessor = queryContext.getHttpQueryTypeProcessor();
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
      validator = new RDAPHttpValidator(queryContext);
    } else {
      networkEnabled = false;
      validator = new RDAPFileValidator(this, datasetService);
    }

    // Initialize the result file from the tool's QueryContext
    RDAPValidationResultFile resultFile;
    if (validator instanceof RDAPValidator) {
        // Use the tool's QueryContext for RDAP validators
        resultFile = queryContext.getResultFile();
        resultFile.initialize(queryContext.getResults(), this, configFile, fileSystem);
    } else {
        // For non-RDAP validators, create a basic QueryContext and use instance-based approach
        if (queryContext == null) {
            queryContext = QueryContext.create(this, datasetService, null, customDnsResolver);
        }
        resultFile = queryContext.getResultFile();
        resultFile.initialize(queryContext.getResults(), this, configFile, fileSystem);
    }

    // Are we querying over the network or is this a file on our system?
    if (networkEnabled) {
      // DNS resolver was already initialized when QueryContext was created
      // Now perform the actual DNS lookups for the target host using QueryContext
      updateProgressPhase("DNS-Resolving");
      queryContext.getDnsResolver().initFromUrl(uri.toString());
      incrementProgress(); // DNS initialization step
      updateProgressPhase("DNS-Validation");
      queryContext.getDnsResolver().doZeroIPAddressesValidation(queryContext, uri.toString(), executeIPv6Queries, executeIPv4Queries);
      incrementProgress(); // DNS validation step
      
      // Start network validation phase
      updateProgressPhase(ProgressPhase.NETWORK_VALIDATION);

      // Check if parallel IP version execution is enabled
      // Execute IPv4 and IPv6 validations sequentially
      logger.info("Executing IPv4 and IPv6 validations sequentially");
        
        // do v6
        logger.debug("IPv6 check: executeIPv6Queries={}, hasV6Addresses={}", executeIPv6Queries, queryContext.getDnsResolver().hasV6Addresses(uri.toString()));
        if(executeIPv6Queries && queryContext.getDnsResolver().hasV6Addresses(uri.toString())) {
          logger.debug("Starting IPv6 validations...");
          updateProgressPhase("IPv6-JSON");

          // Use QueryContext's NetworkInfo instead of singleton
          if (validator instanceof RDAPValidator) {
              RDAPValidator rdapValidator = (RDAPValidator) validator;
              rdapValidator.getQueryContext().setStackToV6();
              rdapValidator.getQueryContext().setAcceptHeaderToApplicationJson();
          }
          // Note: NetworkInfo static calls removed - bridge pattern will delegate to QueryContext

          logger.debug("About to run IPv6-JSON validation");
          int v6ret = validator.validate();
          logger.debug("IPv6-JSON validation completed with result: {}", v6ret);
          incrementProgress(ESTIMATED_VALIDATIONS_PER_ROUND); // Estimated validations per round

          // set the header to RDAP+JSON and redo the validations
          updateProgressPhase("IPv6-RDAP+JSON");

          // Use QueryContext's NetworkInfo instead of singleton
          if (validator instanceof RDAPValidator) {
              RDAPValidator rdapValidator = (RDAPValidator) validator;
              rdapValidator.getQueryContext().setAcceptHeaderToApplicationRdapJson();
          }
          // Note: NetworkInfo static calls removed - bridge pattern will delegate to QueryContext

          logger.debug("About to run IPv6-RDAP+JSON validation");
          int v6ret2 = validator.validate();
          logger.debug("IPv6-RDAP+JSON validation completed with result: {}", v6ret2);
          incrementProgress(ESTIMATED_VALIDATIONS_PER_ROUND); // Estimated validations per round
          logger.debug("IPv6 validations completed");
        } else {
          logger.debug("Skipping IPv6 validations - executeIPv6Queries={}, hasV6Addresses={}", executeIPv6Queries, queryContext.getDnsResolver().hasV6Addresses(uri.toString()));
        }

        // do v4
        if(executeIPv4Queries && queryContext.getDnsResolver().hasV4Addresses(uri.toString())) {
          updateProgressPhase("IPv4-JSON");

          // Use QueryContext's NetworkInfo instead of singleton
          if (validator instanceof RDAPValidator) {
              RDAPValidator rdapValidator = (RDAPValidator) validator;
              rdapValidator.getQueryContext().setStackToV4();
              rdapValidator.getQueryContext().setAcceptHeaderToApplicationJson();
          }
          // Note: NetworkInfo static calls removed - bridge pattern will delegate to QueryContext

          int v4ret = validator.validate();
          incrementProgress(ESTIMATED_VALIDATIONS_PER_ROUND); // Estimated validations per round

          // set the header to RDAP+JSON and redo the validations
          updateProgressPhase("IPv4-RDAP+JSON");

          // Use QueryContext's NetworkInfo instead of singleton
          if (validator instanceof RDAPValidator) {
              RDAPValidator rdapValidator = (RDAPValidator) validator;
              rdapValidator.getQueryContext().setAcceptHeaderToApplicationRdapJson();
          }
          // Note: NetworkInfo static calls removed - bridge pattern will delegate to QueryContext

          int v4ret2 = validator.validate();
          incrementProgress(ESTIMATED_VALIDATIONS_PER_ROUND); // Estimated validations per round
        }

      if(queryContext.getDnsResolver().hasNoAddresses(DNSCacheResolver.getHostnameFromUrl(uri.toString()))) {
        logger.info("Unable to resolve an IP address endpoint using DNS for uri:  "  + DNSCacheResolver.getHostnameFromUrl(uri.toString()));
      }


      // Removing extra errors to avoid discrepancies between profiles when 404 status code is returned
      // Use QueryContext's ConnectionTracker instead of singleton
      boolean isResourceNotFound = false;
      if (validator instanceof RDAPValidator) {
          RDAPValidator rdapValidator = (RDAPValidator) validator;
          isResourceNotFound = rdapValidator.getQueryContext().getConnectionTracker().isResourceNotFoundNoteWarning(rdapValidator.getQueryContext(), this);
      } else {
          isResourceNotFound = queryContext.getConnectionTracker().isResourceNotFoundNoteWarning(queryContext, this);
      }

      if(isResourceNotFound) {
        logger.debug("All HEAD and Main queries returned a 404 Not Found response code.");
        resultFile.removeErrors();
        resultFile.removeResultGroups();
      } else {
        logger.debug("At least one HEAD or Main query returned a non-404 Not Found response code.");
      }

      // Build the result file
      updateProgressPhase(ProgressPhase.RESULTS_GENERATION);
       if(!resultFile.build()) {
          logger.error("Unable to write to results file: " + validator.getResultsPath());
          return ToolResult.FILE_WRITE_ERROR.getCode();
        }
      incrementProgress(); // Results generation step

      // now the results file is set, print the path
      logger.info("Results file: {}",  validator.getResultsPath());
      setResultsFile(validator.getResultsPath());
      
      // Always show results file location to user, even when using progress bar (non-verbose mode)
      if (!isVerbose) {
        System.out.println("\nResults saved to: " + validator.getResultsPath());
      }


      // Having network issues? You WILL need this.
      // Use QueryContext's ConnectionTracker instead of singleton
      if (validator instanceof RDAPValidator) {
          RDAPValidator rdapValidator = (RDAPValidator) validator;
          logger.debug("ConnectionTracking: " + rdapValidator.getQueryContext().getConnectionTracker().toString());
      } else {
          logger.debug("ConnectionTracking: " + queryContext.getConnectionTracker().toString());
      }

      // Complete progress tracking
      completeProgress();

      // Check if domain validation found errors (even though we continued execution)
      if (queryTypeProcessor != null && queryTypeProcessor.getErrorStatus() != null && queryTypeProcessor.getErrorStatus() != ToolResult.SUCCESS) {
        logger.info("Domain validation errors detected - returning error status: {}", queryTypeProcessor.getErrorStatus().getCode());
        return queryTypeProcessor.getErrorStatus().getCode();
      }

      // if we made it to here, exit 0
      return ZERO;
    } else {
      // else we are validating a file
      return validateWithoutNetwork(resultFile, validator);
    }
  }

  /**
   * Validates a local RDAP JSON file without network access.
   *
   * <p>This method is used when the input URI points to a local file rather than
   * an HTTP URL. It performs the same validation logic as network-based validation
   * but skips DNS resolution and HTTP requests.</p>
   *
   * @param resultFile the results file handler for collecting validation outcomes
   * @param validator the file validator workflow to execute
   * @return exit code indicating validation result
   */
  private int validateWithoutNetwork(RDAPValidationResultFile resultFile, ValidatorWorkflow validator) {
    // If network is not enabled or ipv4 AND ipv6 flags are off, validate and return
    updateProgressPhase(ProgressPhase.NETWORK_VALIDATION);
    updateProgressPhase("FileValidation");
    int file_exit_code =  validator.validate();
    incrementProgress(ESTIMATED_VALIDATIONS_PER_ROUND); // File validation step

    // No creating results file if  "USES_THIN_MODEL" exit code is triggered
    if(ToolResult.USES_THIN_MODEL.getCode() == file_exit_code) {
      return ToolResult.USES_THIN_MODEL.getCode();
    }

    updateProgressPhase(ProgressPhase.RESULTS_GENERATION);
    if(!resultFile.build()) {
      logger.error("Unable to write to results file: " + validator.getResultsPath());
      return ToolResult.FILE_WRITE_ERROR.getCode();
    }
    incrementProgress(); // Results generation step
    logger.info("Results file: {}",  validator.getResultsPath());
    setResultsFile(validator.getResultsPath());
    
    // Always show results file location to user, even when using progress bar (non-verbose mode)
    if (!isVerbose) {
      System.out.println("\nResults saved to: " + validator.getResultsPath());
    }
    
    // Complete progress tracking
    completeProgress();
    
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
    // Use QueryContext components if available, otherwise this is called before QueryContext creation
    if (queryContext != null) {
      queryContext.getResults().clear();
      queryContext.getConnectionTracker().reset();
      // ResultFile doesn't need reset as it will be reinitialized
    }
    // Note: In the new architecture, clean() is called before QueryContext creation,
    // so this method has minimal effect during startup
  }

  /**
   * Get validation errors from the last run.
   * @return List of validation errors, or empty list if no validation has been run
   */
  public List<RDAPValidationResult> getErrors() {
    try {
      if (queryContext != null) {
        return queryContext.getResultFile().getErrors();
      }
      // Return empty list if QueryContext hasn't been created yet
      return new java.util.ArrayList<>();
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
      if (queryContext != null) {
        return queryContext.getResultFile().getAllResults();
      }
      // Return empty list if QueryContext hasn't been created yet
      return new java.util.ArrayList<>();
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
      if (queryContext != null) {
        return queryContext.getResultFile().getErrorCount();
      }
      // Return 0 if QueryContext hasn't been created yet
      return 0;
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
      if (queryContext != null) {
        Map<String, Object> resultsMap = queryContext.getResultFile().createResultsMap();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> errors = (List<Map<String, Object>>) resultsMap.get("error");
        JSONArray jsonArray = new JSONArray(errors);
        return jsonArray.toString(PRETTY_PRINT_INDENT);
      }
      // Return empty JSON array if QueryContext hasn't been created yet
      return new JSONArray().toString();
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
      // Use the QueryContext to get the resultFile if available, otherwise return fallback
      if (queryContext == null) {
        throw new IllegalStateException("No validation has been run yet");
      }
      RDAPValidationResultFile resultFile = queryContext.getResultFile();
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
      // Use the QueryContext to get the resultFile if available, otherwise return fallback
      if (queryContext == null) {
        throw new IllegalStateException("No validation has been run yet");
      }
      RDAPValidationResultFile resultFile = queryContext.getResultFile();
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

  /**
   * Initialize progress tracking if not in verbose mode and progress is enabled.
   */
  private void initializeProgressTracking() {
    if (!isVerbose && showProgress) {
      int totalSteps = calculateTotalSteps();
      progressTracker = new ProgressTracker(totalSteps, isVerbose);
      progressTracker.start();
    }
  }

  /**
   * Calculate the total number of steps for progress tracking.
   */
  private int calculateTotalSteps() {
    int steps = 0;
    
    // Dataset operations: datasets * operations (download + parse)
    steps += DATASET_TOTAL_STEPS;
    
    // DNS operations: typically 2 queries (A and AAAA records)
    steps += 2;
    
    // Network validation steps based on IP version flags
    steps += getNetworkValidationSteps();
    
    // Results generation: 1 step
    steps += 1;
    
    return steps;
  }

  /**
   * Calculate network validation steps based on IP version configuration.
   */
  private int getNetworkValidationSteps() {
    int rounds = 0;
    
    if (executeIPv6Queries) {
      rounds += 2; // application/json + application/rdap+json
    }
    
    if (executeIPv4Queries) {
      rounds += 2; // application/json + application/rdap+json
    }
    
    // Estimate validations per round (conservative estimate)
    return rounds * ESTIMATED_VALIDATIONS_PER_ROUND;
  }

  /**
   * Update progress tracking phase.
   */
  public void updateProgressPhase(ProgressPhase phase) {
    if (progressTracker != null) {
      progressTracker.updatePhase(phase);
    }
  }

  /**
   * Update progress tracking phase with custom name.
   */
  public void updateProgressPhase(String phaseName) {
    if (progressTracker != null) {
      progressTracker.updatePhase(phaseName);
    }
  }

  /**
   * Increment progress by one step.
   */
  public void incrementProgress() {
    if (progressTracker != null) {
      progressTracker.incrementStep();
    }
  }

  /**
   * Increment progress by multiple steps.
   */
  public void incrementProgress(int steps) {
    if (progressTracker != null) {
      progressTracker.incrementSteps(steps);
    }
  }

  /**
   * Complete progress tracking.
   */
  public void completeProgress() {
    if (progressTracker != null) {
      progressTracker.complete();
    }
  }

  /**
   * Get the progress tracker (for access by other classes).
   */
  public ProgressTracker getProgressTracker() {
    return progressTracker;
  }

  /**
   * Initialize dataset service with progress tracking using actual completion events.
   */
  private RDAPDatasetService initializeDataSetWithProgress() {
    // Show appropriate phase name based on whether we're downloading or using local datasets
    if (useLocalDatasets) {
      updateProgressPhase("DatasetLoad");
    } else {
      updateProgressPhase(ProgressPhase.DATASET_DOWNLOAD);
    }
    
    // Create a progress callback that updates the real progress tracker
    ProgressCallback progressCallback = null;
    if (progressTracker != null && showProgress) {
      progressCallback = new DatasetProgressCallback();
    }
    
    // Create dataset service directly instead of using non-existent CommonUtils method
    FileSystem fileSystem = new LocalFileSystem();
    RDAPDatasetService datasetService = new RDAPDatasetServiceImpl(fileSystem);
    datasetService.download(useLocalDatasets, progressCallback);
    
    // Ensure we're at the right progress point (after dataset phase)
    if (progressTracker != null && datasetService != null) {
      // Set progress to after dataset operations
      progressTracker.setCurrentStep(DATASET_TOTAL_STEPS);
      updateProgressPhase(ProgressPhase.DNS_RESOLUTION);
    }
    
    return datasetService;
  }

  /**
   * Progress callback implementation that updates the progress tracker with real events.
   */
  private class DatasetProgressCallback implements ProgressCallback {
    private int completedOperations = 0;
    
    @Override
    public void onDatasetDownloadStarted(String datasetName) {
      // Show appropriate phase name based on whether we're downloading or using local datasets
      if (useLocalDatasets) {
        updateProgressPhase("DatasetLoad");
      } else {
        updateProgressPhase("DatasetDownload");
      }
    }
    
    @Override
    public void onDatasetDownloadCompleted(String datasetName) {
      if (progressTracker != null) {
        completedOperations++;
        progressTracker.setCurrentStep(completedOperations);
      }
    }
    
    @Override
    public void onDatasetParseStarted(String datasetName) {
      updateProgressPhase("DatasetParse");
    }
    
    @Override
    public void onDatasetParseCompleted(String datasetName) {
      if (progressTracker != null) {
        completedOperations++;
        progressTracker.setCurrentStep(completedOperations);
      }
    }
    
    @Override
    public void onDatasetError(String datasetName, String operation, Throwable error) {
      // Progress continues even on error - the overall operation will handle failures
      if (progressTracker != null) {
        completedOperations++;
        progressTracker.setCurrentStep(completedOperations);
      }
    }
  }

  /**
   * Validate if the given string is a valid IP address (IPv4 or IPv6).
   * Uses existing IP validators from the Everit JSON Schema library.
   * This method ensures the input is a literal IP address, not a hostname.
   */
  private boolean isValidIpAddress(String ip) {
    if (ip == null || ip.trim().isEmpty()) {
      return false;
    }

    // Try IPv4 validation using Everit validator
    if (new IPV4Validator().validate(ip).isEmpty()) {
      return true;
    }

    // Try IPv6 validation using Everit validator
    if (new IPV6Validator().validate(ip).isEmpty()) {
      return true;
    }

    return false;
  }
}