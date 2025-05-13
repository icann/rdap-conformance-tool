package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URI;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TigValidation3Dot3And3Dot4_2024Test extends ProfileJsonValidationTestBase {

    public TigValidation3Dot3And3Dot4_2024Test() {
        super(
            "/validators/profile/tig_section/notices/valid.json",
            "tigSection_3_3_and_3_4_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();

        URI uri = URI.create("https://www.example.com/domain-names/registration-data-access-protocol/terms-service/index.xhtml");
        doReturn(uri).when(config).getUri();

        config = mock(RDAPValidatorConfiguration.class);
        doReturn(uri).when(config).getUri();
    }

    @Override
    public ProfileJsonValidation getProfileValidation() {
        return new TigValidation3Dot3And3Dot4_2024(
            jsonObject.toString(),
            results,
            this.config);
    }

    @Test
    public void tigSection_3_3_and_3_4_Validation_2024_61200() {
        JSONObject link = jsonObject.getJSONArray("notices").getJSONObject(0).getJSONArray("links").getJSONObject(0);

        link.put("rel", "dummy");
        validate(-61200, jsonObject.toString(), "The response must have one notice to the terms of service.");
    }

    @Test
    public void tigSection_3_3_and_3_4_Validation_2024_61201() {
        JSONObject link = jsonObject.getJSONArray("notices").getJSONObject(0).getJSONArray("links").getJSONObject(0);

        link.put("href", "dummy");
        validate(-61201, link.toString(), "This link must have an href.");
    }

    @Test
    public void tigSection_3_3_and_3_4_Validation_2024_61202() {
        JSONObject link = jsonObject.getJSONArray("notices").getJSONObject(0).getJSONArray("links").getJSONObject(0);

        link.put("value", "dummy");
        validate(-61202, link.toString(), "This link must have a value that is the same as the queried URI.");
    }
}
