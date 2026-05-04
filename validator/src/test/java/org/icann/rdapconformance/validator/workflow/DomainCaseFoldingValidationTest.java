package org.icann.rdapconformance.validator.workflow;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpResponse;
import org.icann.rdapconformance.validator.DNSCacheResolver;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.ValidationTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DomainCaseFoldingValidationTest extends HttpTestingUtils implements ValidationTest {

  private RDAPValidatorResults results;
  private HttpResponse<String> httpsResponse;

  @BeforeMethod
  public void setUp() {
    super.setUp();
    results = mock(RDAPValidatorResults.class); // Mock results for verify() calls
    // Create QueryContext with our mock results instead of using the default one
    queryContext = QueryContext.forTesting("{}", results, config);
    queryContext.setSsrfProtectionEnabled(false);
    httpsResponse = mock(HttpResponse.class);
    doReturn(URI.create("http://domain/test.example")).when(httpsResponse).uri();
  }


  public ProfileValidation getProfileValidation() {
    return new DomainCaseFoldingValidation(httpsResponse, queryContext, RDAPQueryType.DOMAIN);
  }

  @Test
  public void testFoldDomain() {
    DomainCaseFoldingValidation validator = new DomainCaseFoldingValidation(httpsResponse, queryContext, RDAPQueryType.DOMAIN);
    assertThat(validator.foldDomain()).isEqualTo("tEsT.ExAmPlE");
  }

  @Test
  public void testFoldingDigitAndDot() {
    doReturn(URI.create("http://domain/123.example")).when(httpsResponse).uri();
    DomainCaseFoldingValidation validator = new DomainCaseFoldingValidation(httpsResponse, queryContext, RDAPQueryType.DOMAIN);
    assertThat(validator.foldDomain()).isEqualTo("123.eXaMpLe");
  }

  @Test
  public void testFoldingCharNotFoldeable() {
    doReturn(URI.create("http://domain/test.国xample")).when(httpsResponse).uri();
    DomainCaseFoldingValidation validator = new DomainCaseFoldingValidation(httpsResponse, queryContext, RDAPQueryType.DOMAIN);
    assertThat(validator.foldDomain()).isEqualTo("tEsT.国xAmPlE");
  }

  @Test
  public void testDoValidate_EntitiesInDifferentOrder_ShouldPass() throws Exception {
    WireMockConfiguration wmConfig = wireMockConfig()
            .dynamicHttpsPort()
            .bindAddress(WIREMOCK_HOST);
    prepareWiremock(wmConfig);

    String originalResponse = "{\n"
            + "  \"objectClassName\": \"domain\",\n"
            + "  \"ldhName\": \"test.example\",\n"
            + "  \"entities\": [\n"
            + "    {\"objectClassName\": \"entity\", \"handle\": \"REG-001\", \"roles\": [\"registrant\"]},\n"
            + "    {\"objectClassName\": \"entity\", \"handle\": \"ADM-001\", \"roles\": [\"administrative\"]},\n"
            + "    {\"objectClassName\": \"entity\", \"handle\": \"REGT-001\", \"roles\": [\"registrar\"]}\n"
            + "  ]\n"
            + "}";

    // Same data but entities in different order
    String caseFoldedResponse = "{\n"
            + "  \"objectClassName\": \"domain\",\n"
            + "  \"ldhName\": \"test.example\",\n"
            + "  \"entities\": [\n"
            + "    {\"objectClassName\": \"entity\", \"handle\": \"REGT-001\", \"roles\": [\"registrar\"]},\n"
            + "    {\"objectClassName\": \"entity\", \"handle\": \"REG-001\", \"roles\": [\"registrant\"]},\n"
            + "    {\"objectClassName\": \"entity\", \"handle\": \"ADM-001\", \"roles\": [\"administrative\"]}\n"
            + "  ]\n"
            + "}";

    givenUri("http");
    doReturn(config.getUri()).when(httpsResponse).uri();
    doReturn(200).when(httpsResponse).statusCode();
    doReturn(originalResponse).when(httpsResponse).body();

    String caseFoldedPath = "/domain/tEsT.ExAmPlE";
    stubFor(get(urlEqualTo(caseFoldedPath))
            .withScheme("http")
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/rdap+json")
                    .withBody(caseFoldedResponse)));

    // Should PASS — entities are the same, just in different order
    validateOk(results);
  }

  @Test
  public void testDoValidate_RealWorldSnapnamesCaseFolding_ShouldPass() throws Exception {
    WireMockConfiguration wmConfig = wireMockConfig()
            .dynamicHttpsPort()
            .bindAddress(WIREMOCK_HOST);
    prepareWiremock(wmConfig);

    // (entities: registrar, registrant, technical — redacted: registrant first)
    String mixedCaseResponse = "{\n"
            + "\"entities\": [\n"
            + "  {\"handle\":\"270\",\"objectClassName\":\"entity\",\"roles\":[\"registrar\"],\n"
            + "   \"entities\":[{\"objectClassName\":\"entity\",\"roles\":[\"abuse\"],\"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Address Creation, LLC\"],[\"email\",{},\"text\",\"domain.operations@web.com\"],[\"tel\",{\"type\":\"voice\"},\"text\",\"+1.8777228662\"]]]}],\n"
            + "   \"links\":[{\"href\":\"https://rdap.snapnames.com/rdap/\",\"rel\":\"about\",\"type\":\"text/html\",\"value\":\"https://rdap.snapnames.com/rdap/\"}],\n"
            + "   \"publicIds\":[{\"identifier\":\"270\",\"type\":\"IANA Registrar ID\"}],\n"
            + "   \"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Address Creation, LLC\"]]]},\n"
            + "  {\"handle\":\" \",\"objectClassName\":\"entity\",\"roles\":[\"registrant\"],\n"
            + "   \"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"NewFold Digital, Inc.\"],[\"org\",{},\"text\",\"NewFold Digital, Inc.\"],[\"email\",{},\"text\",\"corpdomains@newfold.com\"],[\"tel\",{\"type\":\"voice\"},\"text\",\"+1.9046806600\"],[\"adr\",{\"cc\":\"US\"},\"text\",[\"\",\"\",\"5335 Gate Parkway\",\"Jacksonville\",\"FL\",\"32256\",\"\"]]]]},\n"
            + "  {\"handle\":\" \",\"objectClassName\":\"entity\",\"roles\":[\"technical\"],\n"
            + "   \"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"NewFold Digital, Inc.\"],[\"email\",{},\"text\",\"corpdomains@newfold.com\"],[\"tel\",{\"type\":\"voice\"},\"text\",\"+1.9046806600\"],[\"adr\",{},\"text\",[\"\",\"\",\"\",\"\",\"\",\"\",\"\"]]]]}\n"
            + "],\n"
            + "\"objectClassName\":\"domain\",\"handle\":\"28766829_DOMAIN_COM-VRSN\",\"ldhName\":\"addresscreation.com.\",\n"
            + "\"notices\":[{\"title\":\"Status Codes\",\"description\":[\"For more information on domain status codes, please visit https://icann.org/epp\"],\"links\":[{\"href\":\"https://icann.org/epp\",\"rel\":\"glossary\",\"type\":\"text/html\",\"value\":\"https://rdap.snapnames.com/rdap/domain/aDdReSsCrEaTiOn.cOm\"}]}],\n"
            + "\"redacted\":[{\"method\":\"removal\",\"name\":{\"description\":\"registrant Handle\"},\"pathLang\":\"jsonpath\",\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].handle\",\"reason\":{\"description\":\"Server policy\"}},{\"method\":\"removal\",\"name\":{\"description\":\"technical Handle\"},\"pathLang\":\"jsonpath\",\"postPath\":\"$.entities[?(@.roles[0]=='technical')].handle\",\"reason\":{\"description\":\"Server policy\"}}]\n"
            + "}";

    // (entities: registrar, technical, registrant — redacted: technical first)
    String normalResponse = "{\n"
            + "\"entities\": [\n"
            + "  {\"handle\":\"270\",\"objectClassName\":\"entity\",\"roles\":[\"registrar\"],\n"
            + "   \"entities\":[{\"objectClassName\":\"entity\",\"roles\":[\"abuse\"],\"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Address Creation, LLC\"],[\"email\",{},\"text\",\"domain.operations@web.com\"],[\"tel\",{\"type\":\"voice\"},\"text\",\"+1.8777228662\"]]]}],\n"
            + "   \"links\":[{\"href\":\"https://rdap.snapnames.com/rdap/\",\"rel\":\"about\",\"type\":\"text/html\",\"value\":\"https://rdap.snapnames.com/rdap/\"}],\n"
            + "   \"publicIds\":[{\"identifier\":\"270\",\"type\":\"IANA Registrar ID\"}],\n"
            + "   \"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Address Creation, LLC\"]]]},\n"
            + "  {\"handle\":\" \",\"objectClassName\":\"entity\",\"roles\":[\"technical\"],\n"
            + "   \"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"NewFold Digital, Inc.\"],[\"email\",{},\"text\",\"corpdomains@newfold.com\"],[\"tel\",{\"type\":\"voice\"},\"text\",\"+1.9046806600\"],[\"adr\",{},\"text\",[\"\",\"\",\"\",\"\",\"\",\"\",\"\"]]]]},\n"
            + "  {\"handle\":\" \",\"objectClassName\":\"entity\",\"roles\":[\"registrant\"],\n"
            + "   \"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"NewFold Digital, Inc.\"],[\"org\",{},\"text\",\"NewFold Digital, Inc.\"],[\"email\",{},\"text\",\"corpdomains@newfold.com\"],[\"tel\",{\"type\":\"voice\"},\"text\",\"+1.9046806600\"],[\"adr\",{\"cc\":\"US\"},\"text\",[\"\",\"\",\"5335 Gate Parkway\",\"Jacksonville\",\"FL\",\"32256\",\"\"]]]]}\n"
            + "],\n"
            + "\"objectClassName\":\"domain\",\"handle\":\"28766829_DOMAIN_COM-VRSN\",\"ldhName\":\"addresscreation.com.\",\n"
            + "\"notices\":[{\"title\":\"Status Codes\",\"description\":[\"For more information on domain status codes, please visit https://icann.org/epp\"],\"links\":[{\"href\":\"https://icann.org/epp\",\"rel\":\"glossary\",\"type\":\"text/html\",\"value\":\"https://rdap.snapnames.com/rdap/domain/addresscreation.com\"}]}],\n"
            + "\"redacted\":[{\"method\":\"removal\",\"name\":{\"description\":\"technical Handle\"},\"pathLang\":\"jsonpath\",\"postPath\":\"$.entities[?(@.roles[0]=='technical')].handle\",\"reason\":{\"description\":\"Server policy\"}},{\"method\":\"removal\",\"name\":{\"description\":\"registrant Handle\"},\"pathLang\":\"jsonpath\",\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].handle\",\"reason\":{\"description\":\"Server policy\"}}]\n"
            + "}";

    doReturn(URI.create("http://127.0.0.1:8080/domain/addresscreation.com.")).when(httpsResponse).uri();
    givenUri("http", "/domain/addresscreation.com.");
    doReturn(config.getUri()).when(httpsResponse).uri();
    doReturn(200).when(httpsResponse).statusCode();
    doReturn(normalResponse).when(httpsResponse).body();

    String caseFoldedPath = "/domain/aDdReSsCrEaTiOn.cOm.";
    stubFor(get(urlEqualTo(caseFoldedPath))
            .withScheme("http")
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/rdap+json")
                    .withBody(mixedCaseResponse)));

    validateOk(results);
  }

  @Test
  public void testNrLdhLabel() {
    WireMockConfiguration wmConfig = wireMockConfig()
        .dynamicHttpsPort()
        .bindAddress(WIREMOCK_HOST);
    prepareWiremock(wmConfig);

    // Use real DNS resolution for test - domain case folding doesn't depend on DNS mocking

      givenUri("http");
    doReturn(config.getUri()).when(httpsResponse).uri();
    doReturn(RDAP_RESPONSE).when(httpsResponse).body();
      givenUriWithDifferentResponse("/domain/tEsT.ExAmPlE");

    validateNotOk(results,
          -10403, "http://127.0.0.1:8080/domain/tEsT.ExAmPlE",
          "RDAP responses do not match when handling domain label case folding.");
  }

  @Test
  public void testDoLaunch() {
    assertThat(getProfileValidation().doLaunch()).isTrue();
    assertThat(
        new DomainCaseFoldingValidation(
            httpsResponse,
            queryContext,
            RDAPQueryType.NAMESERVER).doLaunch()).isFalse();
  }

  private void givenUriWithDifferentResponse(String path) {
    givenUri("http", path);
    stubFor(get(urlEqualTo(path))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody("{\"objectClassName\": \"domain\", \"unexpected\":\"property\"}")));
  }

  @Test
  public void testFoldDomain_NonFoldableCharacters() {
    doReturn(URI.create("http://domain/test.123")).when(httpsResponse).uri();
    DomainCaseFoldingValidation validator = new DomainCaseFoldingValidation(httpsResponse, queryContext, RDAPQueryType.DOMAIN);
    
    String folded = validator.foldDomain();
    assertThat(folded).isEqualTo("tEsT.123");
  }

  @Test
  public void testFoldDomain_EmptyDomain() {
    doReturn(URI.create("http://domain/")).when(httpsResponse).uri();
    DomainCaseFoldingValidation validator = new DomainCaseFoldingValidation(httpsResponse, queryContext, RDAPQueryType.DOMAIN);
    
    String folded = validator.foldDomain();
    assertThat(folded).isEmpty();
  }

  @Test
  public void testFoldDomain_SingleCharacter() {
    doReturn(URI.create("http://domain/a")).when(httpsResponse).uri();
    DomainCaseFoldingValidation validator = new DomainCaseFoldingValidation(httpsResponse, queryContext, RDAPQueryType.DOMAIN);
    
    String folded = validator.foldDomain();
    assertThat(folded).isEqualTo("a");
  }

  @Test
  public void testFoldDomain_AlternatesCase() {
    doReturn(URI.create("http://domain/abcd")).when(httpsResponse).uri();
    DomainCaseFoldingValidation validator = new DomainCaseFoldingValidation(httpsResponse, queryContext, RDAPQueryType.DOMAIN);
    
    String folded = validator.foldDomain();
    assertThat(folded).isEqualTo("aBcD");
  }

  @Test
  public void testRedirectHandling_SingleRedirect() throws Exception {
    WireMockConfiguration wmConfig = wireMockConfig()
        .dynamicHttpsPort()
        .bindAddress(WIREMOCK_HOST);
    prepareWiremock(wmConfig);

    // Use real DNS resolution for test - domain case folding doesn't depend on DNS mocking

    //Setup: Original response returns 200 OK
      givenUri("http");
    doReturn(config.getUri()).when(httpsResponse).uri();
    doReturn(200).when(httpsResponse).statusCode();
    doReturn(RDAP_RESPONSE).when(httpsResponse).body();

    //Setup: Case-folded URL returns 301 redirect, then final URL returns same content
    String caseFoldedPath = "/domain/tEsT.ExAmPlE";
    String finalPath = "/domain/test.example";
      
    //Step 1: Case-folded request returns redirect
    stubFor(get(urlEqualTo(caseFoldedPath))
          .withScheme("http")
          .willReturn(aResponse()
              .withStatus(301)
              .withHeader("Location", "http://127.0.0.1:" + wireMockServer.port() + finalPath)
              .withHeader("Content-Type", "application/rdap+json")));
      
    //Step 2: Final redirect target returns same content as original
    stubFor(get(urlEqualTo(finalPath))
          .withScheme("http")
          .willReturn(aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/rdap+json")
              .withBody(RDAP_RESPONSE)));

    //This should now PASS (not fail) because redirects are followed
    validateOk(results);
  }

  @Test
  public void testRedirectHandling_CrossHostRedirectRejected() throws Exception {
    WireMockConfiguration wmConfig = wireMockConfig()
        .dynamicHttpsPort()
        .bindAddress(WIREMOCK_HOST);
    prepareWiremock(wmConfig);

    // Use real DNS resolution for test - domain case folding doesn't depend on DNS mocking

    givenUri("http");
    doReturn(config.getUri()).when(httpsResponse).uri();
    doReturn(200).when(httpsResponse).statusCode();
    doReturn(RDAP_RESPONSE).when(httpsResponse).body();

    //Setup: Case-folded URL returns cross-host redirect (should be rejected)
    String caseFoldedPath = "/domain/tEsT.ExAmPlE";
    stubFor(get(urlEqualTo(caseFoldedPath))
          .withScheme("http")
          .willReturn(aResponse()
              .withStatus(301)
              .withHeader("Location", "http://evil.example.com/domain/test.example")
              .withHeader("Content-Type", "application/rdap+json")));

    //Should fail because cross-host redirect is rejected and status codes don't match
    validateNotOk(results,
          -10403, "http://127.0.0.1:8080/domain/tEsT.ExAmPlE",
          "RDAP responses do not match when handling domain label case folding.");
  }

  @Test
  public void testRedirectHandling_NoLocationHeader() throws Exception {
    WireMockConfiguration wmConfig = wireMockConfig()
        .dynamicHttpsPort()
        .bindAddress(WIREMOCK_HOST);
    prepareWiremock(wmConfig);

    // Use real DNS resolution for test - domain case folding doesn't depend on DNS mocking

      givenUri("http");
    doReturn(config.getUri()).when(httpsResponse).uri();
    doReturn(200).when(httpsResponse).statusCode();
    doReturn(RDAP_RESPONSE).when(httpsResponse).body();

    //Setup: Case-folded URL returns 301 but no Location header
    String caseFoldedPath = "/domain/tEsT.ExAmPlE";
    stubFor(get(urlEqualTo(caseFoldedPath))
          .withScheme("http")
          .willReturn(aResponse()
              .withStatus(301)
              .withHeader("Content-Type", "application/rdap+json")
              .withBody("Moved")));

    //Should fail because redirect cannot be followed and status codes don't match
    validateNotOk(results,
          -10403, "http://127.0.0.1:8080/domain/tEsT.ExAmPlE",
          "RDAP responses do not match when handling domain label case folding.");
  }

  @Test
  public void testRedirectHandling_MultipleRedirects() throws Exception {
    WireMockConfiguration wmConfig = wireMockConfig()
        .dynamicHttpsPort()
        .bindAddress(WIREMOCK_HOST);
    prepareWiremock(wmConfig);

    // Use real DNS resolution for test - domain case folding doesn't depend on DNS mocking

      givenUri("http");
    doReturn(config.getUri()).when(httpsResponse).uri();
    doReturn(200).when(httpsResponse).statusCode();
    doReturn(RDAP_RESPONSE).when(httpsResponse).body();

    //Setup chain: caseFolded -> intermediate -> final
    String caseFoldedPath = "/domain/tEsT.ExAmPlE";
    String intermediatePath = "/domain/temp.example";
    String finalPath = "/domain/test.example";
      
    //Step 1: Case-folded -> intermediate
    stubFor(get(urlEqualTo(caseFoldedPath))
          .withScheme("http")
          .willReturn(aResponse()
              .withStatus(302)
              .withHeader("Location", "http://127.0.0.1:" + wireMockServer.port() + intermediatePath)));
      
    //Step 2: Intermediate -> final
    stubFor(get(urlEqualTo(intermediatePath))
          .withScheme("http")
          .willReturn(aResponse()
              .withStatus(301)
              .withHeader("Location", "http://127.0.0.1:" + wireMockServer.port() + finalPath)));
      
    //Step 3: Final destination
    stubFor(get(urlEqualTo(finalPath))
          .withScheme("http")
          .willReturn(aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/rdap+json")
              .withBody(RDAP_RESPONSE)));

    //Should pass because multiple redirects are followed (up to max limit)
    validateOk(results);
  }

  @Test
  public void testGetGroupName() {
    DomainCaseFoldingValidation validator = new DomainCaseFoldingValidation(httpsResponse, queryContext, RDAPQueryType.DOMAIN);
    
    assertThat(validator.getGroupName()).isEqualTo("domainCaseFoldingValidation");
  }

  @Test
  public void testDoValidate_SameDomain_ReturnsTrue() throws Exception {
    // Test when foldDomain returns the same domain (no case folding possible)
    doReturn(URI.create("http://domain/123.456")).when(httpsResponse).uri();
    DomainCaseFoldingValidation validator = new DomainCaseFoldingValidation(httpsResponse, queryContext, RDAPQueryType.DOMAIN);
    
    boolean result = validator.doValidate();
    
    assertThat(result).isTrue();
  }

  @Test
  public void testDoLaunch_NonDomainQuery_ReturnsFalse() {
    DomainCaseFoldingValidation validator = new DomainCaseFoldingValidation(
        httpsResponse, queryContext, RDAPQueryType.ENTITY);
    
    assertThat(validator.doLaunch()).isFalse();
  }

  @Test
  public void testDoLaunch_DomainQuery_ReturnsTrue() {
    DomainCaseFoldingValidation validator = new DomainCaseFoldingValidation(
        httpsResponse, queryContext, RDAPQueryType.DOMAIN);
    
    assertThat(validator.doLaunch()).isTrue();
  }

  @Test
  public void testDoValidate_DifferentLastUpdateEvent_ShouldPass() throws Exception {
    WireMockConfiguration wmConfig = wireMockConfig()
            .dynamicHttpsPort()
            .bindAddress(WIREMOCK_HOST);
    prepareWiremock(wmConfig);

    String originalResponse = "{\n"
            + "  \"objectClassName\": \"domain\",\n"
            + "  \"ldhName\": \"test.example\",\n"
            + "  \"events\": [\n"
            + "    {\n"
            + "      \"eventAction\": \"registration\",\n"
            + "      \"eventDate\": \"2020-01-01T00:00:00Z\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"eventAction\": \"last update of RDAP database\",\n"
            + "      \"eventDate\": \"2025-01-01T00:00:00Z\"\n"
            + "    }\n"
            + "  ]\n"
            + "}";

    String caseFoldedResponse = "{\n"
            + "  \"objectClassName\": \"domain\",\n"
            + "  \"ldhName\": \"test.example\",\n"
            + "  \"events\": [\n"
            + "    {\n"
            + "      \"eventAction\": \"registration\",\n"
            + "      \"eventDate\": \"2020-01-01T00:00:00Z\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"eventAction\": \"last update of RDAP database\",\n"
            + "      \"eventDate\": \"2025-06-15T12:30:00Z\"\n"
            + "    }\n"
            + "  ]\n"
            + "}";

    givenUri("http");
    doReturn(config.getUri()).when(httpsResponse).uri();
    doReturn(200).when(httpsResponse).statusCode();
    doReturn(originalResponse).when(httpsResponse).body();

    // Case-folded URL returns same content except different "last update of RDAP database" date
    String caseFoldedPath = "/domain/tEsT.ExAmPlE";
    stubFor(get(urlEqualTo(caseFoldedPath))
            .withScheme("http")
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/rdap+json")
                    .withBody(caseFoldedResponse)));

    // Should PASS — the only difference is the "last update of RDAP database" event which must be ignored
    validateOk(results);
  }

  @Test
  public void testDoValidate_DifferentNonLastUpdateEvent_ShouldFail() throws Exception {
    WireMockConfiguration wmConfig = wireMockConfig()
            .dynamicHttpsPort()
            .bindAddress(WIREMOCK_HOST);
    prepareWiremock(wmConfig);

    String originalResponse = "{\n"
            + "  \"objectClassName\": \"domain\",\n"
            + "  \"ldhName\": \"test.example\",\n"
            + "  \"events\": [\n"
            + "    {\n"
            + "      \"eventAction\": \"registration\",\n"
            + "      \"eventDate\": \"2020-01-01T00:00:00Z\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"eventAction\": \"last update of RDAP database\",\n"
            + "      \"eventDate\": \"2025-01-01T00:00:00Z\"\n"
            + "    }\n"
            + "  ]\n"
            + "}";

    String caseFoldedResponse = "{\n"
            + "  \"objectClassName\": \"domain\",\n"
            + "  \"ldhName\": \"test.example\",\n"
            + "  \"events\": [\n"
            + "    {\n"
            + "      \"eventAction\": \"registration\",\n"
            + "      \"eventDate\": \"2022-05-05T00:00:00Z\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"eventAction\": \"last update of RDAP database\",\n"
            + "      \"eventDate\": \"2025-01-01T00:00:00Z\"\n"
            + "    }\n"
            + "  ]\n"
            + "}";

    givenUri("http");
    doReturn(config.getUri()).when(httpsResponse).uri();
    doReturn(200).when(httpsResponse).statusCode();
    doReturn(originalResponse).when(httpsResponse).body();

    String caseFoldedPath = "/domain/tEsT.ExAmPlE";
    stubFor(get(urlEqualTo(caseFoldedPath))
            .withScheme("http")
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/rdap+json")
                    .withBody(caseFoldedResponse)));
    String expectedUrl = config.getUri().resolve(caseFoldedPath).toString();

    // Should FAIL — the "registration" event date differs, which is NOT ignored
    validateNotOk(results,
            -10403, expectedUrl,
            "RDAP responses do not match when handling domain label case folding.");
  }

}