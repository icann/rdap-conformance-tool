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

import static java.net.HttpURLConnection.HTTP_OK;
import static org.icann.rdapconformance.validator.CommonUtils.GET;

public class ResponseValidationHelp_2024 extends ProfileValidation {
    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationHelp_2024.class);
    public static final String HELP = "/help";

    private final RDAPValidatorConfiguration config;
    private final QueryContext queryContext;

    public ResponseValidationHelp_2024(RDAPValidatorConfiguration config, RDAPValidatorResults results) {
        super(results);
        this.config = config;
        this.queryContext = null; // Legacy constructor for backward compatibility
    }

    public ResponseValidationHelp_2024(QueryContext queryContext) {
        super(queryContext.getResults());
        this.config = queryContext.getConfig();
        this.queryContext = queryContext;
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
            helpUri = CommonUtils.replaceQueryTypeInStringWith(queryType, this.config.getUri().toString(), HELP);
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

        String helpUriCleaned =  CommonUtils.cleanStringFromExtraSlash(helpUri);

        logger.debug("Making request to: {}", helpUriCleaned);
        HttpResponse<String> response = null;

        if (queryContext != null) {
            // Use QueryContext-aware request for proper IPv6/IPv4 protocol handling
            response = RDAPHttpRequest.makeRequest(queryContext, new URI(helpUriCleaned), this.config.getTimeout(), GET);
        } else {
            response = RDAPHttpRequest.makeHttpGetRequest(new URI(helpUriCleaned), this.config.getTimeout());
        }

        // final response
        return validateHelpQuery(response, isValid);
    }

    boolean validateHelpQuery(HttpResponse<String> httpHelpResponse, boolean isValid) {
        RDAPHttpQuery.JsonData jsonHelpResponse = null;
        int httpHelpStatusCode = httpHelpResponse.statusCode();
        String rdapHelpResponse = httpHelpResponse.body();

        jsonHelpResponse = new RDAPHttpQuery.JsonData(rdapHelpResponse);
        if(!isHelpJsonValid(jsonHelpResponse ) || HTTP_OK != httpHelpStatusCode) {
            results.add(RDAPValidationResult.builder()
                                            .queriedURI(httpHelpResponse.uri().toString())
                                            .httpMethod(GET)
                                            .httpStatusCode(httpHelpStatusCode)
                                            .code(-20701)
                                            .value(rdapHelpResponse)
                                            .message("Response to a /help query did not yield a proper status code or RDAP response.")
                                            .build());
            isValid = false;
        }

        return isValid;
    }

    private boolean isHelpJsonValid(RDAPHttpQuery.JsonData jsonHelpResponse) {
        boolean propertyExists = true;
        if(!jsonHelpResponse.hasKey("rdapConformance") || !jsonHelpResponse.hasKey("notices")) {
            logger.debug("Validating rdapConformance and notices property in top level");
            propertyExists = false;
        }

        return propertyExists;
    }
}
