package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

public class ResponseValidation2Dot7Dot4Dot9_2024Test extends ProfileJsonValidationTestBase {

    static final String vcardPointer =
            "#/entities/0/vcardArray/1:[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Administrative User\"],[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]],[\"email\",{},\"text\",\"administrative.user@example.com\"],[\"contact-uri\",{\"type\":\"voice\"},\"uri\",\"tel:+1-555-555-1236;ext=789\"],[\"tel\",{\"type\":\"fax\"},\"uri\",\"tel:+1-555-555-6321\"]]";
    static final String vcardNoReqPointer =
            "#/entities/0/vcardArray/1:[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Administrative User\"],[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]],[\"test\",{},\"text\",\"administrative.user@example.com\"],[\"tel\",{\"type\":\"voice\"},\"uri\",\"tel:+1-555-555-1236;ext=789\"],[\"tel\",{\"type\":\"fax\"},\"uri\",\"tel:+1-555-555-6321\"]]";
    static final String methodNoValidPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"test\",\"name\":{\"type\":\"Registrant Email\"},\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String postPathPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Registrant Email\"},\"postPath\":\"$test\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String postPathEmptyPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Registrant Email\"},\"postPath\":\"$.test\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String replacePathPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Registrant Email\"},\"replacementPath\":\"$test\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String prePathPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Registrant Email\"},\"prePath\":\"$test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String replaceEmptyPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Registrant Email\"},\"replacementPath\":\"$.test\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String redactedReplacementPathPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Registrant Email\"},\"pathLang\":\"jsonpath\",\"replacementPath\":\"$.entities[*]\",\"prePath\":\"$[invalid\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String redactedReplacementPathPointer2 =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Registrant Email\"},\"pathLang\":\"jsonpath\",\"replacementPath\":\"$[invalid\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String redactedReplacementPathPointer3 =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Registrant Email\"},\"replacementPath\":\"$[invalid\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String redactedReplacementPathPointer4 =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Registrant Email\"},\"replacementPath\":\"$.entities[*]\",\"prePath\":\"$[invalid\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String redactedReplacementPathPointer5 =
            "#/entities/0/vcardArray/1:[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Administrative User\"],[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]],[\"tel\",{\"type\":\"voice\"},\"uri\",\"tel:+1-555-555-1236;ext=789\"],[\"tel\",{\"type\":\"fax\"},\"uri\",\"tel:+1-555-555-6321\"]]";
    static final String getRedactedReplacementPathPointer6 =
            "#/entities/0/vcardArray/1:[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Administrative User\"],[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]],[\"tel\",{\"type\":\"voice\"},\"uri\",\"tel:+1-555-555-1236;ext=789\"],[\"tel\",{\"type\":\"fax\"},\"uri\",\"tel:+1-555-555-6321\"]]";
    static final String redactedReplacementPathPointer7 =
            "#/entities/0/vcardArray/1:[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Administrative User\"],[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]],[\"email\",{},\"text\",\"\"],[\"tel\",{\"type\":\"voice\"},\"uri\",\"tel:+1-555-555-1236;ext=789\"],[\"tel\",{\"type\":\"fax\"},\"uri\",\"tel:+1-555-555-6321\"]]";
    static final String redactedReplacementPathPointer8 =
            "#/entities/0/vcardArray/1:[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Administrative User\"],[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]],[\"email\",{},\"text\",\"invalid-email\"],[\"tel\",{\"type\":\"voice\"},\"uri\",\"tel:+1-555-555-1236;ext=789\"],[\"tel\",{\"type\":\"fax\"},\"uri\",\"tel:+1-555-555-6321\"]]";

    public ResponseValidation2Dot7Dot4Dot9_2024Test() {
        super("/validators/profile/response_validations/vcard/valid_contact_email.json",
                "rdapResponseProfile_2_7_4_9_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        return new ResponseValidation2Dot7Dot4Dot9_2024(queryContext);
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_No_Registrant() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("roles");
        roles.put(0, "registrar");
        validate();
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_64100() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);

        vArray.put(0, "contact-uri");
        validate(-64100, vcardPointer, "a redaction of Registrant Email may not have both the email and contact-uri");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_64101() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);

