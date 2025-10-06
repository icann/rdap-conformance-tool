package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ResponseValidation2Dot6Dot3_2024Test extends ProfileJsonValidationTestBase {

    static final String statusPointer =
            "#/notices/0:{\"description\":[\"For more information on domain status codes, please visit https://icann.org/epp\",\"Calm down and come back later.\"],\"links\":[{\"rel\":\"glossary\",\"href\":\"https://icann.org/epp\",\"type\":\"text/html\",\"value\":\"https://example.net/entity/XXXX\"}],\"title\":\"test\",\"type\":\"result set truncated due to excessive load\"}";
    static final String descriptionPointer =
            "#/notices/0:{\"description\":[\"Calm down and come back later.\"],\"links\":[{\"rel\":\"glossary\",\"href\":\"https://icann.org/epp\",\"type\":\"text/html\",\"value\":\"https://example.net/entity/XXXX\"}],\"title\":\"Status Codes\",\"type\":\"result set truncated due to excessive load\"}";
    static final String linksPointer =
            "#/notices/0:{\"description\":[\"For more information on domain status codes, please visit https://icann.org/epp\",\"Calm down and come back later.\"],\"title\":\"Status Codes\",\"type\":\"result set truncated due to excessive load\"}";
    static final String hrefPointer =
            "#/notices/0:{\"description\":[\"For more information on domain status codes, please visit https://icann.org/epp\",\"Calm down and come back later.\"],\"links\":[{\"rel\":\"glossary\",\"href\":\"test\",\"type\":\"text/html\",\"value\":\"https://example.net/entity/XXXX\"}],\"title\":\"Status Codes\",\"type\":\"result set truncated due to excessive load\"}";
    static final String relPointer =
            "#/notices/0:{\"description\":[\"For more information on domain status codes, please visit https://icann.org/epp\",\"Calm down and come back later.\"],\"links\":[{\"rel\":\"test\",\"href\":\"https://icann.org/epp\",\"type\":\"text/html\",\"value\":\"https://example.net/entity/XXXX\"}],\"title\":\"Status Codes\",\"type\":\"result set truncated due to excessive load\"}";
    static final String valuePointer =
            "#/notices/0:{\"description\":[\"For more information on domain status codes, please visit https://icann.org/epp\",\"Calm down and come back later.\"],\"links\":[{\"rel\":\"glossary\",\"href\":\"https://icann.org/epp\",\"type\":\"text/html\",\"value\":\"http://test\"}],\"title\":\"Status Codes\",\"type\":\"result set truncated due to excessive load\"}";

    public ResponseValidation2Dot6Dot3_2024Test() {
        super("/validators/profile/response_validations/notices/valid.json",
                "rdapResponseProfile_2_6_3_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
        URI uri = URI.create("https://example.net/entity/XXXX");
        doReturn(uri).when(config).getUri();

        config = mock(RDAPValidatorConfiguration.class);
        doReturn(uri).when(config).getUri();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot6Dot3_2024(
                jsonObject.toString(),
                results,
                this.config,
                RDAPQueryType.DOMAIN);
    }

    @Test
    public void ResponseValidation2Dot6Dot3_2024_46601() {
        JSONObject notice = jsonObject.getJSONArray("notices").getJSONObject(0);


        notice.put("title", "test");
        validate(-46601, statusPointer, "The notice for Status Codes was not found.");
    }

    @Test
    public void ResponseValidation2Dot6Dot3_2024_46602() {
        JSONArray descriptions = jsonObject.getJSONArray("notices").getJSONObject(0).getJSONArray("description");

        descriptions.remove(0);
        validate(-46602, descriptionPointer, "The notice for Status Codes does not have the proper description.");
    }

    @Test
    public void ResponseValidation2Dot6Dot3_2024_46603() {
        JSONObject notice = jsonObject.getJSONArray("notices").getJSONObject(0);

        notice.remove("links");
        validate(-46603, linksPointer, "The notice for Status Codes does not have links.");
    }

    @Test
    public void ResponseValidation2Dot6Dot3_2024_46604() {
        JSONObject link = jsonObject.getJSONArray("notices").getJSONObject(0).getJSONArray("links").getJSONObject(0);

        link.put("href", "test");
        validate(-46604, hrefPointer, "The notice for Status Codes does not have a link to the status codes.");
    }

    @Test
    public void ResponseValidation2Dot6Dot3_2024_46605() {
        JSONObject link = jsonObject.getJSONArray("notices").getJSONObject(0).getJSONArray("links").getJSONObject(0);

        link.put("rel", "test");
        validate(-46605, relPointer, "The notice for Status Codes does not have a link relation type of glossary");
    }

    @Test
    public void ResponseValidation2Dot6Dot3_2024_46606() {
        JSONObject link = jsonObject.getJSONArray("notices").getJSONObject(0).getJSONArray("links").getJSONObject(0);

        link.put("value", "http://test");
        validate(-46606, valuePointer, "The notice for Status Codes does not have a link value of the request URL.");
    }

    @Test
    public void testDoLaunch_DomainQueryType_ReturnsTrue() {
        ResponseValidation2Dot6Dot3_2024 validation = new ResponseValidation2Dot6Dot3_2024(
                jsonObject.toString(),
                results,
                this.config,
                RDAPQueryType.DOMAIN);

        assert validation.doLaunch() : "doLaunch should return true for DOMAIN query type";
    }

    @Test
    public void testDoLaunch_HelpQueryType_ReturnsFalse() {
        ResponseValidation2Dot6Dot3_2024 validation = new ResponseValidation2Dot6Dot3_2024(
                jsonObject.toString(),
                results,
                this.config,
                RDAPQueryType.HELP);

        assert !validation.doLaunch() : "doLaunch should return false for HELP query type";
    }

    @Test
    public void testDoLaunch_EntityQueryType_ReturnsFalse() {
        ResponseValidation2Dot6Dot3_2024 validation = new ResponseValidation2Dot6Dot3_2024(
                jsonObject.toString(),
                results,
                this.config,
                RDAPQueryType.ENTITY);

        assert !validation.doLaunch() : "doLaunch should return false for ENTITY query type";
    }
}
