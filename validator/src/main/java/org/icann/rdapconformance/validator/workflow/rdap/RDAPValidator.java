package org.icann.rdapconformance.validator.workflow.rdap;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.icann.rdapconformance.validator.ConformanceError;
import org.icann.rdapconformance.validator.NetworkInfo;
import org.icann.rdapconformance.validator.ToolResult;

import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.*;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot3_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.*;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver.ResponseValidation4Dot1Handle_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard.*;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot5_2024;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation3Dot3And3Dot4_2024;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.TigValidation3Dot2_2024;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParser;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParserImpl;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.DomainCaseFoldingValidation;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.ValidatorWorkflow;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.profile.RDAPProfile;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot1DotXAndRelated1;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot1DotXAndRelated2;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot1DotXAndRelated3And4;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot1DotXAndRelated5;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot1DotXAndRelated6;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot5Dot2;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot5Dot3;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.SimpleHandleValidation;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity.ResponseValidation3Dot1;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity.ResponseValidation3Dot2;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.miscellaneous.ResponseValidationLastUpdateEvent;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver.ResponseNameserverStatusValidation;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver.ResponseValidation4Dot1Handle;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver.ResponseValidation4Dot1Query;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver.ResponseValidation4Dot3;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot13;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot14;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot2;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot3;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot3_2024;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot6;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot8;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation3Dot3And3Dot4;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation4Dot1;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation7Dot1And7Dot2;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.TigValidation1Dot11Dot1;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.TigValidation3Dot2;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQuery;

public class RDAPValidator implements ValidatorWorkflow {

    private static final Logger logger = LoggerFactory.getLogger(RDAPValidator.class);
    private final RDAPValidatorConfiguration config;
    private final RDAPQueryTypeProcessor queryTypeProcessor;
    private final RDAPQuery query;
    private final FileSystem fileSystem;
    private final ConfigurationFileParser configParser;
    private final RDAPValidatorResults results;
    private static RDAPDatasetService datasetService;

    public RDAPValidator(RDAPValidatorConfiguration config,
                         FileSystem fileSystem,
                         RDAPQueryTypeProcessor queryTypeProcessor,
                         RDAPQuery query) {
        this(config, fileSystem, queryTypeProcessor, query, new ConfigurationFileParserImpl(),
            RDAPValidatorResultsImpl.getInstance(),  RDAPDatasetServiceImpl.getInstance());
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
        RDAPValidator.datasetService = datasetService;
    }

