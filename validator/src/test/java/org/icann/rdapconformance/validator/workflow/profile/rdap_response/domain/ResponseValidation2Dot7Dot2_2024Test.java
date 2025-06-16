package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.json.JSONArray;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ResponseValidation2Dot7Dot2_2024Test extends ProfileJsonValidationTestBase {
    private RDAPValidatorConfiguration config;
    static final String DOMAIN_PATH= "#:{\"objectClassName\":\"domain\",\"notices\":[{\"description\":[\"Service subject to Terms of Use.\"]," +
            "\"links\":[{\"rel\":\"terms-of-service\",\"href\":\"https://www.example.com/domain-names/registration-data-access-protocol/terms-service/index.xhtml\"," +
            "\"type\":\"text/html\",\"value\":\"https://www.example.com/domain-names/registration-data-access-protocol/terms-service/index.xhtml\"}]," +
            "\"title\":\"Terms of Use\"},{\"description\":[\"For more information on domain status codes, please visit https://icann.org/epp\"]," +
            "\"links\":[{\"rel\":\"self\",\"href\":\"https://icann.org/epp\",\"type\":\"text/html\",\"value\":\"https://icann.org/epp\"}],\"title\":\"Status Codes\"}," +
            "{\"description\":[\"URL of the ICANN RDDS Inaccuracy Complaint Form: https://icann.org/wicf\"],\"links\":[{\"rel\":\"self\",\"href\":\"https://icann.org/wicf\"," +
            "\"type\":\"text/html\",\"value\":\"https://icann.org/wicf\"}],\"title\":\"RDDS Inaccuracy Complaint Form\"}],\"nameservers\":[{\"objectClassName\":\"nameserver\"," +
            "\"handle\":\"2138514_NS1_DOMAIN_COM-EXMP\",\"ldhName\":\"NS1.EXAMPLE.COM\",\"status\":[\"active\"]},{\"objectClassName\":\"nameserver\"," +
            "\"handle\":\"2138514_NS2_DOMAIN_COM-EXEMPLE\",\"ldhName\":\"NS2.EXAMPLE.COM\",\"status\":[\"active\"]},{\"objectClassName\":\"nameserver\"," +
            "\"handle\":\"2138514_NS3_DOMAIN_COM-EXEMPLE\",\"ldhName\":\"NS3.EXAMPLE.COM\",\"status\":[\"active\"]},{\"objectClassName\":\"nameserver\"," +
            "\"handle\":\"2138514_NS_DOMAIN_COM-EXEMPLE\",\"ldhName\":\"NS4.EXAMPLE.COM\",\"status\":[\"active\"]}],\"entities\":[{\"objectClassName\":\"entity\",\"publicIds\":[{\"identifier\":\"292\"," +
            "\"type\":\"IANA Registrar ID\"}],\"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Inc.\"]]]," +
            "\"entities\":[{\"objectClassName\":\"entity\",\"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"\"],[\"tel\",{\"type\":\"voice\"}," +
            "\"uri\",\"tel:+1.9999999999\"],[\"email\",{},\"text\",\"abusecomplaints@example.com\"],[\"adr\",{\"type\":\"work\"},\"text\",[\"\",\"Suite 1234\",\"4321 Rue Somewhere\"," +
            "\"Quebec\",\"QC\",\"G1V 2M2\",\"\"]]]],\"roles\":[\"abuse\"],\"handle\":\"292\"}],\"roles\":[\"registrar\",\"technical\"],\"handle\":\"292\"}]," +
            "\"rdapConformance\":[\"rdap_level_0\",\"icann_rdap_technical_implementation_guide_0\",\"icann_rdap_response_profile_0\"],\"handle\":\"2138514_DOMAIN_COM-EXMP\"," +
            "\"links\":[{\"rel\":\"self\",\"href\":\"https://rdap.example.com/com/v1/domain/EXAMPLE.COM\",\"type\":\"application/rdap+json\"," +
            "\"value\":\"https://rdap.example.com/com/v1/domain/EXAMPLE.COM\"},{\"rel\":\"related\",\"href\":\"https://rdap.markmonitor.com/rdap/domain/EXAMPLE.COM\"," +
            "\"type\":\"application/rdap+json\",\"value\":\"https://rdap.markmonitor.com/rdap/domain/EXAMPLE.COM\"}],\"secureDNS\":{\"dsData\":[{\"keyTag\":55204,\"digestType\":2,\"digest\":\"206D88653C43D99BF4567BBD7DF9C078DB357F59AA183741024D345723052E88\",\"algorithm\":8}],\"delegationSigned\":false},\"events\":[{\"eventAction\":\"registration\"," +
            "\"eventDate\":\"1997-09-15T04:00:00Z\"},{\"eventAction\":\"expiration\",\"eventDate\":\"2028-09-14T04:00:00Z\"},{\"eventAction\":\"last update of RDAP database\",\"eventDate\":\"2021-03-18T09:24:18Z\"}],\"ldhName\":\"EXAMPLE.COM\",\"status\":[\"client delete prohibited\",\"client transfer prohibited\",\"client update prohibited\",\"server delete prohibited\"," +
            "\"server transfer prohibited\",\"server update prohibited\"]}";

    public ResponseValidation2Dot7Dot2_2024Test() {
        super("/validators/domain/valid_registrant.json", "rdapResponseProfile_2_1_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
        config = mock(RDAPValidatorConfiguration.class);
    }

    @Override
    public ProfileValidation getProfileValidation() {
        when(config.isGtldRegistrar()).thenReturn(true);
        return new ResponseValidation2Dot7Dot2_2024(config, RDAPQueryType.DOMAIN, jsonObject.toString(), results);
    }

    @Test
    public void testValidate_RegistrantIsInvalid_AddErrorCode63000() {
        when(config.isGtldRegistrar()).thenReturn(true);
        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("roles");

        roles.remove(0);
        roles.put(0, "registrar");
        roles.put(1, "technical");
        validate(-63000, DOMAIN_PATH, "A domain served by a registrar must have one registrant.");
    }

    @Test
    public void testValidate_noRegistrar_NoErrorsAdded() {
        when(config.isGtldRegistrar()).thenReturn(false);
        validate();
    }
}