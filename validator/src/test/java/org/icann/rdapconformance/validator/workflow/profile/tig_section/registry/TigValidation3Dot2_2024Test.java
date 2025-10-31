package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TigValidation3Dot2_2024Test extends ProfileJsonValidationTestBase {

    private RDAPValidatorConfiguration config;
    private RDAPQueryType queryType;
    private RDAPDatasetService datasetService;
    private RegistrarId registrarId;

    public TigValidation3Dot2_2024Test() {
        super("/validators/profile/tig_section/links/valid.json",
            "tigSection_3_2_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
        queryType = RDAPQueryType.DOMAIN;
        config = mock(RDAPValidatorConfiguration.class);
        doReturn(true).when(config).isGtldRegistry();

        try {
            doReturn(new URI("https://rdap.example.com/com/v1/domain/EXAMPLE.COM")).when(config).getUri();
        } catch (URISyntaxException uriSyntaxException) {
            throw new RuntimeException("uriSyntax exception, stopping testing");
        }

        datasetService = mock(RDAPDatasetService.class);
        registrarId = mock(RegistrarId.class);
        doReturn(registrarId).when(datasetService).get(RegistrarId.class);
    }

    public ProfileJsonValidation getProfileValidation() {
        return new TigValidation3Dot2_2024(queryContext);
    }


    @Test
    public void testDoLaunch_NotARegistry_IsFalse() {
        doReturn(false).when(config).isGtldRegistry();
        assertThat(getProfileValidation().doLaunch()).isFalse();
    }

    @Test
    public void testDoLaunch_NotADomainQuery_IsFalse() {
        doReturn(true).when(config).isGtldRegistry();
        queryType = RDAPQueryType.NAMESERVER;
        assertThat(getProfileValidation().doLaunch()).isFalse();
    }

    // RCT-104 only apply if the query is for a gtld registry and the value is not 9999
    @Test
    public void testValidate_NoLinksInTopmostObjectWithRegistrar9999_AddResults23200() {
        JSONObject identifier = new JSONObject();
        identifier.put("identifier", "9999");

        JSONArray publicIdArray = new JSONArray();
        publicIdArray.put(identifier);

        JSONObject publicIds = new JSONObject();
        publicIds.put("publicIds", publicIdArray);

        JSONArray entities = new JSONArray();
        entities.put(publicIds);
        jsonObject.put("entities", entities);

        TigValidation3Dot2_2024 tigValidation3Dot2_2024 = new TigValidation3Dot2_2024(queryContext);
        assertThat(tigValidation3Dot2_2024.isRegistrarId9999()).isTrue();
    }

    // RCT-104 only apply if the query is for a gtld registry and the value is not in excluded range (9994-9999)
    @Test
    public void testValidate_NoLinksInTopmostObjectWithRegistrar9998_IsExcluded() {
        JSONObject identifier = new JSONObject();
        identifier.put("identifier", "9998");

        JSONArray publicIdArray = new JSONArray();
        publicIdArray.put(identifier);

        JSONObject publicIds = new JSONObject();
        publicIds.put("publicIds", publicIdArray);

        JSONArray entities = new JSONArray();
        entities.put(publicIds);
        jsonObject.put("entities", entities);

        TigValidation3Dot2_2024 tigValidation3Dot2_2024 = new TigValidation3Dot2_2024(queryContext);
        assertThat(tigValidation3Dot2_2024.isExcludedRegistrarId()).isTrue();
        assertThat(tigValidation3Dot2_2024.isRegistrarId9999()).isTrue(); // Backward compatibility
    }

    @Test
    public void testValidate_NoLinksInTopmostObject_AddResults23201() throws URISyntaxException {
        doReturn(new URI("https://dummy.com")).when(config).getUri();
        validate(-23201, jsonObject.getJSONArray("links").toString(),
            "a value property must be specified and it must match the URI of the query.");
    }

    // -23202 Test cases: href domain query validation
    @Test
    public void testValidate_ValidDomainQueryHref_NoError() {
        // Valid case: href contains proper domain query
        JSONArray links = new JSONArray();
        JSONObject link1 = new JSONObject();
        link1.put("value", config.getUri().toString()); // Must match for -23201 to pass
        link1.put("rel", "related");
        link1.put("href", "https://rdap.example.com/rdap/domain/example.com");
        links.put(link1);

        jsonObject.put("links", links);
        validate(); // Should not generate any -23202 errors
    }

    @Test
    public void testValidate_VersionedUrlDomainQuery_NoError() {
        // Valid case: href contains versioned domain query (testing flexible pattern)
        JSONArray links = new JSONArray();
        JSONObject link1 = new JSONObject();
        link1.put("value", config.getUri().toString()); // Must match for -23201 to pass
        link1.put("rel", "related");
        link1.put("href", "https://rdap.example.com/rdap/v2/test_code/domain/example.org");
        links.put(link1);

        jsonObject.put("links", links);
        validate(); // Should not generate any -23202 errors
    }

    @Test
    public void testValidate_ApiVersionedDomainQuery_NoError() {
        // Valid case: href contains API versioned domain query
        JSONArray links = new JSONArray();
        JSONObject link1 = new JSONObject();
        link1.put("value", config.getUri().toString()); // Must match for -23201 to pass
        link1.put("rel", "related");
        link1.put("href", "https://api.registry.net/v1/rdap/domain/test.com");
        links.put(link1);

        jsonObject.put("links", links);
        validate(); // Should not generate any -23202 errors
    }

    @Test
    public void testValidate_InvalidHrefNotDomainQuery_AddResults23202() {
        JSONArray links = new JSONArray();
        JSONObject link1 = new JSONObject();
        link1.put("value", config.getUri().toString()); // Must match for -23201 to pass
        link1.put("rel", "related");
        link1.put("href", "https://rdap.example.com/rdap/nameserver/ns1.example.com");
        links.put(link1);
        
        jsonObject.put("links", links);
        validate(-23202, link1.toString(),
            "the href property must be domain query as defined by Section 3.1.3 of RFC 9082.");
    }

    @Test
    public void testValidate_EmptyHref_AddResults23202() {
        JSONArray links = new JSONArray();
        JSONObject link1 = new JSONObject();
        link1.put("value", config.getUri().toString()); // Must match for -23201 to pass
        link1.put("rel", "related");
        link1.put("href", "");
        links.put(link1);
        
        jsonObject.put("links", links);
        validate(-23202, link1.toString(),
            "the href property must be domain query as defined by Section 3.1.3 of RFC 9082.");
    }

    @Test
    public void testValidate_MalformedUrl_AddResults23202() {
        JSONArray links = new JSONArray();
        JSONObject link1 = new JSONObject();
        link1.put("value", config.getUri().toString()); // Must match for -23201 to pass
        link1.put("rel", "related");
        link1.put("href", "not-a-valid-url");
        links.put(link1);
        
        jsonObject.put("links", links);
        validate(-23202, link1.toString(),
            "the href property must be domain query as defined by Section 3.1.3 of RFC 9082.");
    }

    @Test
    public void testValidate_NonRelatedLink_NoValidation() {
        // Only links with rel="related" should be validated for -23202
        JSONArray links = new JSONArray();
        JSONObject link1 = new JSONObject();
        link1.put("value", config.getUri().toString()); // Must match for -23201 to pass
        link1.put("rel", "self");
        link1.put("href", "https://rdap.example.com/rdap/nameserver/ns1.example.com"); // Not a domain query
        links.put(link1);
        
        jsonObject.put("links", links);
        validate(); // Should not generate -23202 error since rel != "related"
    }

    @Test
    public void testValidate_LinkWithoutHref_NoValidation() {
        // Links without href should not trigger -23202 (they're handled by -23200)
        JSONArray links = new JSONArray();
        JSONObject link1 = new JSONObject();
        link1.put("value", config.getUri().toString()); // Must match for -23201 to pass
        link1.put("rel", "related");
        // No href property
        links.put(link1);
        
        jsonObject.put("links", links);
        validate(); // Should not generate -23202 error
    }

    @Test
    public void testValidate_MultipleLinksWithMixedHrefs_AddResults23202() {
        JSONArray links = new JSONArray();
        
        // Valid domain query
        JSONObject validLink = new JSONObject();
        validLink.put("value", config.getUri().toString()); // Must match for -23201 to pass
        validLink.put("rel", "related");
        validLink.put("href", "https://rdap.example.com/rdap/domain/example.com");
        links.put(validLink);
        
        // Invalid href
        JSONObject invalidLink = new JSONObject();
        invalidLink.put("rel", "related");
        invalidLink.put("href", "https://rdap.example.com/rdap/entity/EXAMPLE");
        links.put(invalidLink);
        
        jsonObject.put("links", links);
        validate(-23202, invalidLink.toString(),
            "the href property must be domain query as defined by Section 3.1.3 of RFC 9082.");
    }

    @Test
    public void testValidate_DifferentDomainQueryFormats_Valid() {
        // Test various valid domain query formats
        JSONArray links = new JSONArray();
        
        // Standard domain (with matching value for -23201)
        JSONObject link1 = new JSONObject();
        link1.put("value", config.getUri().toString()); // Must match for -23201 to pass
        link1.put("rel", "related");
        link1.put("href", "https://rdap.registrar.com/rdap/domain/example.com");
        links.put(link1);
        
        // Subdomain (no value property, only testing -23202)
        JSONObject link2 = new JSONObject();
        link2.put("rel", "related");
        link2.put("href", "http://rdap.example.net/rdap/domain/sub.example.com");
        links.put(link2);
        
        // IDN domain (A-label, no value property, only testing -23202)
        JSONObject link3 = new JSONObject();
        link3.put("rel", "related");
        link3.put("href", "https://rdap.example.org/rdap/domain/xn--fo-5ja.example");
        links.put(link3);
        
        jsonObject.put("links", links);
        validate(); // Should not generate any -23202 errors
    }

    @Test
    public void testValidate_ExcludedRegistrarIds_NoValidation() throws IOException {
        // Test that validation is skipped for all excluded registrar IDs
        String[] excludedIds = {"9994", "9995", "9996", "9997", "9998", "9999"};
        
        for (String excludedId : excludedIds) {
            // Reset jsonObject for each test
            setUp();
            
            // Set up excluded registrar ID
            JSONObject identifier = new JSONObject();
            identifier.put("identifier", excludedId);

            JSONArray publicIdArray = new JSONArray();
            publicIdArray.put(identifier);

            JSONObject publicIds = new JSONObject();
            publicIds.put("publicIds", publicIdArray);

            JSONArray entities = new JSONArray();
            entities.put(publicIds);
            jsonObject.put("entities", entities);
            
            // Add invalid href that would normally trigger -23202
            JSONArray links = new JSONArray();
            JSONObject invalidLink = new JSONObject();
            invalidLink.put("value", config.getUri().toString()); // Must match for -23201 to pass
            invalidLink.put("rel", "related");
            invalidLink.put("href", "https://rdap.example.com/rdap/nameserver/ns1.example.com");
            links.put(invalidLink);
            jsonObject.put("links", links);
            
            // Should not generate -23202 error due to excluded registrar ID
            validate();
        }
    }

    // Branch coverage improvement tests for isExcludedRegistrarId()
    @Test
    public void testIsExcludedRegistrarId_NoEntities_ReturnsFalse() {
        // Test case: no entities field at all
        TigValidation3Dot2_2024 validation = new TigValidation3Dot2_2024(queryContext);
        assertThat(validation.isExcludedRegistrarId()).isFalse();
    }

    @Test
    public void testIsExcludedRegistrarId_NullPublicIds_ReturnsFalse() {
        // Test case: entity exists but no publicIds field
        JSONObject entityWithoutPublicIds = new JSONObject();
        // No publicIds field added
        
        JSONArray entities = new JSONArray();
        entities.put(entityWithoutPublicIds);
        jsonObject.put("entities", entities);
        
        TigValidation3Dot2_2024 validation = new TigValidation3Dot2_2024(queryContext);
        assertThat(validation.isExcludedRegistrarId()).isFalse();
    }

    @Test
    public void testIsExcludedRegistrarId_EmptyPublicIds_ReturnsFalse() {
        // Test case: entity has publicIds but it's empty
        JSONObject entity = new JSONObject();
        entity.put("publicIds", new JSONArray()); // Empty array
        
        JSONArray entities = new JSONArray();
        entities.put(entity);
        jsonObject.put("entities", entities);
        
        TigValidation3Dot2_2024 validation = new TigValidation3Dot2_2024(queryContext);
        assertThat(validation.isExcludedRegistrarId()).isFalse();
    }

    @Test
    public void testIsExcludedRegistrarId_NonExcludedId_ReturnsFalse() {
        // Test case: publicId exists but not in excluded range
        JSONObject identifier = new JSONObject();
        identifier.put("identifier", "1234"); // Not in excluded range

        JSONArray publicIdArray = new JSONArray();
        publicIdArray.put(identifier);

        JSONObject entity = new JSONObject();
        entity.put("publicIds", publicIdArray);

        JSONArray entities = new JSONArray();
        entities.put(entity);
        jsonObject.put("entities", entities);
        
        TigValidation3Dot2_2024 validation = new TigValidation3Dot2_2024(queryContext);
        assertThat(validation.isExcludedRegistrarId()).isFalse();
    }

    @Test
    public void testIsExcludedRegistrarId_MultipleEntitiesFirstWithoutPublicIds_ReturnsFalse() {
        // Test case: multiple entities, first without publicIds, second with non-excluded ID
        JSONObject entityWithoutPublicIds = new JSONObject();
        // No publicIds field
        
        JSONObject identifier = new JSONObject();
        identifier.put("identifier", "1234"); // Not excluded

        JSONArray publicIdArray = new JSONArray();
        publicIdArray.put(identifier);

        JSONObject entityWithPublicIds = new JSONObject();
        entityWithPublicIds.put("publicIds", publicIdArray);

        JSONArray entities = new JSONArray();
        entities.put(entityWithoutPublicIds); // First entity without publicIds
        entities.put(entityWithPublicIds);    // Second entity with non-excluded ID
        jsonObject.put("entities", entities);
        
        TigValidation3Dot2_2024 validation = new TigValidation3Dot2_2024(queryContext);
        assertThat(validation.isExcludedRegistrarId()).isFalse();
    }

    @Test  
    public void testIsExcludedRegistrarId_MultiplePublicIdsNoneExcluded_ReturnsFalse() {
        // Test case: multiple publicIds, none are excluded
        JSONObject id1 = new JSONObject();
        id1.put("identifier", "1234");
        
        JSONObject id2 = new JSONObject();
        id2.put("identifier", "5678");

        JSONArray publicIdArray = new JSONArray();
        publicIdArray.put(id1);
        publicIdArray.put(id2);

        JSONObject entity = new JSONObject();
        entity.put("publicIds", publicIdArray);

        JSONArray entities = new JSONArray();
        entities.put(entity);
        jsonObject.put("entities", entities);
        
        TigValidation3Dot2_2024 validation = new TigValidation3Dot2_2024(queryContext);
        assertThat(validation.isExcludedRegistrarId()).isFalse();
    }
}