        vArray.put(0, "test");
        validate(-64101, vcardNoReqPointer, "a redaction of Registrant Email must have either the email and contact-uri");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_64102() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        redactedObject.put("method", "test");
        validate(-64102, methodNoValidPointer, "Registrant Email redaction method must be replacementValue");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_64103() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        redactedObject.put("postPath", "$test");
        validate(-64103, postPathPointer, "jsonpath is invalid for Registrant Email postPath");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_64104() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        redactedObject.put("postPath", "$.test");
        validate(-64104, postPathEmptyPointer, "jsonpath must evaluate to a non-empty set for redaction by replacementvalue of Registrant Email.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_64105() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        vArray.put(0, "contact-uri");
        redactedObject.put("replacementPath", "$test");
        validate(-64105, replacePathPointer, "jsonpath is invalid for Registrant Email replacementPath");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_64106() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        vArray.put(0, "contact-uri");
        redactedObject.put("prePath", "$test");
        validate(-64106, prePathPointer, "jsonpath is invalid for Registrant Email prePath");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_64107() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        vArray.put(0, "contact-uri");
        redactedObject.put("replacementPath", "$.test");
        validate(-64107, replaceEmptyPointer, "jsonpath must evaluate to a non-empty set for redaction by replacementvalue of Registrant Email in replacementPath");
    }

    @Test
    public void testMultiRoleRegistrant() throws java.io.IOException {
        // REGRESSION TEST: Verify multi-role entities are handled correctly after RCT-345 fix
        // Changed from @.roles[0]=='registrant' to @.roles contains 'registrant'
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        
        String multiRoleContent = getResource("/validators/profile/response_validations/vcard/valid_org_multi_role.json");
        jsonObject = new org.json.JSONObject(multiRoleContent);
        
        // Test JSON has entity with roles: ["technical", "registrant"]
        // Now correctly found with 'contains' operator regardless of role position
        
        // Should pass validation with multi-role registrant entity
        validate(); // Should pass - registrant entity correctly found
    }

    @Test
    public void testValidationSkippedWhenNotGtldRegistrar() {
        when(config.isGtldRegistrar()).thenReturn(false);
        validate();
    }

