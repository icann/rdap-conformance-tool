package org.icann.rdapconformance.validator.workflow.rdap;

import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParser;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.ValidatorWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RDAPValidator implements ValidatorWorkflow {

  private static final Logger logger = LoggerFactory.getLogger(RDAPValidator.class);

  private final RDAPValidatorConfiguration config;
  private final RDAPQueryTypeProcessor queryTypeProcessor;
  private final RDAPQuery query;
  private final FileSystem fileSystem;
  private final ConfigurationFileParser configParser;
  private final RDAPValidatorResults results;
  private final RDAPDatasetService datasetService;

  public RDAPValidator(RDAPValidatorConfiguration config,
      FileSystem fileSystem,
      RDAPQueryTypeProcessor queryTypeProcessor,
      RDAPQuery query) {
    this(config, fileSystem, queryTypeProcessor, query,
        new ConfigurationFileParser(fileSystem),
        new RDAPValidatorResults(),
        new RDAPDatasetService(fileSystem));
  }

  RDAPValidator(RDAPValidatorConfiguration config,
      FileSystem fileSystem,
      RDAPQueryTypeProcessor queryTypeProcessor,
      RDAPQuery query,
      ConfigurationFileParser configParser,
      RDAPValidatorResults results,
      RDAPDatasetService datasetService) {
    this.config = config;
    this.fileSystem = fileSystem;
    this.query = query;
    if (!this.config.check()) {
      logger.error("Please fix the configuration");
      throw new RuntimeException("Please fix the configuration");
    }
    this.queryTypeProcessor = queryTypeProcessor;
    this.configParser = configParser;
    this.results = results;
    this.datasetService = datasetService;
  }

  @Override
  public int validate() {
    /*
     * Parse the configuration definition file, and if the file is not parsable,
     * exit with a return code of 1.
     */
    ConfigurationFile configurationFile;
    try {
      configurationFile = configParser.parse(this.config.getConfigurationFile());
    } catch (Exception e) {
      logger.error("Configuration is invalid", e);
      return RDAPValidationStatus.CONFIG_INVALID.getValue();
    }

    final RDAPValidationResultFile rdapValidationResultFile = new RDAPValidationResultFile(results,
        config, configurationFile, fileSystem);

    /* If the parameter (--use-local-dataset) is set, use the dataset found in the filesystem,
     * download the dataset not found in the filesystem, and persist them in the filesystem.
     * If the parameter (--use-local-dataset) is not set, download all the dataset, and
     * overwrite the dataset in the filesystem.
     * If one or more dataset cannot be downloaded, exit with a return code of 2.
     */
    if (!datasetService.download(this.config.useLocalDatasets())) {
      return RDAPValidationStatus.DATASET_UNAVAILABLE.getValue();
    }

    if (!queryTypeProcessor.check()) {
      return queryTypeProcessor.getErrorStatus().getValue();
    }

    if (!query.run()) {
      query.getStatusCode().ifPresent(rdapValidationResultFile::build);
      return query.getErrorStatus().getValue();
    }

    if (!query.checkWithQueryType(queryTypeProcessor.getQueryType())) {
      query.getStatusCode().ifPresent(rdapValidationResultFile::build);
      return query.getErrorStatus().getValue();
    }

    SchemaValidator validator = null;
    if (query.isErrorContent()) {
      validator = new SchemaValidator("rdap_error.json", results);
    } else if (RDAPQueryType.DOMAIN.equals(queryTypeProcessor.getQueryType())) {
      validator = new SchemaValidator("rdap_domain.json", results);
    } else if (RDAPQueryType.HELP.equals(queryTypeProcessor.getQueryType())) {
      validator = new SchemaValidator("rdap_help.json", results);
    } else if (RDAPQueryType.NAMESERVER.equals(queryTypeProcessor.getQueryType())) {
      validator = new SchemaValidator("rdap_nameserver.json", results);
    } else if (RDAPQueryType.NAMESERVERS.equals(queryTypeProcessor.getQueryType())) {
      validator = new SchemaValidator("rdap_nameservers.json", results);
    } else if (RDAPQueryType.ENTITY.equals(queryTypeProcessor.getQueryType())) {
      validator = new SchemaValidator("rdap_entities.json", results);
    }
    assert null != validator;
    validator.validate(query.getData());

    /*
     * Additionally, apply the relevant collection tests when the option
     * --use-rdap-profile-february-2019 is set.
     */
    /* TODO */

    query.getStatusCode().ifPresent(rdapValidationResultFile::build);

    return RDAPValidationStatus.SUCCESS.getValue();
  }
}
