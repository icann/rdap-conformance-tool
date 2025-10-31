package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseDomainValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot5Dot2Test extends ResponseDomainValidationTestBase {

  protected static final String innerEntities = "\"entities\":[{\"objectClassName\":\"entity\","
      + "\"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"\"],"
      + "[\"tel\",{\"type\":\"voice\"},\"uri\",\"tel:+1.9999999999\"],"
      + "[\"email\",{},\"text\",\"abusecomplaints@example.com\"],"
      + "[\"adr\",{\"type\":\"work\"},\"text\",[\"\",\"Suite 1234\",\"4321 Rue Somewhere\","
      + "\"Quebec\",\"QC\",\"G1V 2M2\",\"\"]]]],\"roles\":[\"abuse\"],\"handle\":\"292\"}],";
  protected static final String entities = "#/entities/0:"
      + "{\"objectClassName\":\"entity\",\"publicIds\":[{\"identifier\":\"292\","
      + "\"type\":\"IANA Registrar ID\"}],\"vcardArray\":[\"vcard\","
      + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Inc.\"]"
      + "%s]],"
      + innerEntities
      + "\"roles\":[\"%s\"],\"handle\":\"292\"}";
  RDAPValidatorConfiguration config;

  public ResponseValidation2Dot7Dot5Dot2Test() {
    super("rdapResponseProfile_2_7_5_2_Validation");
  }

  @DataProvider(name = "roles")
  public static Object[][] roles() {
    return new Object[][]{{"registrant"}, {"administrative"}, {"technical"}, {"billing"}};
  }


  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();

    // Create mock config
    config = mock(RDAPValidatorConfiguration.class);
    doReturn(true).when(config).isGtldRegistrar();

    // Recreate QueryContext with our mocked config
    queryContext = org.icann.rdapconformance.validator.QueryContext.forTesting(rdapContent, results, config);
  }

  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot7Dot5Dot2(queryContext);
  }


  @Test
  public void testDoLaunch() {
    super.testDoLaunch();
    queryContext.setQueryType(RDAPQueryType.DOMAIN);
    doReturn(false).when(config).isGtldRegistrar();
    assertThat(getProfileValidation().doLaunch()).isFalse();
  }

  @Test(dataProvider = "roles")
  public void testValidate_EntityContainsEmail_ok(String role) {
    replaceValue("$['entities'][0]['entities'][0]['roles'][0]", role);
    super.testValidate_ok();
  }

  @Test(dataProvider = "roles")
  public void testValidate_EntityDoesNotContainEmailButContactUriHttp_ok(String role) {
    replaceValue("$['entities'][0]['roles'][0]", role);
    addValue("$['entities'][0]['vcardArray'][1]",
        List.of("contact-uri", new JSONObject(), "uri", "http://test.example"));
    super.testValidate_ok();
  }

  @Test(dataProvider = "roles")
  public void testValidate_EntityDoesNotContainEmailButContactUriHttps_ok(String role) {
    replaceValue("$['entities'][0]['roles'][0]", role);
    addValue("$['entities'][0]['vcardArray'][1]",
        List.of("contact-uri", new JSONObject(), "uri", "https://test.example"));
    super.testValidate_ok();
  }

  @Test(dataProvider = "roles")
  public void testValidate_EntityDoesNotContainEmailButContactUriEmail_ok(String role) {
    replaceValue("$['entities'][0]['roles'][0]", role);
    addValue("$['entities'][0]['vcardArray'][1]",
        List.of("contact-uri", new JSONObject(), "uri", "test@example"));
    super.testValidate_ok();
  }

  @Test(dataProvider = "roles")
  public void testValidate_EntityDoesNotContainEmailNorContactUri_AddResults58000(String role) {
    replaceValue("$['entities'][0]['roles'][0]", role);
    validate(-58000, String.format(entities, "", role), "An entity with the administrative, "
        + "technical, or billing role without a CONTACT-URI member was found. "
        + "See section 2.7.5.2 of the RDAP_Response_Profile_2_1.");
  }

  @Test(dataProvider = "roles")
  public void testValidate_EntityDoesNotContainEmailAndInvalidContactUri_AddResults58001(
      String role) {
    replaceValue("$['entities'][0]['roles'][0]", role);
    addValue("$['entities'][0]['vcardArray'][1]",
        List.of("contact-uri", new JSONObject(), "uri", "abcd"));
    validate(-58001, String.format(entities, ",[\"contact-uri\",{},\"uri\",\"abcd\"]", role),
        "The content of the CONTACT-URI member of an entity with the administrative, "
            + "technical, or billing role does not contain an email or http/https link. "
            + "See section 2.7.5.2 of the RDAP_Response_Profile_2_1.");
  }
}
