package org.icann.rdapconformance.validator.workflow.rdap;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.List;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParser;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.ValidatorWorkflow;
import org.icann.rdapconformance.validator.workflow.profile.RDAPProfileFebruary2019;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot13;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot14;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot2;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot3;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot6;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot8;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation3Dot3And3Dot4;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation4Dot1;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation7Dot1And7Dot2;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.Validation1Dot11Dot1;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.Validation3Dot2;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.Validation6Dot1;
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
        new ConfigurationFileParser(),
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
    try (InputStream is = fileSystem.uriToStream(this.config.getConfigurationFile())) {
      configurationFile = configParser.parse(is);
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

    if (!queryTypeProcessor.check(datasetService)) {
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
      validator = new SchemaValidator("rdap_error.json", results, datasetService);
    } else if (RDAPQueryType.DOMAIN.equals(queryTypeProcessor.getQueryType())) {
      validator = new SchemaValidator("rdap_domain.json", results, datasetService);
    } else if (RDAPQueryType.HELP.equals(queryTypeProcessor.getQueryType())) {
      validator = new SchemaValidator("rdap_help.json", results, datasetService);
    } else if (RDAPQueryType.NAMESERVER.equals(queryTypeProcessor.getQueryType())) {
      validator = new SchemaValidator("rdap_nameserver.json", results, datasetService);
    } else if (RDAPQueryType.NAMESERVERS.equals(queryTypeProcessor.getQueryType())) {
      validator = new SchemaValidator("rdap_nameservers.json", results, datasetService);
    } else if (RDAPQueryType.ENTITY.equals(queryTypeProcessor.getQueryType())) {
      if (config.isThin()) {
        logger.error("Thin flag is set while validating entity");
        query.getStatusCode().ifPresent(rdapValidationResultFile::build);
        return RDAPValidationStatus.USES_THIN_MODEL.getValue();
      }
      // asEventActor property is not allow in topMost entity object, see spec 7.2.9.2
      validator = new SchemaValidator("rdap_entity_without_asEventActor.json", results,
          datasetService);
    }
    assert null != validator;
    validator.validate(query.getData());

    /*
     * Additionally, apply the relevant collection tests when the option
     * --use-rdap-profile-february-2019 is set.
     */
    if (config.userRdapProfileFeb2019()) {
      HttpResponse<String> rdapResponse = (HttpResponse<String>) query.getRawResponse();
      RDAPProfileFebruary2019 rdapProfileFebruary2019 = new RDAPProfileFebruary2019(
          List.of(
              new Validation1Dot2(rdapResponse, config, results),
              new Validation1Dot3(rdapResponse, config, results),
              new Validation1Dot6(rdapResponse.statusCode(), config, results),
              new Validation1Dot8(rdapResponse, results, datasetService),
              new Validation1Dot13(rdapResponse, results),
              new Validation1Dot11Dot1(config, results, datasetService,
                  queryTypeProcessor.getQueryType()),
              new Validation1Dot14(query.getData(), datasetService, results),
              new Validation3Dot2(query.getData(), results, config, queryTypeProcessor.getQueryType()),
              new Validation6Dot1(query.getData(), results, queryTypeProcessor.getQueryType()),
              new Validation3Dot3And3Dot4(query.getData(), results, validator),
              new Validation4Dot1(query.getData(), results),
              new Validation7Dot1And7Dot2(query.getData(), results)
          ));
      rdapProfileFebruary2019.validate();
    }

    query.getStatusCode().ifPresent(rdapValidationResultFile::build);

    return RDAPValidationStatus.SUCCESS.getValue();
  }
}
