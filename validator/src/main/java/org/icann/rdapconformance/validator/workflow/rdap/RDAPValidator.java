package org.icann.rdapconformance.validator.workflow.rdap;

import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParser;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.LocalFileSystem;
import org.icann.rdapconformance.validator.workflow.ValidatorWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RDAPValidator implements ValidatorWorkflow {

  private static final Logger logger = LoggerFactory.getLogger(RDAPValidator.class);

  protected final RDAPValidatorConfiguration config;
  protected final RDAPQueryTypeProcessor queryTypeProcessor;
  protected final RDAPQuery query;

  protected final RDAPValidatorResults results = new RDAPValidatorResults();

  public RDAPValidator(RDAPValidatorConfiguration config,
      RDAPQueryTypeProcessor queryTypeProcessor,
      RDAPQuery query) {
    this.config = config;
    this.query = query;
    if (!this.config.check()) {
      logger.error("Please fix the configuration");
      throw new RuntimeException("Please fix the configuration");
    }
    this.queryTypeProcessor = queryTypeProcessor;
  }

  @Override
  public int validate() {
    /*
     * Parse the configuration definition file, and if the file is not parsable,
     * exit with a return code of 1.
     */
    ConfigurationFile configurationFile;
    try {
      ConfigurationFileParser configParser = new ConfigurationFileParser();
      configurationFile = configParser.parse(this.config.getConfigurationFile());
    } catch (Exception e) {
      logger.error("Configuration is invalid", e);
      return RDAPValidationStatus.CONFIG_INVALID.getValue();
    }

    RDAPValidationResultFile rdapValidationResultFile = new RDAPValidationResultFile(results,
        config, configurationFile, new LocalFileSystem());

    /* If the parameter (--use-local-datasets) is set, use the datasets found in the filesystem,
     * download the datasets not found in the filesystem, and persist them in the filesystem.
     * If the parameter (--use-local-datasets) is not set, download all the datasets, and
     * overwrite the datasets in the filesystem.
     * If one or more datasets cannot be downloaded, exit with a return code of 2.
     */
    /* TODO */

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
