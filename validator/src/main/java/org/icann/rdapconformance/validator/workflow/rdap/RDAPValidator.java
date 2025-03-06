package org.icann.rdapconformance.validator.workflow.rdap;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.List;

import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParser;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParserImpl;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.DomainCaseFoldingValidation;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.ValidatorWorkflow;
import org.icann.rdapconformance.validator.workflow.profile.RDAPProfileFebruary2019;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidation2Dot1;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidation2Dot10;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidation2Dot11;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidation2Dot2;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidation2Dot3Dot1Dot1;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidation2Dot3Dot1Dot2;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidation2Dot4Dot1;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidation2Dot4Dot2And2Dot4Dot3;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidation2Dot4Dot5;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidation2Dot6Dot1;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidation2Dot6Dot3;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidation2Dot9Dot1And2Dot9Dot2;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidationNoticesIncluded;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidationRFC3915;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidationRFC5731;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot1DotXAndRelated1;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot1DotXAndRelated2;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot1DotXAndRelated3And4;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot1DotXAndRelated6;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot5Dot2;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot5Dot3;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.SimpleHandleValidation;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity.ResponseValidation3Dot1;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity.ResponseValidation3Dot2;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.ResponseValidation1Dot2Dot2;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.ResponseValidation1Dot3;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.ResponseValidation1Dot4;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.miscellaneous.ResponseValidationLastUpdateEvent;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver.ResponseNameserverStatusValidation;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver.ResponseValidation4Dot1Handle;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver.ResponseValidation4Dot1Query;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver.ResponseValidation4Dot3;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot13;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot14;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot2;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot3;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot6;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot8;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation3Dot3And3Dot4;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation4Dot1;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation7Dot1And7Dot2;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registrar.TigValidation1Dot12Dot1;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.TigValidation1Dot11Dot1;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.TigValidation3Dot2;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.TigValidation6Dot1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPValidator implements ValidatorWorkflow {

  private static final Logger logger = LoggerFactory.getLogger(RDAPValidator.class);

  private final RDAPValidatorConfiguration config;
  private final RDAPQueryTypeProcessor queryTypeProcessor;
  private final RDAPQuery query;
  private final FileSystem fileSystem;
  private final ConfigurationFileParser configParser;
  private final RDAPValidatorResults results;
  private final RDAPDatasetService datasetService;

  private String resultsPath;

  public RDAPValidator(RDAPValidatorConfiguration config,
      FileSystem fileSystem,
      RDAPQueryTypeProcessor queryTypeProcessor,
      RDAPQuery query) {
    this(config, fileSystem, queryTypeProcessor, query,
        new ConfigurationFileParserImpl(),
        new RDAPValidatorResultsImpl(),
        new RDAPDatasetServiceImpl(fileSystem));
  }

  public RDAPValidator(RDAPValidatorConfiguration config,
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

    query.setResults(results);
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
    HttpResponse<String> rdapResponse = (HttpResponse<String>) query.getRawResponse();

    // extra validations not categorized (change request):
    // query.isErrorContent() added as condition in cases where they have 404 as status code
    if (rdapResponse != null && !query.isErrorContent()) {
      new DomainCaseFoldingValidation(rdapResponse, config, results,
          queryTypeProcessor.getQueryType()).validate();
    }
    
    /*
     * Additionally, apply the relevant collection tests when the option
     * --use-rdap-profile-february-2019 is set.
     * query.isErrorContent() added as condition in cases where they have 404 as status code
     */
    if (config.useRdapProfileFeb2019() && !query.isErrorContent()) {
        RDAPProfileFebruary2019 rdapProfileFebruary2019 = new RDAPProfileFebruary2019(
          List.of(
              new TigValidation1Dot2(rdapResponse, config, results),
              new TigValidation1Dot3(rdapResponse, config, results),
              new TigValidation1Dot6(rdapResponse.statusCode(), config, results),
              new TigValidation1Dot8(rdapResponse, results, datasetService),
              new TigValidation1Dot13(rdapResponse, results),
              new TigValidation1Dot11Dot1(config, results, datasetService,
                  queryTypeProcessor.getQueryType()),
              new TigValidation1Dot14(query.getData(), results),
              new TigValidation3Dot2(query.getData(), results, config,
                  queryTypeProcessor.getQueryType()),
              new TigValidation6Dot1(query.getData(), results, queryTypeProcessor.getQueryType()),
              new TigValidation3Dot3And3Dot4(query.getData(), results, validator),
              new TigValidation4Dot1(query.getData(), results),
              new TigValidation7Dot1And7Dot2(query.getData(), results),
              new TigValidation1Dot12Dot1(query.getData(), results, datasetService,
                  queryTypeProcessor.getQueryType()),
              new ResponseValidation1Dot2Dot2(query.getData(), results),
              new ResponseValidation1Dot3(query.getData(), results),
              new ResponseValidation1Dot4(query.getData(), results),
              new ResponseValidationLastUpdateEvent(query.getData(), results,
                  queryTypeProcessor.getQueryType()),
              new ResponseValidation2Dot1(query.getData(), results, config,
                  queryTypeProcessor.getQueryType()),
              new ResponseValidation2Dot2(query.getData(), results, datasetService,
                  queryTypeProcessor.getQueryType()),
              new ResponseValidation2Dot3Dot1Dot1(query.getData(), results,
                  queryTypeProcessor.getQueryType()),
              new ResponseValidation2Dot3Dot1Dot2(query.getData(), results,
                  queryTypeProcessor.getQueryType()),
              new ResponseValidationNoticesIncluded(query.getData(), results,
                  queryTypeProcessor.getQueryType()),
              new ResponseValidation2Dot6Dot3(query.getData(), results,
                  queryTypeProcessor.getQueryType()),
              new ResponseValidation2Dot11(query.getData(), results,
                  queryTypeProcessor.getQueryType()),
              new ResponseValidation2Dot10(query.getData(), results,
                  queryTypeProcessor.getQueryType()),
              new ResponseValidationRFC5731(query.getData(), results,
                  queryTypeProcessor.getQueryType()),
              new ResponseValidationRFC3915(query.getData(), results,
                  queryTypeProcessor.getQueryType()),
              new ResponseValidation2Dot6Dot1(query.getData(), results,
                  queryTypeProcessor.getQueryType()),
              new ResponseValidation2Dot9Dot1And2Dot9Dot2(query.getData(), results,
                  datasetService, queryTypeProcessor.getQueryType()),
              new ResponseValidation2Dot4Dot1(query.getData(), results,
                  queryTypeProcessor.getQueryType()),
              new ResponseValidation2Dot4Dot2And2Dot4Dot3(query.getData(), results,
                  datasetService, queryTypeProcessor.getQueryType()),
              new ResponseValidation2Dot4Dot5(query.getData(), results,
                  queryTypeProcessor.getQueryType()),
              new ResponseValidation2Dot7Dot1DotXAndRelated1(query.getData(), results,
                  queryTypeProcessor.getQueryType(), config),
              new ResponseValidation2Dot7Dot1DotXAndRelated2(query.getData(), results,
                  queryTypeProcessor.getQueryType(), config),
              new ResponseValidation2Dot7Dot1DotXAndRelated3And4(query.getData(), results,
                  queryTypeProcessor.getQueryType(), config,
                  new SimpleHandleValidation(query.getData(), results, datasetService,
                      queryTypeProcessor.getQueryType(), -52102)),
              new ResponseValidation2Dot7Dot1DotXAndRelated6(query.getData(), results,
                  queryTypeProcessor.getQueryType(), config),
              new ResponseValidation2Dot7Dot5Dot2(query.getData(), results,
                  queryTypeProcessor.getQueryType(), config),
              new ResponseValidation2Dot7Dot5Dot3(query.getData(), results,
                  queryTypeProcessor.getQueryType(), config),
              new ResponseValidation3Dot1(query.getData(), results,
                  queryTypeProcessor.getQueryType(), config),
              new ResponseValidation3Dot2(query.getData(), results,
                  queryTypeProcessor.getQueryType(), config),
              new ResponseNameserverStatusValidation(query.getData(), results,
                  queryTypeProcessor.getQueryType()),
              new ResponseValidation4Dot1Handle(query.getData(), results,
                  datasetService, queryTypeProcessor.getQueryType()),
              new ResponseValidation4Dot1Query(query.getData(), results,
                  config, queryTypeProcessor.getQueryType()),
              new ResponseValidation4Dot3(query.getData(), results,
                  datasetService, queryTypeProcessor.getQueryType())
          ));
      rdapProfileFebruary2019.validate();
    }

    query.getStatusCode().ifPresent(rdapValidationResultFile::build);

    this.resultsPath = rdapValidationResultFile.resultPath;
    return RDAPValidationStatus.SUCCESS.getValue();
  }

  @Override
  public String getResultsPath() {
      return this.resultsPath;
  }
}
