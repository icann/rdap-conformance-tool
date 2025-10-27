package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQuery;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQueryTypeProcessor;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Objects;

import static org.icann.rdapconformance.validator.CommonUtils.GET;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_NOT_FOUND;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

public class ResponseValidationDomainInvalid_2024 extends ProfileValidation {
    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationDomainInvalid_2024.class);
    public static final String DOMAIN_INVALID = "/domain/not-a-domain.invalid";

    private final RDAPValidatorConfiguration config;
    private final QueryContext queryContext;

    public ResponseValidationDomainInvalid_2024(RDAPValidatorConfiguration config, RDAPValidatorResults results) {
        super(results);
        this.config = config;
        this.queryContext = null; // Legacy constructor for backward compatibility
    }

    public ResponseValidationDomainInvalid_2024(QueryContext queryContext) {
        super(queryContext.getResults());
        this.config = queryContext.getConfig();
        this.queryContext = queryContext;
    }

    @Override
    public String getGroupName() {
        return "rdapResponseDomainInvalid_2024_Validation";
    }

    @Override
    public boolean doValidate() throws Exception {
        boolean isValid = true;

        logger.debug("Creating domain invalid query for host {}", config.getUri().getHost());
        var queryType = RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.getType(this.config.getUri().toString());
        String domainInvalidUri = null;
        if (Objects.nonNull(queryType)) {
            domainInvalidUri = CommonUtils.replaceQueryTypeInStringWith(queryType, this.config.getUri().toString(), DOMAIN_INVALID);
            int index = domainInvalidUri.indexOf("domain");
            if (index != -1) {
                domainInvalidUri = domainInvalidUri.substring(ZERO, index + DOMAIN_INVALID.length());
            } else {
                logger.debug("Domain Invalid word was not found, using original url");
                domainInvalidUri = this.config.getUri().getHost().concat(DOMAIN_INVALID);
            }
            logger.debug("Domain invalid URI built {}", domainInvalidUri);
        } else {
            domainInvalidUri = this.config.getUri().getHost().concat(DOMAIN_INVALID);
        }
        String domainInvalidUriCleaned = CommonUtils.cleanStringFromExtraSlash(domainInvalidUri);

        logger.debug("Making request to: {}", domainInvalidUriCleaned);
        HttpResponse<String> response = null;

        if (queryContext != null) {
            // Use QueryContext-aware request for proper IPv6/IPv4 protocol handling
            response = RDAPHttpRequest.makeRequest(queryContext, new URI(domainInvalidUriCleaned), this.config.getTimeout(), GET);
        } else {
            response = RDAPHttpRequest.makeHttpGetRequest(new URI(domainInvalidUriCleaned), this.config.getTimeout());
        }

        // final response
        return validateDomainInvalidQuery(response, isValid);
    }

boolean validateDomainInvalidQuery(HttpResponse<String> domainInvalidResponse, boolean isValid) {
    RDAPHttpQuery.JsonData jsonDomainInvalidResponse = null;
    int domainInvalidStatusCode = domainInvalidResponse.statusCode();
    String rdapDomainInvalidResponse = domainInvalidResponse.body();

    jsonDomainInvalidResponse = new RDAPHttpQuery.JsonData(rdapDomainInvalidResponse);
    if(HTTP_NOT_FOUND != domainInvalidStatusCode) {
        if(!isDomainInvalidJsonValid(jsonDomainInvalidResponse )) {
            results.add(RDAPValidationResult.builder()
                                            .queriedURI(domainInvalidResponse.uri().toString())
                                            .httpMethod(GET)
                                            .httpStatusCode(domainInvalidStatusCode)
                                            .code(-65300)
                                            .value(String.valueOf(domainInvalidStatusCode))
                                            .message("A query for an invalid domain name did not yield a 404 response.")
                                            .build());
            isValid = false;
        }
    }

    return isValid;
}

    private boolean isDomainInvalidJsonValid(RDAPHttpQuery.JsonData jsonDomainInvalidResponse) {
        boolean propertyExists = true;

        if(!jsonDomainInvalidResponse.isValid()) {
            return false;
        }

        var code = jsonDomainInvalidResponse.getValue("errorCode");
        if(!(code instanceof Integer errorCode)) {
            return false;
        }


        if(!jsonDomainInvalidResponse.hasKey("rdapConformance")
                || !jsonDomainInvalidResponse.hasKey("errorCode")
                ||  errorCode != 404) {
            logger.debug("Validating rdapConformance and errorCode property in top level");
            propertyExists = false;
        }

        return propertyExists;
    }
}