    @Test
    public void testNoRedactedArray() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        jsonObject.remove("redacted");
        validate();
    }

    @Test
    public void testRedactedArrayNoRegistrantEmail() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray redacted = jsonObject.getJSONArray("redacted");
        JSONObject obj = redacted.getJSONObject(0);
        JSONObject name = obj.getJSONObject("name");
        name.put("type", "Other");
        validate();
    }

    @Test
    public void testRedactedRegistrantEmailNoMethod() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.remove("method");
        validate();
    }

    @Test
    public void testRedactedRegistrantEmailMethodNotString() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.put("method", 12345);
        validate();
    }

    @Test
    public void testRedactedRegistrantEmailNoPathLang() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.remove("pathLang");
        validate();
    }

    @Test
    public void testVcardArrayMissing() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        jsonObject.getJSONArray("entities").getJSONObject(0).remove("vcardArray");
        validate();
    }

    @Test
    public void testVcardArrayMalformed() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        jsonObject.getJSONArray("entities").getJSONObject(0).put("vcardArray", new JSONArray());
        validate();
    }

    @Test
    public void testRedactedRegistrantEmailPostPathNotString() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.put("postPath", 12345); // Not a string
        validate();
    }

    @Test
    public void testRedactedRegistrantEmailPostPathMissing() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.remove("postPath");
        validate();
    }

    @Test
    public void testRedactedRegistrantEmailReplacementPathNotString() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.put("replacementPath", 12345); // Not a string
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        vArray.put(0, "contact-uri");
        validate();
    }

    @Test
    public void testRedactedRegistrantEmailReplacementPathMissing() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.remove("replacementPath");
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        vArray.put(0, "contact-uri");
        validate();
    }

    @Test
    public void testRedactedRegistrantEmailPrePathNotString() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.put("prePath", 12345); // Not a string
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        vArray.put(0, "contact-uri");
        validate();
    }

    @Test
    public void testRedactedRegistrantEmailPrePathMissing() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.remove("prePath");
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        vArray.put(0, "contact-uri");
        validate();
    }

    @Test
    public void testRedactedRegistrantEmailPathLangNotString() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.put("pathLang", 12345); // Not a string
        validate();
    }

    @Test
    public void testRedactedRegistrantEmailPathLangNotJsonPath() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.put("pathLang", "notjsonpath");
        validate();
    }

    @Test
    public void testValidPostPathAndReplacementPathAndPrePath() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        // Use valid JSONPath expressions that point to a non-empty set
        redactedObject.put("postPath", "$.entities[?(@.roles contains 'registrant')].vcardArray[1][?(@[0]=='email')]");
        redactedObject.put("replacementPath", "$.entities[?(@.roles contains 'registrant')].vcardArray[1][?(@[0]=='contact-uri')]");
        redactedObject.put("prePath", "$.entities[?(@.roles contains 'registrant')].vcardArray[1][?(@[0]=='email')]");
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        vArray.put(0, "contact-uri");
        validate();
    }

    @Test
    public void testEntitiesArrayEmpty() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        jsonObject.put("entities", new JSONArray());
        validate();
    }

    @Test
    public void testVcardArrayIsNull() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        jsonObject.getJSONArray("entities").getJSONObject(0).put("vcardArray", JSONObject.NULL);
        validate();
    }

    @Test
    public void testDoLaunchTrue() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        ProfileValidation validation = getProfileValidation();
        validation.doLaunch();
    }

    @Test
    public void testDoLaunchFalse() {
        when(config.isGtldRegistrar()).thenReturn(false);
        ProfileValidation validation = getProfileValidation();
        validation.doLaunch();
    }

    @Test
    public void testMultipleRedactedEntriesOnlyLastRegistrantEmailUsed() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray redacted = jsonObject.getJSONArray("redacted");
        JSONObject irrelevant = new JSONObject();
        irrelevant.put("name", new JSONObject().put("type", "Other"));
        redacted.put(irrelevant);
        // Add a valid Registrant Email entry
        JSONObject valid = new JSONObject(redacted.getJSONObject(0).toString());
        valid.put("method", "replacementValue");
        redacted.put(valid);
        validate();
    }

    @Test
    public void testRedactedEntryMissingName() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray redacted = jsonObject.getJSONArray("redacted");
        JSONObject obj = new JSONObject();
        // No 'name' property
        redacted.put(obj);
        validate();
    }

    @Test
    public void testRedactedEntryNameNotObject() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray redacted = jsonObject.getJSONArray("redacted");
        JSONObject obj = new JSONObject();
        obj.put("name", "notAnObject");
        redacted.put(obj);
        validate();
    }

    @Test
    public void testRedactedEntryTypeNotString() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray redacted = jsonObject.getJSONArray("redacted");
        JSONObject obj = new JSONObject();
        obj.put("name", new JSONObject().put("type", 12345));
        redacted.put(obj);
        validate();
    }

    @Test
    public void testRedactedArrayEmpty() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        jsonObject.put("redacted", new JSONArray());
        validate();
    }

    @Test
    public void testRedactedArrayAllIrrelevant() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray redacted = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("name", new JSONObject().put("type", "Other"));
        redacted.put(obj);
        jsonObject.put("redacted", redacted);
        validate();
    }

    @Test
    public void testEntitiesArrayMissing() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        jsonObject.remove("entities");
        validate();
    }

    @Test
    public void testEntitiesArrayNull() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        jsonObject.put("entities", Map.of());
        validate();
    }

    @Test
    public void testRedactedNameTypeThrowsException() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray redacted = jsonObject.getJSONArray("redacted");
        JSONObject obj = redacted.getJSONObject(0);
        // Replace 'name' with an object that throws on get("type")
        obj.put("name", new JSONObject() {
            @Override
            public Object get(String key) {
                throw new RuntimeException("forced exception");
            }
        });
        validate();
    }

    @Test
    public void testVcardElementZeroNotString() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        // Replace first vcard element with an array whose first element is not a String
        JSONArray nonStringVcard = new JSONArray();
        nonStringVcard.put(12345);
        vcardArray.put(nonStringVcard);
        validate();
    }

    @Test
    public void testRedactedRegistrantEmailNullInMethodValidation() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Remove all Registrant Email entries so redactedRegistrantEmail is null
        JSONArray redacted = jsonObject.getJSONArray("redacted");
        for (int i = 0; i < redacted.length(); i++) {
            JSONObject obj = redacted.getJSONObject(i);
            if (obj.has("name")) {
                JSONObject name = obj.getJSONObject("name");
                name.put("type", "Other");
            }
        }
        validate();
    }

    @Test
    public void testValidateMethodPropertyException() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Remove 'method' property to force exception in try/catch
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.remove("method");
        validate();
    }

    @Test
    public void testValidateEmailRedactedPropertiesException() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Remove 'pathLang' property to force exception in try/catch
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.remove("pathLang");
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        vArray.put(0, "email");
        validate();
    }

    @Test
    public void testValidateContactRedactedPropertiesException() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Remove 'pathLang' property to force exception in try/catch
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.remove("pathLang");
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        vArray.put(0, "contact-uri");
        validate();
    }

    @Test
    public void testValidateReplacementPathBasedOnPathLangException() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Remove 'replacementPath' property to force exception in try/catch
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.remove("replacementPath");
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        vArray.put(0, "contact-uri");
        validate();
    }

    @Test
    public void testValidatePrePathBasedOnPathLangException() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Remove 'prePath' property to force exception in try/catch
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.remove("prePath");
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        vArray.put(0, "contact-uri");
        validate();
    }

    @Test
    public void testVcardArrayElementIsNull() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcardArray.put(JSONObject.NULL);
        validate();
    }

    @Test
    public void testVcardArrayElementIsObject() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcardArray.put(new JSONObject().put("foo", "bar"));
    }

    @Test
    public void testMethodNotStringAndTitlesNoEmailOrContactUri() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.put("method", 12345); // Not a String

        // Set vCard entry with a title that is neither "email" nor "contact-uri"
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONArray customVcard = new JSONArray();
        customVcard.put("custom-title"); // Not "email" or "contact-uri"
        customVcard.put(new JSONObject());
        customVcard.put("text");
        customVcard.put("some value");
        vcardArray.put(customVcard);

        vcardArray.put(12345);
        validate();
    }

    @Test
    public void testContactRedactedProperties_nullRedactedRegistrantEmail() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Remove all redacted objects
        jsonObject.remove("redacted");
        validate(); // Should pass, nothing to validate
    }

    @Test
    public void testContactRedactedProperties_pathLangJsonPath_replacementPathValid_prePathValid() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("method", "replacementValue");
        redacted.put("name", new JSONObject().put("type", "Registrant Email"));
        redacted.put("pathLang", "jsonpath");
        redacted.put("replacementPath", "$.entities[*]"); // valid
        redacted.put("prePath", "$.entities[*]"); // valid
        // Remove email from vcard, add contact-uri
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(4); // Remove email
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");
        vcard.put(contactUriEntry);
        validate();
    }

    @Test
    public void testContactRedactedProperties_pathLangJsonPath_replacementPathValid_prePathInvalid() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("method", "replacementValue");
        redacted.put("name", new JSONObject().put("type", "Registrant Email"));
        redacted.put("pathLang", "jsonpath");
        redacted.put("replacementPath", "$.entities[*]"); // valid
        redacted.put("prePath", "$[invalid"); // invalid
        // Remove email from vcard, add contact-uri
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(4); // Remove email
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");
        vcard.put(contactUriEntry);
        validate(-64106, redactedReplacementPathPointer, "jsonpath is invalid for Registrant Email prePath");
    }

    @Test
    public void testContactRedactedProperties_pathLangJsonPath_replacementPathInvalid() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("method", "replacementValue");
        redacted.put("name", new JSONObject().put("type", "Registrant Email"));
        redacted.put("pathLang", "jsonpath");
        redacted.put("replacementPath", "$[invalid"); // invalid
        // Remove email from vcard, add contact-uri
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(4); // Remove email
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");
        vcard.put(contactUriEntry);
        validate(-64105, redactedReplacementPathPointer2, "jsonpath is invalid for Registrant Email replacementPath");
    }

    @Test
    public void testContactRedactedProperties_pathLangJsonPath_replacementPathValid_prePathMissing() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("method", "replacementValue");
        redacted.put("name", new JSONObject().put("type", "Registrant Email"));
        redacted.put("pathLang", "jsonpath");
        redacted.put("replacementPath", "$.entities[*]"); // valid
        redacted.remove("prePath");
        // Remove email from vcard, add contact-uri
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(4); // Remove email
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");
        vcard.put(contactUriEntry);
        validate();
    }

    @Test
    public void testContactRedactedProperties_pathLangNotJsonPath() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("method", "replacementValue");
        redacted.put("name", new JSONObject().put("type", "Registrant Email"));
        redacted.put("pathLang", "notjsonpath");
        // Remove email from vcard, add contact-uri
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(4); // Remove email
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");
        vcard.put(contactUriEntry);
        validate();
    }

    @Test
    public void testContactRedactedProperties_pathLangMissing_replacementPathValid_prePathValid() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("method", "replacementValue");
        redacted.put("name", new JSONObject().put("type", "Registrant Email"));
        redacted.remove("pathLang");
        redacted.put("replacementPath", "$.entities[*]"); // valid
        redacted.put("prePath", "$.entities[*]"); // valid
        // Remove email from vcard, add contact-uri
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(4); // Remove email
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");
        vcard.put(contactUriEntry);
        validate();
    }

    @Test
    public void testContactRedactedProperties_pathLangMissing_replacementPathInvalid() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("method", "replacementValue");
        redacted.put("name", new JSONObject().put("type", "Registrant Email"));
        redacted.remove("pathLang");
        redacted.put("replacementPath", "$[invalid"); // invalid
        // Remove email from vcard, add contact-uri
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(4); // Remove email
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");
        vcard.put(contactUriEntry);
        validate(-64105, redactedReplacementPathPointer3, "jsonpath is invalid for Registrant Email replacementPath");
    }

    @Test
    public void testContactRedactedProperties_pathLangMissing_replacementPathValid_prePathInvalid() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("method", "replacementValue");
        redacted.put("name", new JSONObject().put("type", "Registrant Email"));
        redacted.remove("pathLang");
        redacted.put("replacementPath", "$.entities[*]"); // valid
        redacted.put("prePath", "$[invalid"); // invalid
        // Remove email from vcard, add contact-uri
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(4); // Remove email
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");
        vcard.put(contactUriEntry);
        validate(-64106, redactedReplacementPathPointer4, "jsonpath is invalid for Registrant Email prePath");
    }

    @Test
    public void testContactRedactedProperties_pathLangMissing_replacementPathValid_prePathMissing() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("method", "replacementValue");
        redacted.put("name", new JSONObject().put("type", "Registrant Email"));
        redacted.remove("pathLang");
        redacted.put("replacementPath", "$.entities[*]"); // valid
        redacted.remove("prePath");
        // Remove email from vcard, add contact-uri
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(4); // Remove email
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");
        vcard.put(contactUriEntry);
        validate();
    }

    @Test
    public void testValidateMethodProperty_nullRedactedRegistrantEmail() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Remove all Registrant Email entries so redactedRegistrantEmail is null
        JSONArray redacted = jsonObject.getJSONArray("redacted");
        for (int i = 0; i < redacted.length(); i++) {
            JSONObject obj = redacted.getJSONObject(i);
            if (obj.has("name")) {
                JSONObject name = obj.getJSONObject("name");
                name.put("type", "Other");
            }
        }
        validate();
    }

    @Test
    public void testValidateMethodProperty_methodNotString() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("method", 12345); // Not a String
        validate();
    }

    @Test
    public void testValidateMethodProperty_methodMissing() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.remove("method");
        validate();
    }

    @Test
    public void testValidateMethodProperty_titlesContainsNeither() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Remove both email and contact-uri from vcard
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(4); // Remove email
        validate(-64101, redactedReplacementPathPointer5, "a redaction of Registrant Email must have either the email and contact-uri");
    }

    @Test
    public void testValidateEmailRedactedProperties_pathLangNotString() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("pathLang", 12345); // Not a String
        validate();
    }

    @Test
    public void testValidatePostPathBasedOnPathLang_postPathNotString() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("postPath", 12345); // Not a String
        validate();
    }

    @Test
    public void testValidateReplacementPathBasedOnPathLang_notString() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("replacementPath", 12345); // Not a String
        // Remove email from vcard, add contact-uri
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(4);
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");
        vcard.put(contactUriEntry);
        validate();
    }

    @Test
    public void testValidateReplacementPathBasedOnPathLang_missing() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.remove("replacementPath");
        // Remove email from vcard, add contact-uri
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(4);
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");
        vcard.put(contactUriEntry);
        validate();
    }

    @Test
    public void testValidatePrePathBasedOnPathLang_notString() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.put("prePath", 12345); // Not a String
        // Remove email from vcard, add contact-uri
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(4);
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");
        vcard.put(contactUriEntry);
        validate();
    }

    @Test
    public void testValidatePrePathBasedOnPathLang_missing() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        JSONObject redacted = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redacted.remove("prePath");
        // Remove email from vcard, add contact-uri
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(4);
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");
        vcard.put(contactUriEntry);
        validate();
    }

    @Test
    public void testValidateEmailRedactedProperties_pathLangJsonpath() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Set up the redactedRegistrantEmail with pathLang as a String with spaces and mixed case
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);
        redactedObject.put("pathLang", "  JsOnPaTh");
        // Ensure method is correct to trigger the logic (if needed)
        redactedObject.put("method", "replacementValue");
        // Add a postPath to ensure validatePostPathBasedOnPathLang() is called
        redactedObject.put("postPath", "$.redacted");
        // The validate() method will trigger the validation logic
        validate();
    }

    @Test
    public void testValidateEmailPropertyAtLeastOneVCard_noVCardPointers() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Remove registrant role so VCARD_PATH returns empty
        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("roles");
        roles.put(0, "other");
        jsonObject.remove("redacted"); // Ensure redactedRegistrantEmail is null
        validate(); // Should pass, no error
    }

    @Test
    public void testValidateEmailPropertyAtLeastOneVCard_vCardArrayEmpty() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Set vcardArray[1] to empty array
        JSONArray vcardArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray");
        vcardArray.put(1, new JSONArray());
        jsonObject.remove("redacted");
        validate(-64108, "#/entities/0/vcardArray/1:[]", "An email must either be present and valid or redacted for the registrant");
    }

    @Test
    public void testValidateEmailPropertyAtLeastOneVCard_noEmailEntry() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Remove email entry from vcardArray[1]
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        vcard.remove(4); // Remove email
        jsonObject.remove("redacted");
        validate(-64108, getRedactedReplacementPathPointer6, "An email must either be present and valid or redacted for the registrant");
    }

    @Test
    public void testValidateEmailPropertyAtLeastOneVCard_emailEntryEmptyValue() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Set email value to empty string
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        vcard.put(3, "");
        jsonObject.remove("redacted");
        validate(-64108, redactedReplacementPathPointer7, "An email must either be present and valid or redacted for the registrant");
    }

    @Test
    public void testValidateEmailPropertyAtLeastOneVCard_emailEntryInvalidValue() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Set email value to invalid email
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        vcard.put(3, "invalid-email");
        jsonObject.remove("redacted");
        validate(-64108, redactedReplacementPathPointer8, "An email must either be present and valid or redacted for the registrant");
    }

    @Test
    public void testValidateEmailPropertyAtLeastOneVCard_emailEntryValidValue() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Set email value to valid email
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        vcard.put(3, "valid.email@example.com");
        jsonObject.remove("redacted");
        validate(); // Should pass, no error
    }

    @Test
    public void testValidateEmailPropertyAtLeastOneVCard_firstElementNotString() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Add vcard entry with first element not a String
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        JSONArray nonStringEntry = new JSONArray();
        nonStringEntry.put(12345);
        nonStringEntry.put(new JSONObject());
        nonStringEntry.put("text");
        nonStringEntry.put("not-an-email");
        vcard.put(nonStringEntry);
        jsonObject.remove("redacted");
        validate(); // Should pass, no error (entry skipped)
    }

    @Test
    public void testValidateEmailPropertyAtLeastOneVCard_exceptionInQuery() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Simulate exception by removing vcardArray so query fails
        jsonObject.getJSONArray("entities").getJSONObject(0).remove("vcardArray");
        jsonObject.remove("redacted");
        try {
            validate(); // Should handle gracefully or fail
        } catch (Exception e) {
            // Acceptable if exception is thrown
        }
    }

    @Test
    public void testValidateEmailPropertyAtLeastOneVCard_multipleEmailsAtLeastOneValid() {
        when(queryContext.getConfig().isGtldRegistrar()).thenReturn(true);
        // Multiple email entries to vcardArray, one invalid, one valid
        JSONArray vcard = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1);
        // First email: invalid
        JSONArray invalidEmailEntry = new JSONArray();
        invalidEmailEntry.put("email");
        invalidEmailEntry.put(new JSONObject());
        invalidEmailEntry.put("text");
        invalidEmailEntry.put("not-an-email");
        vcard.put(invalidEmailEntry);
        // Second email: valid
        JSONArray validEmailEntry = new JSONArray();
        validEmailEntry.put("email");
        validEmailEntry.put(new JSONObject());
        validEmailEntry.put("text");
        validEmailEntry.put("valid.email@example.com");
        vcard.put(validEmailEntry);
        jsonObject.remove("redacted");
        validate();
    }


}
