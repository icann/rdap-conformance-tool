package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static java.net.HttpURLConnection.HTTP_OK;

import java.net.http.HttpResponse;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.workflow.SchemaValidatorCache;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gTLD Profile v3 conformance: when the query is an entity search and the HTTP
 * status code is 200, every object in "entitySearchResults" must pass the entity
 * lookup validations (-12300 .. -12317, defined by rdap_entity.json).
 * Each entity that fails is reported with -66005; the underlying -123xx results
 * are emitted by the entity schema validation itself.
 */
public final class ResponseValidationGtldProfile3EntitySearchResults extends ProfileJsonValidation {

    private static final Logger logger =
        LoggerFactory.getLogger(ResponseValidationGtldProfile3EntitySearchResults.class);

    static final String ENTITY_SCHEMA = "rdap_entity.json";
    private static final String ENTITY_SEARCH_RESULTS = "entitySearchResults";
    private static final String JSON_POINTER = "#/" + ENTITY_SEARCH_RESULTS;

    private final QueryContext queryContext;

    public ResponseValidationGtldProfile3EntitySearchResults(QueryContext queryContext) {
        super(queryContext.getRdapResponseData(), queryContext.getResults());
        this.queryContext = queryContext;
    }

    @Override
    public String getGroupName() {
        return "gtldProfile3EntitySearchResults_Validation";
    }

    @Override
    protected boolean doValidate() {
        if (!RDAPQueryType.ENTITIES.equals(queryContext.getQueryType())) {
            logger.debug("Not an entity search query, skipping -66005 check.");
            return true;
        }

        HttpResponse<String> response = queryContext.getCurrentHttpResponse();
        if (response != null && response.statusCode() != HTTP_OK) {
            logger.debug("HTTP status {} is not 200, skipping -66005 check.", response.statusCode());
            return true;
        }

        // A missing or non-array entitySearchResults is reported by -66004.
        if (!(jsonObject.opt(ENTITY_SEARCH_RESULTS) instanceof JSONArray entities)) {
            logger.debug("entitySearchResults is not an array, skipping -66005 check.");
            return true;
        }

        SchemaValidator entityValidator = SchemaValidatorCache.getCachedValidator(
            ENTITY_SCHEMA, results, queryContext.getDatasetService(), queryContext);

        boolean isValid = true;
        for (int i = 0; i < entities.length(); i++) {
            Object entity = entities.opt(i);
            if (!(entity instanceof JSONObject) || !entityValidator.validate(entity.toString())) {
                results.add(RDAPValidationResult.builder()
                    .code(-66005)
                    .value(getResultValue(JSON_POINTER + "/" + i))
                    .message("entitySearchResults has an invalid entity.")
                    .build(queryContext));
                isValid = false;
            }
        }
        return isValid;
    }
}
