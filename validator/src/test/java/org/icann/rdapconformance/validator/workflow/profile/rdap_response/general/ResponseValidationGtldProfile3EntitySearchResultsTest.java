package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.List;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock;
import org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Uses a real {@link RDAPValidatorResults} (not a mock) because the nested entity schema
 * validation decides validity by the number of results accrued.
 */
public class ResponseValidationGtldProfile3EntitySearchResultsTest {

    private static final String GROUP = "gtldProfile3EntitySearchResults_Validation";
    private static final String MESSAGE = "entitySearchResults has an invalid entity.";

    private QueryContext queryContext;
    private RDAPValidatorResults results;
    private HttpResponse<String> httpResponse;
    private JSONObject jsonObject;
    private JSONObject validEntity;

    @BeforeMethod
    public void setUp() throws IOException {
        RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
        doReturn(true).when(config).isGtldRegistrar();
        doReturn(URI.create("https://example.com/entities?fn=Example")).when(config).getUri();

        RDAPDatasetServiceMock datasets = new RDAPDatasetServiceMock();
        datasets.download(true);

        queryContext = QueryContext.forTesting(config, datasets);
        results = queryContext.getResults();
        results.clear();

        httpResponse = mock(HttpResponse.class);
        doReturn(200).when(httpResponse).statusCode();
        queryContext.setQueryType(RDAPQueryType.ENTITIES);
        queryContext.setCurrentHttpResponse(httpResponse);

        validEntity = new JSONObject(SchemaValidatorTest.getResource("/validators/entity/valid.json"));
        jsonObject = new JSONObject()
            .put("rdapConformance", new JSONArray(List.of("rdap_level_0", "gtldProfile3")))
            .put("entitySearchResults", new JSONArray().put(validEntity));
    }

    private boolean validate() {
        queryContext.setRdapResponseData(jsonObject.toString());
        return new ResponseValidationGtldProfile3EntitySearchResults(queryContext).validate();
    }

    private List<RDAPValidationResult> resultsWithCode(int code) {
        return results.getAll().stream().filter(r -> r.getCode() == code).toList();
    }

    @Test
    public void testValidate_AllEntitiesValid_Ok() {
        jsonObject.getJSONArray("entitySearchResults").put(new JSONObject(validEntity.toString()));

        assertThat(validate()).isTrue();
        assertThat(resultsWithCode(-66005)).isEmpty();
        assertThat(results.getGroupOk()).contains(GROUP);
    }

    @Test
    public void testValidate_EmptyArray_Ok() {
        jsonObject.put("entitySearchResults", new JSONArray());

        assertThat(validate()).isTrue();
        assertThat(resultsWithCode(-66005)).isEmpty();
    }

    @Test
    public void testValidate_WrongObjectClassName_AddResult66005And12303() {
        JSONObject invalid = new JSONObject(validEntity.toString()).put("objectClassName", "domain");
        jsonObject.put("entitySearchResults", new JSONArray().put(invalid));

        assertThat(validate()).isFalse();
        assertThat(resultsWithCode(-12303)).isNotEmpty();
        assertThat(resultsWithCode(-66005))
            .singleElement()
            .hasFieldOrPropertyWithValue("value", "#/entitySearchResults/0:" + invalid)
            .hasFieldOrPropertyWithValue("message", MESSAGE);
        assertThat(results.getGroupErrorWarning()).contains(GROUP);
    }

    @Test
    public void testValidate_UnknownKey_AddResult66005And12301() {
        JSONObject invalid = new JSONObject(validEntity.toString()).put("unknown", "x");
        jsonObject.put("entitySearchResults", new JSONArray().put(invalid));

        assertThat(validate()).isFalse();
        assertThat(resultsWithCode(-12301)).isNotEmpty();
        assertThat(resultsWithCode(-66005)).hasSize(1);
    }

    @Test
    public void testValidate_NonObjectItem_AddResult66005() {
        jsonObject.put("entitySearchResults", new JSONArray().put("not-an-entity"));

        assertThat(validate()).isFalse();
        assertThat(resultsWithCode(-66005))
            .singleElement()
            .hasFieldOrPropertyWithValue("value", "#/entitySearchResults/0:not-an-entity");
    }

    @Test
    public void testValidate_OnlyInvalidEntitiesReported() {
        JSONObject invalid = new JSONObject(validEntity.toString()).put("handle", 123);
        jsonObject.put("entitySearchResults", new JSONArray()
            .put(new JSONObject(validEntity.toString()))
            .put(invalid)
            .put(new JSONObject(validEntity.toString())));

        assertThat(validate()).isFalse();
        assertThat(resultsWithCode(-12304)).isNotEmpty();
        assertThat(resultsWithCode(-66005))
            .singleElement()
            .hasFieldOrPropertyWithValue("value", "#/entitySearchResults/1:" + invalid);
    }

    @Test
    public void testValidate_NotArray_SkippedCoveredBy66004() {
        jsonObject.put("entitySearchResults", new JSONObject().put("objectClassName", "entity"));

        assertThat(validate()).isTrue();
        assertThat(resultsWithCode(-66005)).isEmpty();
    }

    @Test
    public void testValidate_MissingEntitySearchResults_Skipped() {
        jsonObject.remove("entitySearchResults");

        assertThat(validate()).isTrue();
        assertThat(resultsWithCode(-66005)).isEmpty();
    }

    @Test
    public void testValidate_NotEntitySearchQuery_Skipped() {
        queryContext.setQueryType(RDAPQueryType.ENTITY);
        jsonObject.put("entitySearchResults", new JSONArray().put("not-an-entity"));

        assertThat(validate()).isTrue();
        assertThat(resultsWithCode(-66005)).isEmpty();
    }

    @Test
    public void testValidate_Non200Status_Skipped() {
        doReturn(404).when(httpResponse).statusCode();
        jsonObject.put("entitySearchResults", new JSONArray().put("not-an-entity"));

        assertThat(validate()).isTrue();
        assertThat(resultsWithCode(-66005)).isEmpty();
    }

    @Test
    public void testValidate_NoHttpResponse_StillChecksEntities() {
        queryContext.setCurrentHttpResponse(null);
        jsonObject.put("entitySearchResults", new JSONArray().put("not-an-entity"));

        assertThat(validate()).isFalse();
        assertThat(resultsWithCode(-66005)).hasSize(1);
    }
}
