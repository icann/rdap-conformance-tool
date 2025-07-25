package org.icann.rdapconformance.validator.workflow.rdap;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot5Dot3_2024;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icann.rdapconformance.validator.ConformanceError;
import org.icann.rdapconformance.validator.NetworkInfo;
import org.icann.rdapconformance.validator.ToolResult;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.*;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot3_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot5Dot1_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot5Dot2_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity.ResponseValidationRegistrantHandle_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.*;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver.ResponseValidation4Dot1Handle_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard.*;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot5_2024;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation3Dot3And3Dot4_2024;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.TigValidation3Dot2_2024;
import org.icann.rdapconformance.validator.workflow.rdap.file.RDAPFileQueryTypeProcessor;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQueryTypeProcessor;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest.SimpleHttpResponse;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.workflow.SchemaValidatorCache;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.DomainCaseFoldingValidation;
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
    public RDAPQueryTypeProcessor queryTypeProcessor;
    private final RDAPQuery query;
    private final RDAPValidatorResults results;
    private static RDAPDatasetService datasetService;

    public RDAPValidator(
                         RDAPValidatorConfiguration config,
                         RDAPQuery query,
                         RDAPDatasetService datasetService) {
        this.config = config;
        this.query = query;
        if (!this.config.check()) {
            logger.error("Please fix the configuration");
            throw new RuntimeException("Please fix the configuration");
        }

        // TODO: find a cleaner way to do this.
        if(config.isNetworkEnabled()) {
            this.queryTypeProcessor = RDAPHttpQueryTypeProcessor.getInstance();
        } else {
            this.queryTypeProcessor = RDAPFileQueryTypeProcessor.getInstance(config);
        }

        this.results = RDAPValidatorResultsImpl.getInstance();
        RDAPValidator.datasetService = datasetService;
    }

    @Override
    public int validate() {
        SchemaValidator validator = null;
        Map<RDAPQueryType, String> schemaMap = getDomainMap();

        // create this here so we don't call it multiple times
        RDAPQueryType queryType  = queryTypeProcessor.getQueryType();

        // if the query can't run, return the error code
        if (!query.run()) {
            if (query.getErrorStatus() == null || query.getErrorStatus() == ToolResult.SUCCESS) {
                return ToolResult.SUCCESS.getCode();
            }
           ConformanceError errorCode =  query.getErrorStatus();
            return errorCode.getCode();
        }

        // check the query type and log their errors to the results file if they have any
        query.validateStructureByQueryType(queryType);

        if (query.isErrorContent()) {
            // if they return a 404 then we need a schema validator that checks the error response content itself
            validator = SchemaValidatorCache.getCachedValidator("rdap_error.json", results, datasetService);
        } else {
            // else we check the schema of the data pertaining to the query type
            String schemaFile = schemaMap.get(queryType);
            if (schemaFile != null) {
                   if (RDAPQueryType.ENTITY.equals(queryType) && config.isThin()) {
                    logger.error("Thin flag is set while validating entity");
                    return ToolResult.USES_THIN_MODEL.getCode();
                   }
                validator = SchemaValidatorCache.getCachedValidator(schemaFile, results, datasetService);
            }
        }

        // verify that the above hasn't failed and sent us a null validator.
        if( validator == null) {
            logger.error("Validator is null, this should not happen -  please check the configuration [{}] and the query type: {}", config.getUri(), queryType);
            return ToolResult.UNSUPPORTED_QUERY.getCode();
        }

        // else we continue with the validation process and get the data from the query
        String rdapResponseData = query.getData();

        // otherwise, validate the JSON and get the rdapResponse
        validator.validate(rdapResponseData);
        SimpleHttpResponse rdapResponse = (SimpleHttpResponse ) query.getRawResponse();
        if(rdapResponse != null) {
            logger.info("[Raw Response HTTP Code: {} TrackingId: {}",  rdapResponse.statusCode(), rdapResponse.getTrackingId());
        }

        // fold the name stuff and send out another query to that URL
        if (rdapResponse != null && !query.isErrorContent() && config.isNetworkEnabled()) {
            new DomainCaseFoldingValidation(rdapResponse, config, results, queryType).validate(); // Network calls
        }

        // Issue additional queries (/help and /not-a-domain.invalid) when flag is true and profile 2024 is false
        if(config.isAdditionalConformanceQueries() && !config.useRdapProfileFeb2024()) {
            logger.info("Validations for additional conformance queries");
            
            boolean aggressiveNetworkParallel = "true".equals(System.getProperty("rdap.parallel.network", "false"));
            if (aggressiveNetworkParallel) {
                // Execute additional queries in parallel
                List<ProfileValidation> additionalValidations = List.of(
                    new ResponseValidationHelp_2024(config, results),
                    new ResponseValidationDomainInvalid_2024(config, results)
                );
                org.icann.rdapconformance.validator.workflow.profile.NetworkValidationCoordinator.executeNetworkValidations(additionalValidations);
            } else {
                // Sequential execution (default)
                new ResponseValidationHelp_2024(config, results).validate();  // Network calls
                new ResponseValidationDomainInvalid_2024(config, results).validate(); // Network calls
            }
        }

        // get all the 2019 profile validations and run them
        if (config.useRdapProfileFeb2019()) {
            logger.info("Validations for 2019 profile");
            RDAPProfile rdapProfile = new RDAPProfile(
                get2019RdapValidations(rdapResponse, config, results, datasetService, queryType, validator, rdapResponseData));
            rdapProfile.validate();
        }

        // get all the 2024 profile validations and run them
        if (config.useRdapProfileFeb2024()) {
            logger.info("Validations for 2024 profile");
            RDAPProfile rdapProfile = new RDAPProfile(
                get2024ProfileValidations(rdapResponse, config, results, datasetService, queryType, rdapResponseData));
            rdapProfile.validate();
        }

        // Log URI, IP address, and redirects
        String ipAddress = NetworkInfo.getServerIpAddress();
        List<URI> redirects = (query instanceof RDAPHttpQuery httpQuery) ? httpQuery.getRedirects() : List.of();


        // Nice to have info
        logger.info("URI used for the query: {}", config.getUri());
        logger.info("IP Address used: {}", ipAddress);
        logger.info("Redirects followed: {}", redirects);

        // if we made it this far without an error, return success
        return ToolResult.SUCCESS.getCode();
    }

    public static Map<RDAPQueryType, String> getDomainMap() {
        return Map.of(
            RDAPQueryType.DOMAIN, "rdap_domain.json",
            RDAPQueryType.HELP, "rdap_help.json",
            RDAPQueryType.NAMESERVER, "rdap_nameserver.json",
            RDAPQueryType.NAMESERVERS, "rdap_nameservers.json",
            RDAPQueryType.ENTITY, "rdap_entity_without_asEventActor.json",
            RDAPQueryType.AUTNUM, "rdap_autnum.json",
            RDAPQueryType.IP_NETWORK, "rdap_ip_network.json"
        );
    }

    // the Front-End needs this to get the results path
    @Override
    public String getResultsPath() {
        return RDAPValidationResultFile.getInstance().getResultsPath();
    }


    private List<ProfileValidation> get2024ProfileValidations(HttpResponse<String> rdapResponse,
                                                              RDAPValidatorConfiguration config,
                                                              RDAPValidatorResults results,
                                                              RDAPDatasetService datasetService,
                                                              RDAPQueryType queryType,
                                                              String rdapResponseData) {
        List<ProfileValidation> validations = new ArrayList<>();

        // All validations in original order - exactly like master branch
        // From 2019 profile validations
        validations.add(new TigValidation3Dot2(rdapResponseData, results, config, queryType));
        validations.add(new TigValidation4Dot1(rdapResponseData, results));
        validations.add(new TigValidation7Dot1And7Dot2(rdapResponseData, results));
        validations.add(new ResponseValidation1Dot2Dot2(rdapResponseData, results));
        validations.add(new ResponseValidation1Dot4(rdapResponseData, results));
        validations.add(new ResponseValidationLastUpdateEvent(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation2Dot1(rdapResponseData, results, config, queryType));
        validations.add(new ResponseValidation2Dot3Dot1Dot1(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation2Dot3Dot1Dot2(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation2Dot10(rdapResponseData, results, queryType));
        validations.add(new ResponseValidationRFC5731(rdapResponseData, results, queryType));
        validations.add(new ResponseValidationRFC3915(rdapResponseData, results,queryType));
        validations.add(new ResponseValidation2Dot6Dot1(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation2Dot9Dot1And2Dot9Dot2(config, rdapResponseData, results, datasetService, queryType));
        validations.add(new ResponseValidation2Dot4Dot1(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation2Dot4Dot2And2Dot4Dot3(rdapResponseData, results, datasetService, queryType));
        validations.add(new ResponseValidation2Dot4Dot5(rdapResponseData, results, queryType));
        validations.add(new ResponseNameserverStatusValidation(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation4Dot1Handle(config, rdapResponseData, results, datasetService, queryType));
        validations.add(new ResponseValidation4Dot1Query(rdapResponseData, results, config, queryType));
        validations.add(new ResponseValidation4Dot3(rdapResponseData, results, datasetService, queryType));

        // 2024 specific validations
        validations.add(new TigValidation1Dot3_2024(rdapResponseData, results));
        validations.add(new ResponseValidation1Dot2_1_2024(rdapResponseData, results));
        validations.add(new ResponseValidation1Dot2_2_2024(rdapResponseData, results));
        validations.add(new ResponseValidation2Dot2_2024(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation2Dot2_1_2024(rdapResponseData, results, datasetService));
        validations.add(new ResponseValidation2Dot4Dot6_2024(rdapResponseData, results, datasetService,queryType, config));
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024(rdapResponseData, results, queryType, config));
        validations.add(new ResponseValidation2Dot7Dot3_2024(config, rdapResponseData, results, datasetService, queryType));
        validations.add(new ResponseValidation2Dot7Dot5Dot1_2024(rdapResponseData, results));
        validations.add(new ResponseValidation2Dot7Dot5Dot2_2024(rdapResponseData, results));
        validations.add(new ResponseValidation2Dot7Dot5Dot3_2024(rdapResponseData, results));
        validations.add(new ResponseValidation2Dot7Dot6Dot2_2024(rdapResponseData, results));
        validations.add(new ResponseValidation2Dot7Dot6Dot3_2024(rdapResponseData, results));
        validations.add(new ResponseValidation2Dot9Dot1And2Dot9Dot2_2024(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation4Dot1Handle_2024(rdapResponseData, results, queryType));
        validations.add(new ResponseValidationRegistrantHandle_2024(rdapResponseData, results, datasetService));
        validations.add(new ResponseValidationLinkElements_2024(rdapResponseData, results));
        validations.add(new ResponseValidation2Dot7Dot2_2024(config, queryType, rdapResponseData, results));
        validations.add(new ResponseValidation2Dot7Dot4Dot1_2024(rdapResponseData, results));
        validations.add(new ResponseValidation2Dot7Dot4Dot2_2024(rdapResponseData, results));
        validations.add(new ResponseValidation2Dot7Dot4Dot3_2024(rdapResponseData, results));
        validations.add(new ResponseValidation2Dot7Dot4Dot4_2024(rdapResponseData, results));
        validations.add(new ResponseValidation2Dot7Dot4Dot6_2024(rdapResponseData, results));
        validations.add(new ResponseValidation2Dot7Dot4Dot8_2024(rdapResponseData, results));
        validations.add(new ResponseValidation2Dot7Dot4Dot9_2024(rdapResponseData, results));
        validations.add(new ResponseValidationStatusDuplication_2024(rdapResponseData, results));
        validations.add(new ResponseValidation2Dot7Dot6Dot1_2024(rdapResponseData, results));
        validations.add(new StdRdapConformanceValidation_2024(rdapResponseData, results));
        validations.add(new TigValidation3Dot2_2024(rdapResponseData, results, config, queryType));
        validations.add(new TigValidation3Dot3And3Dot4_2024(rdapResponseData, results, config));
        validations.add(new ResponseValidation2Dot6Dot3_2024(rdapResponseData, results, config));
        validations.add(new ResponseValidation2Dot10_2024(rdapResponseData, results));

        // Network-dependent validations
        if (config.isNetworkEnabled()) {
            validations.add(new TigValidation1Dot6(rdapResponse.statusCode(), config, results)); // HTTP head request
            validations.add(new TigValidation1Dot13(rdapResponse, results)); // reads HTTP headers
            validations.add(new TigValidation1Dot2(rdapResponse, config, results)); // SSL Network connection
            validations.add(new TigValidation1Dot8(rdapResponse, results, datasetService, config)); // DNS queries
            validations.add(new TigValidation1Dot11Dot1(config, results, datasetService, queryType)); // URL-based validation
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
                                                           RDAPQueryType queryType,
                                                           SchemaValidator validator,
                                                           String rdapResponseData) {
        List<ProfileValidation> validations = new ArrayList<>();

        // All validations in original order - exactly like master branch
        validations.add(new TigValidation1Dot14(rdapResponseData, results));
        validations.add(new TigValidation3Dot2(rdapResponseData, results, config, queryType));
        validations.add(new TigValidation3Dot3And3Dot4(rdapResponseData, results, validator));
        validations.add(new TigValidation4Dot1(rdapResponseData, results));
        validations.add(new TigValidation7Dot1And7Dot2(rdapResponseData, results));
        validations.add(new ResponseValidation1Dot2Dot2(rdapResponseData, results));
        validations.add(new ResponseValidation1Dot3(rdapResponseData, results));
        validations.add(new ResponseValidation1Dot4(rdapResponseData, results));
        validations.add(new ResponseValidationLastUpdateEvent(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation2Dot1(rdapResponseData, results, config, queryType));
        validations.add(new ResponseValidation2Dot2(config, rdapResponseData, results, datasetService, queryType));
        validations.add(new ResponseValidation2Dot3Dot1Dot1(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation2Dot3Dot1Dot2(rdapResponseData, results, queryType));
        validations.add(new ResponseValidationNoticesIncluded(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation2Dot6Dot3(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation2Dot11(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation2Dot10(rdapResponseData, results, queryType));
        validations.add(new ResponseValidationRFC5731(rdapResponseData, results, queryType));
        validations.add(new ResponseValidationRFC3915(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation2Dot6Dot1(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation2Dot9Dot1And2Dot9Dot2(config, rdapResponseData, results, datasetService, queryType));
        validations.add(new ResponseValidation2Dot4Dot1(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation2Dot4Dot2And2Dot4Dot3(rdapResponseData, results, datasetService, queryType));
        validations.add(new ResponseValidation2Dot4Dot5(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated1(rdapResponseData, results, queryType, config));
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated2(rdapResponseData, results, queryType, config));
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated3And4(rdapResponseData, results, queryType, config,
            new SimpleHandleValidation(config, rdapResponseData, results, datasetService, queryType, -52102)));
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated5(rdapResponseData, results, queryType, config));
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated6(rdapResponseData, results, queryType, config));
        validations.add(new ResponseValidation2Dot7Dot5Dot2(rdapResponseData, results, queryType, config));
        validations.add(new ResponseValidation2Dot7Dot5Dot3(rdapResponseData, results, queryType, config));
        validations.add(new ResponseValidation3Dot1(rdapResponseData, results, queryType, config));
        validations.add(new ResponseValidation3Dot2(rdapResponseData, results, queryType, config));
        validations.add(new ResponseNameserverStatusValidation(rdapResponseData, results, queryType));
        validations.add(new ResponseValidation4Dot1Handle(config, rdapResponseData, results, datasetService, queryType));
        validations.add(new ResponseValidation4Dot1Query(rdapResponseData, results, config, queryType));
        validations.add(new ResponseValidation4Dot3(rdapResponseData, results, datasetService, queryType));

        // Network-dependent validations
        if (config.isNetworkEnabled()) {
            validations.add(new TigValidation1Dot3(rdapResponse, config, results)); // SSL context
            validations.add(new TigValidation1Dot6(rdapResponse.statusCode(), config, results)); // HTTP head request
            validations.add(new TigValidation1Dot13(rdapResponse, results)); // reads HTTP headers
            validations.add(new TigValidation1Dot2(rdapResponse, config, results)); // SSL Network connection
            validations.add(new TigValidation1Dot8(rdapResponse, results, datasetService, config)); // DNS queries
            validations.add(new TigValidation1Dot11Dot1(config, results, datasetService, queryType)); // URL-based validation
        }

        return validations;
    }
}
