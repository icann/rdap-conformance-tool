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
import org.icann.rdapconformance.validator.ToolResult;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.*;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot3_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot5Dot1_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot5Dot2_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity.ResponseValidationRegistrantHandle_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.*;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver.ResponseValidation4Dot1Handle_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard.*;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard.ResponseValidationTechEmail_2024;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot5_2024;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation3Dot3And3Dot4_2024;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.TigValidation3Dot2_2024;
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
    private final QueryContext queryContext;


    /**
     * Creates a new RDAPValidator with the complete QueryContext.
     *
     * @param queryContext the QueryContext containing all configuration, services, and state
     */
    public RDAPValidator(QueryContext queryContext) {
        this.queryContext = queryContext;

        if (!queryContext.getConfig().check()) {
            logger.error("Please fix the configuration");
            throw new RuntimeException("Please fix the configuration");
        }

    }

    @Override
    public int validate() {
        SchemaValidator validator = null;
        Map<RDAPQueryType, String> schemaMap = getDomainMap();

        // create this here so we don't call it multiple times
        RDAPQueryType queryType = queryContext.getQueryType();

        // if the query can't run, return the error code
        if (!queryContext.getQuery().run()) {
            if (queryContext.getQuery().getErrorStatus() == null || queryContext.getQuery().getErrorStatus() == ToolResult.SUCCESS) {
                return ToolResult.SUCCESS.getCode();
            }
            ConformanceError errorCode = queryContext.getQuery().getErrorStatus();
            return errorCode.getCode();
        }

        // check the query type and log their errors to the results file if they have any
        queryContext.getQuery().validateStructureByQueryType(queryType);

        if (queryContext.getQuery().isErrorContent()) {
            // if they return a 404 then we need a schema validator that checks the error response content itself
            validator = SchemaValidatorCache.getCachedValidator("rdap_error.json", queryContext.getResults(), queryContext.getDatasetService(), queryContext);
        } else {
            // else we check the schema of the data pertaining to the query type
            String schemaFile = schemaMap.get(queryType);
            if (schemaFile != null) {
                if (RDAPQueryType.ENTITY.equals(queryType) && queryContext.getConfig().isThin()) {
                    logger.error("Thin flag is set while validating entity");
                    return ToolResult.USES_THIN_MODEL.getCode();
                }
                validator = SchemaValidatorCache.getCachedValidator(schemaFile, queryContext.getResults(), queryContext.getDatasetService(), queryContext);
            }
        }

        // verify that the above hasn't failed and sent us a null validator.
        if( validator == null) {
            logger.error("Validator is null, this should not happen -  please check the configuration [{}] and the query type: {}", queryContext.getConfig().getUri(), queryType);
            return ToolResult.UNSUPPORTED_QUERY.getCode();
        }

        // else we continue with the validation process and get the data from the query
        String rdapResponseData = queryContext.getQuery().getData();

        // Store response data in QueryContext for other components to access
        queryContext.setRdapResponseData(rdapResponseData);

        // Get the HTTP response BEFORE schema validation to provide status code context to ExceptionParser
        SimpleHttpResponse rdapResponse = (SimpleHttpResponse) queryContext.getQuery().getRawResponse();
        if(rdapResponse != null) {
            logger.debug("[Raw Response HTTP Code: {} TrackingId: {}",  rdapResponse.statusCode(), rdapResponse.getTrackingId());
            // Set HTTP response in QueryContext for schema validation error reporting
            queryContext.setCurrentHttpResponse(rdapResponse);
        }

        // otherwise, validate the JSON and get the rdapResponse
        validator.validate(rdapResponseData);

        // fold the name stuff and send out another query to that URL
        if (rdapResponse != null && !queryContext.getQuery().isErrorContent() && queryContext.getConfig().isNetworkEnabled()) {
            new DomainCaseFoldingValidation(rdapResponse, queryContext, queryType).validate(); // Network calls
        }

        // Issue additional queries (/help and /not-a-domain.invalid) when flag is true regardless of profile
        if(queryContext.getConfig().isAdditionalConformanceQueries()) {
            logger.info("Validations for additional conformance queries");

            // Sequential execution (always)
            new ResponseValidationHelp_2024(queryContext).validate();  // Network calls
            new ResponseValidationDomainInvalid_2024(queryContext).validate(); // Network calls
        }

        // get all the 2019 profile validations and run them
        if (queryContext.getConfig().useRdapProfileFeb2019()) {
            logger.info("Validations for 2019 profile");
            RDAPProfile rdapProfile = new RDAPProfile(
                get2019RdapValidations(rdapResponse, validator));
            rdapProfile.validate();
        }

        // get all the 2024 profile validations and run them
        if (queryContext.getConfig().useRdapProfileFeb2024()) {
            logger.info("Validations for 2024 profile");
            RDAPProfile rdapProfile = new RDAPProfile(
                get2024ProfileValidations(rdapResponse));
            rdapProfile.validate();
        }

        // Log URI, IP address, and redirects
        String ipAddress = queryContext.getNetworkInfo().getServerIpAddressValue();
        List<URI> redirects = (queryContext.getQuery() instanceof RDAPHttpQuery httpQuery) ? httpQuery.getRedirects() : List.of();


            // Nice to have info
            logger.info("URI used for the query: {}", queryContext.getConfig().getUri());
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
        return queryContext.getResultFile().getResultsPath();
    }

    public QueryContext getQueryContext() {
        return queryContext;
    }


    private List<ProfileValidation> get2024ProfileValidations(HttpResponse<String> rdapResponse) {
        // Set current HTTP response in QueryContext for validations that need it (like TigValidation1Dot2)
        queryContext.setCurrentHttpResponse(rdapResponse);

        // Extract commonly used values from queryContext for convenience
        RDAPValidatorConfiguration config = queryContext.getConfig();
        List<ProfileValidation> validations = new ArrayList<>();

        // All validations in original order - exactly like master branch
        // From 2019 profile validations
        validations.add(new TigValidation3Dot2(queryContext));
        validations.add(new TigValidation4Dot1(queryContext));
        validations.add(new TigValidation7Dot1And7Dot2(queryContext));
        validations.add(new ResponseValidation1Dot2Dot2(queryContext));
        validations.add(new ResponseValidation1Dot4(queryContext));
        validations.add(new ResponseValidationLastUpdateEvent(queryContext));
        validations.add(new ResponseValidation2Dot1(queryContext));
        validations.add(new ResponseValidation2Dot3Dot1Dot1(queryContext));
         // Only run this validation if it's a gTLD registry
        if(config.isGtldRegistry()) {
            validations.add(new ResponseValidation2Dot3Dot1Dot2(queryContext));
        }
        // Only add the validation if it's a gTLD registrar
        if (config.isGtldRegistrar()) {
            validations.add(new ResponseValidation2Dot3Dot2_2024(queryContext));
        }
        validations.add(new ResponseValidation2Dot10(queryContext));
        validations.add(new ResponseValidationRFC5731(queryContext));
        validations.add(new ResponseValidationRFC3915(queryContext));
        validations.add(new ResponseValidation2Dot6Dot1(queryContext));
        validations.add(new ResponseValidation2Dot9Dot1And2Dot9Dot2(queryContext));
        validations.add(new ResponseValidation2Dot4Dot1(queryContext));
        validations.add(new ResponseValidation2Dot4Dot2And2Dot4Dot3(queryContext));
        validations.add(new ResponseValidation2Dot4Dot5(queryContext));
        validations.add(new ResponseNameserverStatusValidation(queryContext));
        validations.add(new ResponseValidation4Dot1Handle(queryContext));
        validations.add(new ResponseValidation4Dot1Query(queryContext));
        validations.add(new ResponseValidation4Dot3(queryContext));

        // 2024 specific validations
        validations.add(new TigValidation1Dot3_2024(queryContext));
        validations.add(new ResponseValidation1Dot2_1_2024(queryContext));
        validations.add(new ResponseValidation1Dot2_2_2024(queryContext));
        validations.add(new ResponseValidation2Dot2_2024(queryContext));
        validations.add(new ResponseValidation2Dot2_1_2024(queryContext));
        validations.add(new ResponseValidation2Dot4Dot6_2024(queryContext));
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024(queryContext));
        validations.add(new ResponseValidation2Dot7Dot3_2024(queryContext));
        validations.add(new ResponseValidation2Dot7Dot5Dot1_2024(queryContext));
        validations.add(new ResponseValidation2Dot7Dot5Dot2_2024(queryContext));
        validations.add(new ResponseValidation2Dot7Dot5Dot3_2024(queryContext));
        validations.add(new ResponseValidation2Dot7Dot6Dot2_2024(queryContext));
        validations.add(new ResponseValidation2Dot7Dot6Dot3_2024(queryContext));
        validations.add(new ResponseValidationTechEmail_2024(queryContext));
        validations.add(new ResponseValidation2Dot9Dot1And2Dot9Dot2_2024(queryContext));
        validations.add(new ResponseValidation4Dot1Handle_2024(queryContext));
        validations.add(new ResponseValidationRegistrantHandle_2024(queryContext));
        validations.add(new ResponseValidationLinkElements_2024(queryContext));
        validations.add(new ResponseValidation2Dot7Dot2_2024(queryContext));
        validations.add(new ResponseValidation2Dot7Dot4Dot1_2024(queryContext));
        validations.add(new ResponseValidation2Dot7Dot4Dot2_2024(queryContext));
        validations.add(new ResponseValidation2Dot7Dot4Dot3_2024(queryContext));
        validations.add(new ResponseValidation2Dot7Dot4Dot4_2024(queryContext));
        validations.add(new ResponseValidation2Dot7Dot4Dot6_2024(queryContext));
        validations.add(new ResponseValidation2Dot7Dot4Dot8_2024(queryContext));
        validations.add(new ResponseValidation2Dot7Dot4Dot9_2024(queryContext));
        validations.add(new ResponseValidationRegistrantEmail_2024(queryContext));
        validations.add(new ResponseValidationStatusDuplication_2024(queryContext));
        validations.add(new ResponseValidation2Dot7Dot6Dot1_2024(queryContext));
        validations.add(new StdRdapConformanceValidation_2024(queryContext));
        validations.add(new TigValidation3Dot2_2024(queryContext));
        validations.add(new TigValidation3Dot3And3Dot4_2024(queryContext));
        validations.add(new ResponseValidation2Dot6Dot3_2024(queryContext));
        validations.add(new ResponseValidation2Dot10_2024(queryContext));

        // Network-dependent validations
        if (config.isNetworkEnabled()) {
            validations.add(new TigValidation1Dot6(queryContext)); // HTTP head request
            validations.add(new TigValidation1Dot13(queryContext)); // reads HTTP headers
            validations.add(new TigValidation1Dot2(queryContext)); // SSL Network connection
            validations.add(new TigValidation1Dot8(queryContext)); // DNS queries
            validations.add(new TigValidation1Dot11Dot1(queryContext)); // URL-based validation
            validations.add(new TigValidation1Dot5_2024(queryContext)); // SSL Network connection
            validations.add(new ResponseValidationTestInvalidRedirect_2024(queryContext)); // Network connection
        }

        return validations;
    }


    private List<ProfileValidation> get2019RdapValidations(HttpResponse<String> rdapResponse, SchemaValidator validator) {
        // Extract commonly used values from queryContext for convenience
        RDAPValidatorConfiguration config = queryContext.getConfig();
        RDAPValidatorResults results = queryContext.getResults();
        RDAPDatasetService datasetService = queryContext.getDatasetService();
        RDAPQueryType queryType = queryContext.getQueryType();
        String rdapResponseData = queryContext.getRdapResponseData();
        List<ProfileValidation> validations = new ArrayList<>();

        // All validations in original order - exactly like master branch
        validations.add(new TigValidation1Dot14(queryContext));
        validations.add(new TigValidation3Dot2(queryContext));
        validations.add(new TigValidation3Dot3And3Dot4(queryContext));
        validations.add(new TigValidation4Dot1(queryContext));
        validations.add(new TigValidation7Dot1And7Dot2(queryContext));
        validations.add(new ResponseValidation1Dot2Dot2(queryContext));
        validations.add(new ResponseValidation1Dot3(rdapResponseData, results));
        validations.add(new ResponseValidation1Dot4(queryContext));
        validations.add(new ResponseValidationLastUpdateEvent(queryContext));
        validations.add(new ResponseValidation2Dot1(queryContext));
        validations.add(new ResponseValidation2Dot2(config, rdapResponseData, results, datasetService, queryType));
        validations.add(new ResponseValidation2Dot3Dot1Dot1(queryContext));

        // Only run this validation if it's a gTLD registry
        if(config.isGtldRegistry()) {
            validations.add(new ResponseValidation2Dot3Dot1Dot2(queryContext));
        }

        validations.add(new ResponseValidationNoticesIncluded(queryContext));
        validations.add(new ResponseValidation2Dot6Dot3(queryContext));
        validations.add(new ResponseValidation2Dot11(queryContext));
        validations.add(new ResponseValidation2Dot10(queryContext));
        validations.add(new ResponseValidationRFC5731(queryContext));
        validations.add(new ResponseValidationRFC3915(queryContext));
        validations.add(new ResponseValidation2Dot6Dot1(queryContext));
        validations.add(new ResponseValidation2Dot9Dot1And2Dot9Dot2(queryContext));
        validations.add(new ResponseValidation2Dot4Dot1(queryContext));
        validations.add(new ResponseValidation2Dot4Dot2And2Dot4Dot3(queryContext));
        validations.add(new ResponseValidation2Dot4Dot5(queryContext));
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated1(queryContext));
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated2(queryContext));
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated3And4(queryContext));
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated5(queryContext));
        validations.add(new ResponseValidation2Dot7Dot1DotXAndRelated6(queryContext));
        validations.add(new ResponseValidation2Dot7Dot5Dot2(queryContext));
        validations.add(new ResponseValidation2Dot7Dot5Dot3(queryContext));
        validations.add(new ResponseValidation3Dot1(queryContext));
        validations.add(new ResponseValidation3Dot2(queryContext));
        validations.add(new ResponseNameserverStatusValidation(queryContext));
        validations.add(new ResponseValidation4Dot1Handle(queryContext));
        validations.add(new ResponseValidation4Dot1Query(queryContext));
        validations.add(new ResponseValidation4Dot3(queryContext));

        // Network-dependent validations
        if (config.isNetworkEnabled()) {
            validations.add(new TigValidation1Dot3(queryContext)); // SSL context
            validations.add(new TigValidation1Dot6(queryContext)); // HTTP head request
            validations.add(new TigValidation1Dot13(queryContext)); // reads HTTP headers
            validations.add(new TigValidation1Dot2(queryContext)); // SSL Network connection
            validations.add(new TigValidation1Dot8(queryContext)); // DNS queries
            validations.add(new TigValidation1Dot11Dot1(queryContext)); // URL-based validation
        }

        return validations;
    }
}