    @Override
    public int validate() {
        ConfigurationFile configurationFile;
        SchemaValidator validator = null;
        Map<RDAPQueryType, String> schemaMap = Map.of(
            RDAPQueryType.DOMAIN, "rdap_domain.json",
            RDAPQueryType.HELP, "rdap_help.json",
            RDAPQueryType.NAMESERVER, "rdap_nameserver.json",
            RDAPQueryType.NAMESERVERS, "rdap_nameservers.json",
            RDAPQueryType.ENTITY, "rdap_entity_without_asEventActor.json",
            RDAPQueryType.AUTNUM, "rdap_autnum.json",
            RDAPQueryType.IP_NETWORK, "rdap_ip_network.json"
        );

        try (InputStream is = fileSystem.uriToStream(this.config.getConfigurationFile())) {
            configurationFile = configParser.parse(is);
        } catch (Exception e) {
            logger.error("Configuration is invalid", e);
            return ToolResult.CONFIG_INVALID.getCode();
        }

        RDAPValidationResultFile rdapValidationResultFile = RDAPValidationResultFile.getInstance();
        rdapValidationResultFile.initialize(results, config, configurationFile, fileSystem);

        if (!queryTypeProcessor.check(datasetService)) {
            System.out.println("We failed checking the query type: " + queryTypeProcessor.getErrorStatus());
            return  queryTypeProcessor.getErrorStatus().getCode();
        }

//        RDAPQueryType queryType  = queryTypeProcessor.getQueryType();

        query.setResults(results);
        if (!query.run()) {
            if (query.getErrorStatus() == null || query.getErrorStatus() == ToolResult.SUCCESS) {
                return ToolResult.SUCCESS.getCode();
            }
           ConformanceError errorCode =  query.getErrorStatus();
            return errorCode.getCode();
        }

        query.checkWithQueryType(queryTypeProcessor.getQueryType());

        if (query.isErrorContent()) {
            validator = new SchemaValidator("rdap_error.json", results, datasetService);
        } else {
            String schemaFile = schemaMap.get(queryTypeProcessor.getQueryType());
            if (schemaFile != null) {
                if (RDAPQueryType.ENTITY.equals(queryTypeProcessor.getQueryType()) && config.isThin()) {
                    logger.error("Thin flag is set while validating entity");
                    return ToolResult.USES_THIN_MODEL.getCode();
                }
                // asEventActor property is not allow in topMost entity object, see spec 7.2.9.2
                validator = new SchemaValidator(schemaFile, results, datasetService);
            }
        }

        assert null != validator;
        validator.validate(query.getData()); // validates the JSON
        RDAPHttpRequest.SimpleHttpResponse rdapResponse = (RDAPHttpRequest.SimpleHttpResponse ) query.getRawResponse();

        if(rdapResponse != null) {
            logger.info("[Raw Response HTTP Code: {} TrackingId: {}",  rdapResponse.statusCode(), rdapResponse.getTrackingId());
        }


        if (rdapResponse != null && !query.isErrorContent() && config.isNetworkEnabled()) {
            new DomainCaseFoldingValidation(rdapResponse, config, results, queryTypeProcessor.getQueryType()).validate(); // Network calls
        }

        // Issue additional queries (/help and /not-a-domain.invalid) when flag is true and profile 2024 is false
        if(config.isAdditionalConformanceQueries() && !config.useRdapProfileFeb2024()) {
            logger.info("Validations for additional conformance queries");
            new ResponseValidationHelp_2024(config, results).validate();
            new ResponseValidationDomainInvalid_2024(config, results).validate();
        }

        if (config.useRdapProfileFeb2019()) {
            logger.info("Validations for 2019 profile");
            RDAPProfile rdapProfile = new RDAPProfile(
                get2019RdapValidations(rdapResponse, config, results, datasetService, queryTypeProcessor, validator, query));
            rdapProfile.validate();
        }

        if (config.useRdapProfileFeb2024()) {
            logger.info("Validations for 2024 profile");
            RDAPProfile rdapProfile = new RDAPProfile(
                get2024ProfileValidations(rdapResponse, config, results, datasetService, queryTypeProcessor, query));
            rdapProfile.validate();
        }

        // Log URI, IP address, and redirects
        String ipAddress = NetworkInfo.getServerIpAddress();
        List<URI> redirects = (query instanceof RDAPHttpQuery httpQuery) ? httpQuery.getRedirects() : List.of();


        logger.info("URI used for the query: {}", config.getUri());
        logger.info("IP Address used: {}", ipAddress);
        logger.info("Redirects followed: {}", redirects);

        return ToolResult.SUCCESS.getCode();
    }

    @Override
    public RDAPValidatorResults getResults() {
        return null;
    }

    @Override
    public String getResultsPath() {
        return RDAPValidationResultFile.getInstance().getResultsPath();
    }

