package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.StatusCodes;
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
import static org.icann.rdapconformance.validator.CommonUtils.addErrorToResultsFile;

public class ResponseValidationHelp_2024 extends ProfileValidation {
    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationHelp_2024.class);
    public static final String HELP = "/help";

    private final RDAPValidatorConfiguration config;

    public ResponseValidationHelp_2024(RDAPValidatorConfiguration config, RDAPValidatorResults results) {
        super(results);
        this.config = config;
    }

    @Override
    public String getGroupName() {
        return "rdapResponseHelp_2024_Validation";
    }

    @Override
    public boolean doValidate() throws Exception {
        boolean isValid = true;

        logger.debug("Creating help query for host {}", config.getUri().getHost());
        var queryType = RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.getType(this.config.getUri().toString());
        String helpUri = null;
        if (Objects.nonNull(queryType)) {
            helpUri = CommonUtils.replaceQueryTypeInStringWith(queryType, this.config.getUri().toString(), "help");
            int index = helpUri.indexOf("help");
            if (index != -1) {
                helpUri = helpUri.substring(0, index + "help".length());
            } else {
                logger.debug("Help word was not found, using original url");
                helpUri = this.config.getUri().getHost().concat(HELP);
            }
            logger.debug("Help URI built {}", helpUri);
        } else {
            helpUri = this.config.getUri().getHost().concat(HELP);
        }

        logger.info("Making request to: {}", helpUri);
        HttpResponse<String> response = null;

        response = RDAPHttpRequest.makeHttpGetRequest(new URI(helpUri), this.config.getTimeout());

        // final response
        return validateHelpQuery(response, isValid);
    }

    boolean validateHelpQuery(HttpResponse<String> httpHelpResponse, boolean isValid) {
        RDAPHttpQuery.JsonData jsonHelpResponse = null;
        int httpHelpStatusCode = httpHelpResponse.statusCode();
        String rdapHelpResponse = httpHelpResponse.body();

        jsonHelpResponse = new RDAPHttpQuery.JsonData(rdapHelpResponse);
        if (jsonHelpResponse.isValid()) {
            if(!isHelpJsonValid(jsonHelpResponse ) || HTTP_OK != httpHelpStatusCode) {
                addErrorToResultsFile(-20701, rdapHelpResponse,"Response to a /help query did not yield a proper status code or RDAP response.");
                isValid = false;
            }
        } else {
            isValid = false;
        }

        return isValid;
    }

    private boolean isHelpJsonValid(RDAPHttpQuery.JsonData jsonHelpResponse) {
        boolean propertyExists = true;
        if(!jsonHelpResponse.hasKey("rdapConformance") || !jsonHelpResponse.hasKey("notices")) {
            logger.info("Validating rdapConformance and notices property in top level");
            propertyExists = false;
        }

        return propertyExists;
    }
}
