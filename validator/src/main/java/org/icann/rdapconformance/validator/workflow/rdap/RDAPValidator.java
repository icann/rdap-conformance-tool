package org.icann.rdapconformance.validator.workflow.rdap;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.Optional;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.ResponseValidation1Dot2_1_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.ResponseValidation1Dot2_2_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.ResponseValidationLinkElements_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.ResponseValidationStatusDuplication_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.ResponseValidationTestInvalidDomain;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.ResponseValidationTestInvalidRedirect_2024;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.StdRdapConformanceValidation_2024;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot5_2024;
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
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities.ResponseValidation2Dot7Dot1DotXAndRelated5;
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
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot3_2024;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot6;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation1Dot8;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation3Dot3And3Dot4;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation4Dot1;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidation7Dot1And7Dot2;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registrar.TigValidation1Dot12Dot1;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.TigValidation1Dot11Dot1;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.TigValidation3Dot2;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.TigValidation6Dot1;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQuery;

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
        this(config, fileSystem, queryTypeProcessor, query, new ConfigurationFileParserImpl(),
            RDAPValidatorResultsImpl.getInstance(), new RDAPDatasetServiceImpl(fileSystem));
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

        // Parse the configuration definition file, and if the file is not parsable, exit with a return code of 1.
        try (InputStream is = fileSystem.uriToStream(this.config.getConfigurationFile())) {
            configurationFile = configParser.parse(is);
        } catch (Exception e) {
            logger.error("Configuration is invalid", e);
            return dumpErrorInfo(RDAPValidationStatus.CONFIG_INVALID.getValue(), config, query);
        }

        // set up the results file so we can write to it
        RDAPValidationResultFile rdapValidationResultFile = RDAPValidationResultFile.getInstance();
        rdapValidationResultFile.initialize(results, config, configurationFile, fileSystem);

        // If the parameter (--use-local-dataset) is set, use the dataset found in the filesystem,
        // download the dataset not found in the filesystem, and persist them in the filesystem.
        // If the parameter (--use-local-dataset) is not set, download all the dataset, and
        // overwrite the dataset in the filesystem.
        if (!datasetService.download(this.config.useLocalDatasets())) {
          // If one or more dataset cannot be downloaded, exit with a return code of 2.
          return dumpErrorInfo(RDAPValidationStatus.DATASET_UNAVAILABLE.getValue(), config, query);
        }

        // this checks if the query is valid query for the HttpQueryType or the FileQueryType
        if (!queryTypeProcessor.check(datasetService)) {
            return dumpErrorInfo(queryTypeProcessor.getErrorStatus().getValue(), config, query);
        }

        query.setResults(results);
        if (!query.run()) {
            if (query.getErrorStatus() == null) {
                // it means it is 13001 or 13002, the status will be null, and we should exit with code 0
                return RDAPValidationStatus.SUCCESS.getValue();
            }
            return dumpErrorInfo(query.getErrorStatus().getValue(), config, query);
        }

        // this always returns true - don't bother checking the return
        query.checkWithQueryType(queryTypeProcessor.getQueryType());

        // Check if we are doing a domain query for test.invalid and the response code was 200, that is bad but continue on
        if (ResponseValidationTestInvalidDomain.isHttpOKAndTestDotInvalid(query, queryTypeProcessor, results, rdapValidationResultFile)) {
            logger.info("Detected a test.invalid domain query with HTTP 200 response code.");
        }

        // Schema validation
        if (query.isErrorContent()) {
            validator = new SchemaValidator("rdap_error.json", results, datasetService);
        } else {
            String schemaFile = schemaMap.get(queryTypeProcessor.getQueryType());
            if (schemaFile != null) {
                if (RDAPQueryType.ENTITY.equals(queryTypeProcessor.getQueryType()) && config.isThin()) {
                    logger.error("Thin flag is set while validating entity");
                    return dumpErrorInfo(RDAPValidationStatus.USES_THIN_MODEL.getValue(), config, query);
                }
                // asEventActor property is not allow in topMost entity object, see spec 7.2.9.2
                validator = new SchemaValidator(schemaFile, results, datasetService);
            }
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

        // Additionally, apply the relevant collection tests when the option
        // --use-rdap-profile-february-2019 or --use-rdap-profile-february-2024 is set
        // query.isErrorContent() added as condition in cases where they have 404 as status code
        if ((config.useRdapProfileFeb2019() || config.useRdapProfileFeb2024()) && !query.isErrorContent()) {
            logger.info("Validations for 2019 profile");
            RDAPProfile rdapProfile = new RDAPProfile(
                getRdapValidations(rdapResponse, config, results, datasetService, queryTypeProcessor, validator,
                    query));
            rdapProfile.validate();
        }

        // Additionally, apply the relevant collection tests when the option
        // --use-rdap-profile-february-2024 is set
        // query.isErrorContent() added as condition in cases where they have 404 as status code
        if (config.useRdapProfileFeb2024() && !query.isErrorContent()) {
            logger.info("Validations for 2024 profile");
            List<ProfileValidation> validations = get2024ProfileValidations(rdapResponse, config, results, query);
            RDAPProfile rdapProfile = new RDAPProfile(validations);
            rdapProfile.validate();
        }

        // finally we set the statusCode and results path
//        this.resultsPath = rdapValidationResultFile.resultPath;

        // we do not dumpInfo here, everything is fine
        return RDAPValidationStatus.SUCCESS.getValue();
    }

    @Override
    public RDAPValidatorResults getResults() {
        return null;
    }

    @Override
    public String getResultsPath() {
        return this.resultsPath;
    }

    private List<ProfileValidation> get2024ProfileValidations(HttpResponse<String> rdapResponse,
                                                              RDAPValidatorConfiguration config,
                                                              RDAPValidatorResults results,
                                                              RDAPQuery query) {
        List<ProfileValidation> validations = new ArrayList<>();
        validations.add(new TigValidation1Dot3_2024(query.getData(), results)); // clean
        validations.add(new ResponseValidation1Dot2_1_2024(query.getData(), results)); // clean
        validations.add(new ResponseValidation1Dot2_2_2024(query.getData(), results)); // clean
        validations.add(new ResponseValidationLinkElements_2024(query.getData(), results)); // clean
        validations.add(new ResponseValidationStatusDuplication_2024(query.getData(), results)); // clean
        validations.add(new StdRdapConformanceValidation_2024(query.getData(), results)); // clean

        // Conditionally add validations that require network connections
        if (config.isNetworkEnabled()) {
            logger.info("Network enabled tests for 2024 profile");
            validations.add(new TigValidation1Dot5_2024(rdapResponse, config, results)); // SSL Network connection
            validations.add(new ResponseValidationTestInvalidRedirect_2024(config, results)); // Network connection
        }

        return validations;
    }

    private List<ProfileValidation> getRdapValidations(HttpResponse<String> rdapResponse,
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
            new SimpleHandleValidation(query.getData(), results, datasetService, queryTypeProcessor.getQueryType(), -52102))); // clean
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
            validations.add(new TigValidation1Dot8(rdapResponse, results, datasetService)); // DNS queries
            validations.add(new TigValidation1Dot11Dot1(config, results, datasetService, queryTypeProcessor.getQueryType())); // assume you passed in a URL on the cli
        }

        return validations;
    }

    public int dumpErrorInfo(int exitCode, RDAPValidatorConfiguration config, RDAPQuery query) {
        System.out.println("Exit code: " + exitCode + " - " + RDAPValidationStatus.fromValue(exitCode).name());
        System.out.println("URI used for the query: " + config.getUri());
        if (query instanceof RDAPHttpQuery httpQuery) {
            System.out.println("Redirects followed: " + httpQuery.getRedirects());
            System.out.println("Accept header used for the query: " + httpQuery.getAcceptHeader());
        } else {
            System.out.println("Redirects followed: N/A (query is not an RDAPHttpQuery)");
            System.out.println("Accept header used for the query: N/A (query is not an RDAPHttpQuery)");
        }

        if (config.getUri() != null && config.getUri().getHost() != null) {
            String host = config.getUri().getHost();
            try {
                InetAddress address = InetAddress.getByName(host);
                if (address instanceof java.net.Inet6Address) {
                    System.out.println("IP protocol used for the query: IPv6");
                } else if (address instanceof java.net.Inet4Address) {
                    System.out.println("IP protocol used for the query: IPv4");
                } else {
                    System.out.println("IP protocol used for the query: Unknown");
                }
            } catch (UnknownHostException e) {
                System.out.println("Invalid host: " + host);
            }
        } else {
            System.out.println("IP protocol used for the query: unknown (URI or host is null)");
        }

        return exitCode;
    }
}