    private List<ProfileValidation> get2024ProfileValidations(HttpResponse<String> rdapResponse,
                                                              RDAPValidatorConfiguration config,
                                                              RDAPValidatorResults results,
                                                              RDAPDatasetService datasetService,
                                                              RDAPQueryTypeProcessor queryTypeProcessor,
                                                              RDAPQuery query) {
        List<ProfileValidation> validations = new ArrayList<>();

        // below are from 2019 profile validations
        // Add validations that do not require network connections
        validations.add(new TigValidation3Dot2(query.getData(), results, config, queryTypeProcessor.getQueryType())); // clean
        validations.add(new TigValidation4Dot1(query.getData(), results)); // clean
        validations.add(new TigValidation7Dot1And7Dot2(query.getData(), results)); // clean
        validations.add(new ResponseValidation1Dot2Dot2(query.getData(), results)); // clean
        validations.add(new ResponseValidation1Dot4(query.getData(), results)); // clean
        validations.add(new ResponseValidationLastUpdateEvent(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot1(query.getData(), results, config, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot3Dot1Dot1(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot3Dot1Dot2(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot10(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidationRFC5731(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidationRFC3915(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot6Dot1(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot9Dot1And2Dot9Dot2(config, query.getData(), results, datasetService, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot4Dot1(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot4Dot2And2Dot4Dot3(query.getData(), results, datasetService, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot4Dot5(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseNameserverStatusValidation(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation4Dot1Handle(config, query.getData(), results, datasetService, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation4Dot1Query(query.getData(), results, config, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation4Dot3(query.getData(), results, datasetService, queryTypeProcessor.getQueryType())); // clean

        // Add validations that require network connections if the flag is enabled
        if (config.isNetworkEnabled()) {
            logger.info("Network enabled tests");
            validations.add(new TigValidation1Dot6(rdapResponse.statusCode(), config, results)); // http head request
            validations.add(new TigValidation1Dot13(rdapResponse, results)); // reads HTTP headers
            validations.add(new TigValidation1Dot2(rdapResponse, config, results)); // SSL Network connection
            validations.add(new TigValidation1Dot8(rdapResponse, results, datasetService, config)); // DNS queries
            validations.add(new TigValidation1Dot11Dot1(config, results, datasetService, queryTypeProcessor.getQueryType())); // assume you passed in a URL on the cli
        }
        // above are from 2019 validations

        // 2024 validations
        validations.add(new TigValidation1Dot3_2024(query.getData(), results)); // clean
        validations.add(new ResponseValidation1Dot2_1_2024(query.getData(), results)); // clean
        validations.add(new ResponseValidation1Dot2_2_2024(query.getData(), results)); // clean
        validations.add(new ResponseValidation2Dot2_2024(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot2_1_2024(query.getData(), results, datasetService)); // clean
        validations.add(new ResponseValidation2Dot4Dot6_2024(query.getData(), results, datasetService, queryTypeProcessor.getQueryType(), config)); // clean
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024(query.getData(), results, queryTypeProcessor.getQueryType(), config)); // clean
        validations.add(new ResponseValidation2Dot7Dot3_2024(config, query.getData(), results, datasetService, queryTypeProcessor.getQueryType()));
        validations.add(new ResponseValidation2Dot7Dot6Dot2_2024(query.getData(), results)); // clean
        validations.add(new ResponseValidation2Dot7Dot6Dot3_2024(query.getData(), results)); // clean
        validations.add(new ResponseValidation2Dot9Dot1And2Dot9Dot2_2024(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation4Dot1Handle_2024(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidationLinkElements_2024(query.getData(), results)); // clean
        validations.add(new ResponseValidation2Dot7Dot4Dot1_2024(query.getData(), results)); // clean
        validations.add(new ResponseValidation2Dot7Dot4Dot3_2024(query.getData(), results)); // clean
        validations.add(new ResponseValidation2Dot7Dot4Dot4_2024(query.getData(), results)); // clean
        validations.add(new ResponseValidation2Dot7Dot4Dot6_2024(query.getData(), results)); // clean
        validations.add(new ResponseValidation2Dot7Dot4Dot8_2024(config, query.getData(), results)); // clean
        validations.add(new ResponseValidationStatusDuplication_2024(query.getData(), results)); // clean
        validations.add(new StdRdapConformanceValidation_2024(query.getData(), results)); // clean
        validations.add(new TigValidation3Dot2_2024(query.getData(), results, config, queryTypeProcessor.getQueryType())); // clean
        validations.add(new TigValidation3Dot3And3Dot4_2024(query.getData(), results, config)); // clean
        validations.add(new ResponseValidation2Dot6Dot3_2024(query.getData(), results)); //clean
        validations.add(new ResponseValidation2Dot10_2024(query.getData(), results)); // clean

        // Conditionally add validations that require network connections
        if (config.isNetworkEnabled()) {
            logger.info("Network enabled tests for 2024 profile");
            validations.add(new TigValidation1Dot5_2024(rdapResponse, config, results)); // SSL Network connection
            validations.add(new ResponseValidationTestInvalidRedirect_2024(config, results)); // Network connection

            if(!config.isAdditionalConformanceQueries()) {
                validations.add(new ResponseValidationHelp_2024(config, results)); // Network connection
                validations.add(new ResponseValidationDomainInvalid_2024(config, results)); // Network connection
            }
        }

        return validations;
    }

    private List<ProfileValidation> get2019RdapValidations(HttpResponse<String> rdapResponse,
                                                       RDAPValidatorConfiguration config,
                                                       RDAPValidatorResults results,
                                                       RDAPDatasetService datasetService,
                                                       RDAPQueryTypeProcessor queryTypeProcessor,
                                                       SchemaValidator validator,
                                                       RDAPQuery query) {
        List<ProfileValidation> validations = new ArrayList<>();

        // Add validations that do not require network connections
        validations.add(new TigValidation1Dot14(query.getData(), results)); // clean
        validations.add(new TigValidation3Dot2(query.getData(), results, config, queryTypeProcessor.getQueryType())); // clean
        validations.add(new TigValidation3Dot3And3Dot4(query.getData(), results, validator)); // clean
        validations.add(new TigValidation4Dot1(query.getData(), results)); // clean
        validations.add(new TigValidation7Dot1And7Dot2(query.getData(), results)); // clean
        validations.add(new ResponseValidation1Dot2Dot2(query.getData(), results)); // clean
        validations.add(new ResponseValidation1Dot3(query.getData(), results)); // clean
        validations.add(new ResponseValidation1Dot4(query.getData(), results)); // clean
        validations.add(new ResponseValidationLastUpdateEvent(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot1(query.getData(), results, config, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot2(config, query.getData(), results, datasetService, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot3Dot1Dot1(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot3Dot1Dot2(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidationNoticesIncluded(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot6Dot3(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot11(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot10(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidationRFC5731(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidationRFC3915(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot6Dot1(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot9Dot1And2Dot9Dot2(config, query.getData(), results, datasetService, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot4Dot1(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot4Dot2And2Dot4Dot3(query.getData(), results, datasetService, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot4Dot5(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated1(query.getData(), results, queryTypeProcessor.getQueryType(), config)); // clean
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated2(query.getData(), results, queryTypeProcessor.getQueryType(), config)); // clean
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated3And4(query.getData(), results, queryTypeProcessor.getQueryType(), config,
            new SimpleHandleValidation(config, query.getData(), results, datasetService, queryTypeProcessor.getQueryType(), -52102))); // clean
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated5(query.getData(), results, queryTypeProcessor.getQueryType(), config)); // clean
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated6(query.getData(), results, queryTypeProcessor.getQueryType(), config)); // clean
        validations.add(new ResponseValidation2Dot7Dot5Dot2(query.getData(), results, queryTypeProcessor.getQueryType(), config)); // clean
        validations.add(new ResponseValidation2Dot7Dot5Dot3(query.getData(), results, queryTypeProcessor.getQueryType(), config)); // clean
        validations.add(new ResponseValidation3Dot1(query.getData(), results, queryTypeProcessor.getQueryType(), config)); // clean
        validations.add(new ResponseValidation3Dot2(query.getData(), results, queryTypeProcessor.getQueryType(), config)); // clean
        validations.add(new ResponseNameserverStatusValidation(query.getData(), results, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation4Dot1Handle(config, query.getData(), results, datasetService, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation4Dot1Query(query.getData(), results, config, queryTypeProcessor.getQueryType())); // clean
        validations.add(new ResponseValidation4Dot3(query.getData(), results, datasetService, queryTypeProcessor.getQueryType())); // clean

        // Add validations that require network connections if the flag is enabled
        if (config.isNetworkEnabled()) {
            logger.info("Network enabled tests");
            validations.add(new TigValidation1Dot3(rdapResponse, config, results)); // SSL context
            validations.add(new TigValidation1Dot6(rdapResponse.statusCode(), config, results)); // http head request
            validations.add(new TigValidation1Dot13(rdapResponse, results)); // reads HTTP headers
            validations.add(new TigValidation1Dot2(rdapResponse, config, results)); // SSL Network connection
            validations.add(new TigValidation1Dot8(rdapResponse, results, datasetService, config)); // DNS queries
            validations.add(new TigValidation1Dot11Dot1(config, results, datasetService, queryTypeProcessor.getQueryType())); // assume you passed in a URL on the cli
        }

        return validations;
    }
}
