package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQuery;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQueryTypeProcessor;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Objects;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_NOT_FOUND;
import static org.icann.rdapconformance.validator.CommonUtils.addErrorToResultsFile;

public class ResponseValidationDomainInvalid_2024 extends ProfileValidation {
    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationDomainInvalid_2024.class);
    public static final String DOMAIN_INVALID = "domain/not-a-domain.invalid";

    private final RDAPValidatorConfiguration config;

    public ResponseValidationDomainInvalid_2024(RDAPValidatorConfiguration config, RDAPValidatorResults results) {
        super(results);
        this.config = config;
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
                domainInvalidUri = domainInvalidUri.substring(0, index + DOMAIN_INVALID.length());
            } else {
                logger.debug("Domain Invalid word was not found, using original url");
                domainInvalidUri = this.config.getUri().getHost().concat(DOMAIN_INVALID);
            }
            logger.debug("Domain invalid URI built {}", domainInvalidUri);
        } else {
            domainInvalidUri = this.config.getUri().getHost().concat(DOMAIN_INVALID);
        }

        logger.info("Making request to: {}", domainInvalidUri);
        HttpResponse<String> response = null;

        response = RDAPHttpRequest.makeHttpGetRequest(new URI(domainInvalidUri), this.config.getTimeout());

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
                addErrorToResultsFile(domainInvalidStatusCode, -46701, String.valueOf(domainInvalidStatusCode),"A query for an invalid domain name did not yield a 404 response.");
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
            logger.info("Validating rdapConformance and errorCode property in top level");
            propertyExists = false;
        }

        return propertyExists;
    }
}
