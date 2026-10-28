package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static java.net.HttpURLConnection.HTTP_OK;

import java.net.http.HttpResponse;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gTLD Profile v3 conformance: when the query is an entity search
 * (e.g. /entities?fn=...) and the HTTP status code is 200, the response must
 * contain an "entitySearchResults" JSON array. Emits -66004 otherwise.
 */
public final class ResponseValidationGtldProfile3EntitySearch extends ProfileJsonValidation {

    private static final Logger logger =
        LoggerFactory.getLogger(ResponseValidationGtldProfile3EntitySearch.class);

    static final String ENTITY_SEARCH_RESULTS = "entitySearchResults";
    private static final String JSON_POINTER = "#/" + ENTITY_SEARCH_RESULTS;

    private final QueryContext queryContext;

    public ResponseValidationGtldProfile3EntitySearch(QueryContext queryContext) {
        super(queryContext.getRdapResponseData(), queryContext.getResults());
        this.queryContext = queryContext;
    }

    @Override
    public String getGroupName() {
        return "gtldProfile3EntitySearch_Validation";
    }

    @Override
    protected boolean doValidate() {
        if (!RDAPQueryType.ENTITIES.equals(queryContext.getQueryType())) {
            logger.debug("Not an entity search query, skipping -66004 check.");
            return true;
        }

        HttpResponse<String> response = queryContext.getCurrentHttpResponse();
        if (response != null && response.statusCode() != HTTP_OK) {
            logger.debug("HTTP status {} is not 200, skipping -66004 check.", response.statusCode());
            return true;
        }

        if (jsonObject.opt(ENTITY_SEARCH_RESULTS) instanceof JSONArray) {
            return true;
        }

        results.add(RDAPValidationResult.builder()
            .code(-66004)
            .value(jsonObject.has(ENTITY_SEARCH_RESULTS)
                ? getResultValue(JSON_POINTER)
                : JSON_POINTER + ":null")
            .message("Expected an entitySearchResults array.")
            .build(queryContext));
        return false;
    }
}
