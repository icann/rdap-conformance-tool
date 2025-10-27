package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

public class ResponseValidation2Dot7Dot6Dot2_2024Test extends ProfileJsonValidationTestBase {

    static final String telPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"test\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String pathLangPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"test\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}, #/redacted/3:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Tech Phone\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"test\"}";
    static final String pathLangObjectPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}, #/redacted/3:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Tech Phone\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":{}}";
    static final String pathLangMissingPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}, #/redacted/3:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Tech Phone\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"prePath\":\"$test\"}";
    static final String prePathExistingPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}, #/redacted/3:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Tech Phone\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"prePath\":\"$.redacted[*]\"}";
    static final String methodPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}, #/redacted/3:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"test\",\"name\":{\"type\":\"Tech Phone\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String telPointer1 = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}, #/redacted/3:{\"name\":{\"type\":\"Tech Phone\"}}";
    static final String telPointer2 = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}, #/redacted/3:{\"name\":{\"type\":\"Other\"}}, #/redacted/4:{\"name\":{\"type\":\"Tech Phone\"}}";
    static final String telPointer3 = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}, #/redacted/3:{\"extra\":\"value\",\"name\":{\"type\":\"Tech Phone\"}}";
    static final String telPointer4 = "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}, #/redacted/3:{\"name\":{\"type\":\"TECH PHONE\"}}, #/redacted/4:{\"name\":{\"type\":\"tech phone\"}}";


    public ResponseValidation2Dot7Dot6Dot2_2024Test() {
        super("/validators/profile/response_validations/vcard/valid.json",
                "rdapResponseProfile_2_7_6_2_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        QueryContext generalContext = new QueryContext(
            queryContext.getQueryId(),
            queryContext.getConfig(),
            queryContext.getDatasetService(),
            queryContext.getQuery(),
            queryContext.getResults(),
            RDAPQueryType.DOMAIN
        );
        generalContext.setRdapResponseData(queryContext.getRdapResponseData());
        return new ResponseValidation2Dot7Dot6Dot2_2024(generalContext);

    }

    @Test
    public void ResponseValidation2Dot7Dot6Dot2_2024_No_Technical() {
        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(1).getJSONArray("roles");
        roles.put(0, "registrar");
        validate();
    }

    // TESTS FOR -65100 FALSE POSITIVE BUG FIX (voice array type parameters)
    // These tests verify the fix for the bug where technical entities with valid 
    // voice tel properties using array type parameters (e.g., ["voice", "work"]) 
    // were incorrectly triggering -65100 errors
    
    @Test
    public void testTechnicalVoiceWithArrayType_ShouldNotTrigger65100() {
        // This test demonstrates the bug fix working correctly:
        // Technical entity HAS a valid voice tel property with type ["voice", "work"]
        // This should NOT trigger -65100 (which requires Tech Phone redaction when voice is missing)
        // Before the fix, the JSONPath couldn't detect array type parameters
        
        String jsonResponse;
        try {
            jsonResponse = getResource("/validators/profile/rdap_response/technical/technical_voice_array_type.json");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test JSON", e);
        }
        jsonObject = new JSONObject(jsonResponse);
        
        ProfileValidation validation = getProfileValidation();
        boolean isValid = validation.validate();
        
        // The validation should pass (return true) because voice tel IS present
        assertThat(isValid).as("Validation should pass because technical entity HAS voice tel property").isTrue();
    }

    @Test 
    public void testTechnicalVoiceWithArrayType_WithoutHandle() {
        // Test the original bug scenario - handle validation is separate 
        String jsonResponse;
        try {
            jsonResponse = getResource("/validators/profile/rdap_response/technical/technical_voice_array_type.json");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test JSON", e);
        }
        jsonObject = new JSONObject(jsonResponse);
        
        // Remove handle to test missing handle scenario  
        removeKey("$.['entities'][0]['handle']");
        
        validate();
    }

    // These tests target specific code paths and edge cases to improve code coverage
    // They cover error handling, malformed data, and various input combinations
    @Test
    public void testTechnicalEntity_WithVoiceTypeAsArray_MultipleValues() {
        // Test case where type is an array with multiple values including "voice"
        JSONObject technicalEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        jsonObject.getJSONArray("redacted").remove(0);
        technicalEntity.getJSONArray("roles").put(0, "technical");
        
        JSONArray telProperty = technicalEntity.getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject params = telProperty.getJSONObject(1);
        // Set type as array with multiple values
        params.put("type", new JSONArray().put("voice").put("work").put("home"));
        
        validate();
    }

    @Test
    public void testTechnicalEntity_WithVoiceTypeAsArray_NoVoice() {
        // Test case where type is an array but doesn't contain "voice"
        JSONObject technicalEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        technicalEntity.getJSONArray("roles").put(0, "technical");
        
        JSONArray telProperty = technicalEntity.getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject params = telProperty.getJSONObject(1);
        // Set type as array without "voice"
        params.put("type", new JSONArray().put("work").put("home"));
        
        // Remove existing redacted array to trigger -65100
        jsonObject.remove("redacted");
        
        // This should require Tech Phone redaction
        validate(-65100, "", "a redaction of type Tech Phone is required.");
    }
    
    @Test 
    public void testTechnicalEntity_WithMalformedVcardProperty() {
        // Test case with malformed vcard property to trigger catch block in hasTechnicalVoiceTelProperty
        JSONObject technicalEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        technicalEntity.getJSONArray("roles").put(0, "technical");
        
        JSONArray vcardProps = technicalEntity.getJSONArray("vcardArray").getJSONArray(1);
        
        // Add malformed property - not an array but an object
        vcardProps.put(new JSONObject().put("invalid", "structure"));
        
        // Remove original tel with voice to trigger redaction requirement
        vcardProps.remove(4);
        
        // Remove existing redacted array to trigger -65100
        jsonObject.remove("redacted");
        
        validate(-65100, "", "a redaction of type Tech Phone is required.");
    }
    
    @Test
    public void testTechnicalEntity_WithNonStringInTypeArray() {
        // Test case where type array contains non-string elements (exception handling)
        JSONObject technicalEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        jsonObject.getJSONArray("redacted").remove(0);
        technicalEntity.getJSONArray("roles").put(0, "technical");
        
        JSONArray telProperty = technicalEntity.getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject params = telProperty.getJSONObject(1);
        
        // Set type as array with mixed types including non-strings
        JSONArray typeArray = new JSONArray();
        typeArray.put(123); // number
        typeArray.put(new JSONObject()); // object
        typeArray.put("voice"); // string at the end
        params.put("type", typeArray);
        
        validate();
    }
    
    @Test
    public void testTechnicalEntity_WithEmptyTypeArray() {
        // Test case where type is an empty array
        JSONObject technicalEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        technicalEntity.getJSONArray("roles").put(0, "technical");
        
        JSONArray telProperty = technicalEntity.getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject params = telProperty.getJSONObject(1);
        
        // Set type as empty array
        params.put("type", new JSONArray());
        
        // Remove existing redacted array to trigger -65100
        jsonObject.remove("redacted");
        
        validate(-65100, "", "a redaction of type Tech Phone is required.");
    }
    
    @Test
    public void testTechnicalEntity_WithNullVcardArray() {
        // Test case where vcardArray is missing
        JSONObject technicalEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        technicalEntity.getJSONArray("roles").put(0, "technical");
        
        // Remove vcardArray entirely
        technicalEntity.remove("vcardArray");
        
        // Remove existing redacted array to trigger -65100
        jsonObject.remove("redacted");
        
        validate(-65100, "", "a redaction of type Tech Phone is required.");
    }
    
    @Test
    public void testTechnicalEntity_WithShortVcardArray() {
        // Test case where vcardArray doesn't have enough elements
        JSONObject technicalEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        technicalEntity.getJSONArray("roles").put(0, "technical");
        
        // Replace with vcardArray that only has the "vcard" string
        technicalEntity.put("vcardArray", new JSONArray().put("vcard"));
        
        // Remove existing redacted array to trigger -65100
        jsonObject.remove("redacted");
        
        validate(-65100, "", "a redaction of type Tech Phone is required.");
    }
    
    @Test
    public void testTechnicalEntity_WithInvalidTelProperty() {
        // Test case with tel property that isn't properly formed
        JSONObject technicalEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        technicalEntity.getJSONArray("roles").put(0, "technical");
        
        JSONArray vcardProps = technicalEntity.getJSONArray("vcardArray").getJSONArray(1);
        
        // Replace tel property with one that has too few elements
        vcardProps.put(4, new JSONArray().put("tel"));
        
        // Remove existing redacted array to trigger -65100
        jsonObject.remove("redacted");
        
        validate(-65100, "", "a redaction of type Tech Phone is required.");
    }
    
    @Test
    public void testTechnicalEntity_WithTypeAsUnexpectedObject() {
        // Test case where type is neither string nor array (covers else branch in hasVoiceType)
        JSONObject technicalEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        technicalEntity.getJSONArray("roles").put(0, "technical");
        
        JSONArray telProperty = technicalEntity.getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject params = telProperty.getJSONObject(1);
        
        // Set type as a number or boolean (unexpected type)
        params.put("type", 42);
        
        // Remove existing redacted array to trigger -65100
        jsonObject.remove("redacted");
        
        validate(-65100, "", "a redaction of type Tech Phone is required.");
    }
    
    @Test
    public void testRedactedArray_WithMalformedNameObject() {
        // Test redacted array exception handling with various malformed entries
        JSONObject technicalEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        technicalEntity.getJSONArray("roles").put(0, "technical");
        
        // Remove tel voice
        JSONArray vcardProps = technicalEntity.getJSONArray("vcardArray").getJSONArray(1);
        vcardProps.remove(4);
        
        // Add redacted array with various malformed entries to trigger exception handling
        JSONArray redactedArray = new JSONArray();
        
        // Add entry without name property at all
        redactedArray.put(new JSONObject().put("method", "removal"));
        
        // Add entry with name as string instead of object
        redactedArray.put(new JSONObject().put("name", "invalid"));
        
        // Add entry with name object but no type
        redactedArray.put(new JSONObject().put("name", new JSONObject().put("description", "something")));
        
        // Add entry with name.type as non-string
        redactedArray.put(new JSONObject().put("name", new JSONObject().put("type", 123)));
        
        jsonObject.put("redacted", redactedArray);
        
        validate(-65100, "#/redacted/0:{\"method\":\"removal\"}, #/redacted/1:{\"name\":\"invalid\"}, #/redacted/2:{\"name\":{\"description\":\"something\"}}, #/redacted/3:{\"name\":{\"type\":123}}", "a redaction of type Tech Phone is required.");
    }

    @Test
    public void ResponseValidation2Dot7Dot6Dot2_2024_65100() {
        JSONObject telVoiceObject = jsonObject.getJSONArray("entities").getJSONObject(1).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4).getJSONObject(1);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        telVoiceObject.remove("type");
        redactedObject.getJSONObject("name").put("type", "test");
        validate(-65100, telPointer, "a redaction of type Tech Phone is required.");
    }

    @Test
    public void ResponseValidation2Dot7Dot6Dot2_2024_65101() {
        String redactedTechPhone = "{\n" +
                "  \"reason\" : {\n" +
                "    \"description\" : \"Server policy\"\n" +
                "  },\n" +
                "  \"method\" : \"emptyValue\",\n" +
                "  \"name\" : {\n" +
                "    \"type\" : \"Tech Phone\"\n" +
                "  },\n" +
                "  \"postPath\" : \"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\n" +
                "  \"pathLang\" : \"jsonpath\"\n" +
                "}";
        JSONObject telVoiceObject = jsonObject.getJSONArray("entities").getJSONObject(1).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4).getJSONObject(1);
        jsonObject.getJSONArray("redacted").put(new JSONObject(redactedTechPhone));
        jsonObject.getJSONArray("redacted").getJSONObject(0).getJSONObject("name").put("type", "test");
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(3);

        telVoiceObject.remove("type");
        redactedObject.put("pathLang", "test");
        validate(-65101, pathLangPointer, "jsonpath is invalid for Tech Phone.");
    }

    @Test
    public void ResponseValidation2Dot7Dot6Dot2_2024_65101_By_PathLangObject() {
        String redactedTechPhone = "{\n" +
                "  \"reason\" : {\n" +
                "    \"description\" : \"Server policy\"\n" +
                "  },\n" +
                "  \"method\" : \"emptyValue\",\n" +
                "  \"name\" : {\n" +
                "    \"type\" : \"Tech Phone\"\n" +
                "  },\n" +
                "  \"postPath\" : \"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\n" +
                "  \"pathLang\" : \"jsonpath\"\n" +
                "}";
        JSONObject telVoiceObject = jsonObject.getJSONArray("entities").getJSONObject(1).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4).getJSONObject(1);
        jsonObject.getJSONArray("redacted").put(new JSONObject(redactedTechPhone));
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(3);

        telVoiceObject.remove("type");
        redactedObject.put("pathLang", new JSONObject());
        validate(-65101, pathLangObjectPointer, "jsonpath is invalid for Tech Phone.");
    }

    @Test
    public void ResponseValidation2Dot7Dot6Dot2_2024_65101_By_MissingPathLang_Bad_PrePath() {
        String redactedTechPhone = "{\n" +
                "  \"reason\" : {\n" +
                "    \"description\" : \"Server policy\"\n" +
                "  },\n" +
                "  \"method\" : \"emptyValue\",\n" +
                "  \"name\" : {\n" +
                "    \"type\" : \"Tech Phone\"\n" +
                "  },\n" +
                "  \"postPath\" : \"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\n" +
                "  \"pathLang\" : \"jsonpath\"\n" +
                "}";
        JSONObject telVoiceObject = jsonObject.getJSONArray("entities").getJSONObject(1).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4).getJSONObject(1);
        jsonObject.getJSONArray("redacted").put(new JSONObject(redactedTechPhone));
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(3);

        telVoiceObject.remove("type");
        redactedObject.remove("pathLang");
        redactedObject.put("prePath", "$test");
        validate(-65101, pathLangMissingPointer, "jsonpath is invalid for Tech Phone.");
    }

    @Test
    public void ResponseValidation2Dot7Dot6Dot2_2024_65102_By_MissingPathLang_Bad_PrePath() {
        String redactedTechPhone = "{\n" +
                "  \"reason\" : {\n" +
                "    \"description\" : \"Server policy\"\n" +
                "  },\n" +
                "  \"method\" : \"emptyValue\",\n" +
                "  \"name\" : {\n" +
                "    \"type\" : \"Tech Phone\"\n" +
                "  },\n" +
                "  \"postPath\" : \"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\n" +
                "  \"pathLang\" : \"jsonpath\"\n" +
                "}";
        JSONObject telVoiceObject = jsonObject.getJSONArray("entities").getJSONObject(1).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4).getJSONObject(1);
        jsonObject.getJSONArray("redacted").put(new JSONObject(redactedTechPhone));
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(3);

        telVoiceObject.remove("type");
        redactedObject.remove("pathLang");
        redactedObject.put("prePath", "$.redacted[*]");
        validate(-65102, prePathExistingPointer, "jsonpath must evaluate to a zero set for redaction by removal of Tech Phone.");
    }

    @Test
    public void ResponseValidation2Dot7Dot6Dot2_2024_65103_By_Method() {
        String redactedTechPhone = "{\n" +
                "  \"reason\" : {\n" +
                "    \"description\" : \"Server policy\"\n" +
                "  },\n" +
                "  \"method\" : \"emptyValue\",\n" +
                "  \"name\" : {\n" +
                "    \"type\" : \"Tech Phone\"\n" +
                "  },\n" +
                "  \"postPath\" : \"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\n" +
                "  \"pathLang\" : \"jsonpath\"\n" +
                "}";
        JSONObject telVoiceObject = jsonObject.getJSONArray("entities").getJSONObject(1).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4).getJSONObject(1);
        jsonObject.getJSONArray("redacted").put(new JSONObject(redactedTechPhone));
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(3);

        telVoiceObject.remove("type");
        redactedObject.put("method", "test");
        validate(-65103, methodPointer, "Tech Phone redaction method must be removal if present");
    }

    @Test
    public void testMalformedRedactedArray() throws java.io.IOException {
        // Load malformed JSON that has malformed redacted object at index 0 
        // but valid "Tech Phone" redaction at index 1
        String malformedContent = getResource("/validators/profile/response_validations/vcard/malformed_redacted_test.json");
        jsonObject = new org.json.JSONObject(malformedContent);
        
        // This should pass validation because "Tech Phone" redaction exists at index 1,
        // even though index 0 has malformed "name": null  
        validate(); // Should NOT generate -65100 error
    }

    @Test
    public void testMultiRoleTechnical() throws java.io.IOException {
        // REGRESSION TEST: Verify multi-role entities are handled correctly after RCT-345 fix
        // Changed from @.roles[0]=='technical' to @.roles contains 'technical'

        String multiRoleContent = getResource("/validators/profile/response_validations/vcard/valid_org_multi_role.json");
        jsonObject = new org.json.JSONObject(multiRoleContent);
        jsonObject.getJSONArray("redacted").remove(2);

        // Test JSON has entity with roles: ["technical", "registrant"]
        // Now correctly found with 'contains' operator regardless of role position
        // Should pass validation with multi-role technical entity
        validate(); // Should pass - technical entity correctly found
    }

    @Test
    public void testTechnicalVoiceTel_NoRedactedTechPhone_ShouldPass() {
        // Technical entity with voice tel, no redacted Tech Phone
        JSONObject technicalEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        technicalEntity.getJSONArray("roles").put(0, "technical");
        // Ensure tel property with type "voice" exists
        JSONArray telProperty = technicalEntity.getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject params = telProperty.getJSONObject(1);
        params.put("type", "voice");
        // Remove any redacted Tech Phone
        JSONArray redacted = jsonObject.optJSONArray("redacted");
        if (redacted != null) {
            for (int i = redacted.length() - 1; i >= 0; i--) {
                JSONObject obj = redacted.getJSONObject(i);
                if (obj.has("name") && obj.getJSONObject("name").optString("type").equalsIgnoreCase("Tech Phone")) {
                    redacted.remove(i);
                }
            }
        }
        validate(); // Should pass, no error
    }

    @Test
    public void testTechnicalVoiceTel_WithRedactedTechPhone_ShouldFail65104() {
        // Technical entity with voice tel, redacted Tech Phone present
        JSONObject technicalEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        technicalEntity.getJSONArray("roles").put(0, "technical");
        JSONArray telProperty = technicalEntity.getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject params = telProperty.getJSONObject(1);
        params.put("type", "voice");
        // Add a redacted Tech Phone
        JSONArray redacted = jsonObject.optJSONArray("redacted");
        if (redacted == null) {
            redacted = new JSONArray();
            jsonObject.put("redacted", redacted);
        }
        JSONObject techPhoneRedaction = new JSONObject();
        techPhoneRedaction.put("name", new JSONObject().put("type", "Tech Phone"));
        redacted.put(techPhoneRedaction);
        validate(-65104, telPointer1, "a redaction of type Tech Phone was found but tech phone was not redacted.");
    }

    @Test
    public void testTechnicalVoiceTel_MultipleRedactedEntries_OneTechPhone_ShouldFail65104() {
        // Technical entity with voice tel, multiple redacted entries, one is Tech Phone
        JSONObject technicalEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        technicalEntity.getJSONArray("roles").put(0, "technical");
        JSONArray telProperty = technicalEntity.getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject params = telProperty.getJSONObject(1);
        params.put("type", "voice");
        JSONArray redacted = jsonObject.optJSONArray("redacted");
        if (redacted == null) {
            redacted = new JSONArray();
            jsonObject.put("redacted", redacted);
        }
        // Add unrelated redacted
        redacted.put(new JSONObject().put("name", new JSONObject().put("type", "Other")));
        // Add valid Tech Phone redacted
        redacted.put(new JSONObject().put("name", new JSONObject().put("type", "Tech Phone")));
        validate(-65104, telPointer2, "a redaction of type Tech Phone was found but tech phone was not redacted.");
    }

    @Test
    public void testTechnicalVoiceTel_RedactedTechPhoneWithExtraFields_ShouldFail65104() {
        // Technical entity with voice tel, redacted Tech Phone with extra/malformed fields
        JSONObject technicalEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        technicalEntity.getJSONArray("roles").put(0, "technical");
        JSONArray telProperty = technicalEntity.getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject params = telProperty.getJSONObject(1);
        params.put("type", "voice");
        JSONArray redacted = jsonObject.optJSONArray("redacted");
        if (redacted == null) {
            redacted = new JSONArray();
            jsonObject.put("redacted", redacted);
        }
        JSONObject techPhoneRedaction = new JSONObject();
        techPhoneRedaction.put("name", new JSONObject().put("type", "Tech Phone"));
        techPhoneRedaction.put("extra", "value"); // extra field
        redacted.put(techPhoneRedaction);
        validate(-65104, telPointer3, "a redaction of type Tech Phone was found but tech phone was not redacted.");
    }

    @Test
    public void testTechnicalVoiceTel_RedactedTechPhoneTypeCaseInsensitive_ShouldFail65104() {
        // Technical entity with voice tel, redacted Tech Phone with type case variations
        JSONObject technicalEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        technicalEntity.getJSONArray("roles").put(0, "technical");
        JSONArray telProperty = technicalEntity.getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject params = telProperty.getJSONObject(1);
        params.put("type", "voice");
        JSONArray redacted = jsonObject.optJSONArray("redacted");
        if (redacted == null) {
            redacted = new JSONArray();
            jsonObject.put("redacted", redacted);
        }
        // Add Tech Phone with different case
        redacted.put(new JSONObject().put("name", new JSONObject().put("type", "TECH PHONE")));
        redacted.put(new JSONObject().put("name", new JSONObject().put("type", "tech phone")));
        validate(-65104, telPointer4, "a redaction of type Tech Phone was found but tech phone was not redacted.");
    }
}
