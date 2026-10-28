package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidationGtldProfile3EntitySearchTest extends ProfileJsonValidationTestBase {

    private static final String MESSAGE = "Expected an entitySearchResults array.";

    private HttpResponse<String> httpResponse;

    public ResponseValidationGtldProfile3EntitySearchTest() {
        super("/validators/profile/rdapConformance/valid.json", "gtldProfile3EntitySearch_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
        httpResponse = mock(HttpResponse.class);
        doReturn(200).when(httpResponse).statusCode();
        queryContext.setQueryType(RDAPQueryType.ENTITIES);
        queryContext.setCurrentHttpResponse(httpResponse);
        // Baseline: a valid entity search response
        jsonObject.put("entitySearchResults", new JSONArray()
            .put(new JSONObject(Map.of("objectClassName", "entity", "handle", "ABC-123"))));
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidationGtldProfile3EntitySearch(queryContext);
    }

    @Test
    public void testValidate_EmptyArray_Ok() {
        jsonObject.put("entitySearchResults", new JSONArray());
        validate();
    }

    @Test
    public void testValidate_MissingEntitySearchResults_AddResult66004() {
        jsonObject.remove("entitySearchResults");
        validate(-66004, "#/entitySearchResults:null", MESSAGE);
    }

    @Test
    public void testValidate_EntitySearchResultsNotArray_AddResult66004() {
        jsonObject.put("entitySearchResults", new JSONObject(Map.of("objectClassName", "entity")));
        validate(-66004, "#/entitySearchResults:{\"objectClassName\":\"entity\"}", MESSAGE);
    }

    @Test
    public void testValidate_EntitySearchResultsString_AddResult66004() {
        jsonObject.put("entitySearchResults", "not-an-array");
        validate(-66004, "#/entitySearchResults:not-an-array", MESSAGE);
    }

    @Test
    public void testValidate_NotEntitySearchQuery_Skips() {
        queryContext.setQueryType(RDAPQueryType.ENTITY);
        jsonObject.remove("entitySearchResults");
        validate();
    }

    @Test
    public void testValidate_Non200Status_Skips() {
        doReturn(404).when(httpResponse).statusCode();
        jsonObject.remove("entitySearchResults");
        validate();
    }

    @Test
    public void testValidate_NoHttpResponse_StillChecksStructure() {
        queryContext.setCurrentHttpResponse(null);
        jsonObject.put("entitySearchResults", List.of());
        validate();
    }
}
