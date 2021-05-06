package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.workflow.profile.ProfileValidationTestBase.validateNotOk;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Comparator;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot2.RDAPJsonComparator;
import org.icann.rdapconformance.validator.workflow.rdap.HttpTestingUtils;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Validation1Dot2Test extends HttpTestingUtils {

  private RDAPValidatorResults results;

  @BeforeMethod
  public void setUp() {
    super.setUp();
    results = mock(RDAPValidatorResults.class);
  }

  @Test
  public void testValidate_UriNotHttps_AddResult20100() {
    HttpResponse<String> httpsResponse = mock(HttpResponse.class);

    doReturn(URI.create("http://domain/test.example")).when(config).getUri();
    doReturn(config.getUri()).when(httpsResponse).uri();

    validateNotOk(new Validation1Dot2(httpsResponse, config, results), results,
        -20100, config.getUri().toString(),
        "The URL is HTTP, per section 1.2 of the RDAP_Technical_Implementation_Guide_2_1 shall be HTTPS only.");
  }

  @Test
  public void testValidate_UriNotHttpsInOneRedirect_AddResult20100() {
    RedirectData redirectData = givenChainedHttpRedirects();

    // set URI as being an HTTP request to avoid going through HTTP test for code -20101
    doReturn(URI.create("http://domain/test.example")).when(config).getUri();

    validateNotOk(new Validation1Dot2(redirectData.startingResponse, config, results), results,
        -20100, redirectData.endingResponse.uri().toString(),
        "The URL is HTTP, per section 1.2 of the RDAP_Technical_Implementation_Guide_2_1 shall be HTTPS only.");
  }

  @Test
  public void testValidate_HttpResponseEqualsHttpsResponse_AddResult20101() {
    HttpResponse<String> httpsResponse = mock(HttpResponse.class);

    WireMockConfiguration wmConfig = wireMockConfig()
        .dynamicHttpsPort()
        .bindAddress(WIREMOCK_HOST);
    prepareWiremock(wmConfig);

    // configure wiremock for HTTP as we will make an HTTP request
    givenUri("http");
    doReturn(RDAP_RESPONSE).when(httpsResponse).body();
    // replace configuration URI by https because we need to enter the tests for an HTTPS uri
    doReturn(URI.create(config.getUri().toString().replace("http", "https"))).when(config).getUri();
    doReturn(config.getUri()).when(httpsResponse).uri();
    stubFor(get(urlEqualTo(REQUEST_PATH))
        .withScheme("http")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/rdap+JSON;encoding=UTF-8")
            .withBody(RDAP_RESPONSE)));

    validateNotOk(new Validation1Dot2(httpsResponse, config, results), results,
        -20101, RDAP_RESPONSE + "\n/\n" + RDAP_RESPONSE,
        "The RDAP response was provided over HTTP, per section 1.2 of the "
            + "RDAP_Technical_Implementation_Guide_2_1shall be HTTPS only.");
  }


  @Test
  public void testRDAPJsonComparator_WithUnorderedListExceptVCardAndDifferentLastUpdate_IsEqual()
      throws JsonProcessingException {
    String rdap1 = "{\n"
        + "  \"notices\": [\n"
        + "    {\n"
        + "      \"title\": \"Status Codes\",\n"
        + "      \"links\": [\n"
        + "        {\n"
        + "          \"rel\": \"related\",\n"
        + "          \"href\": \"https://icann.org/epp\"\n"
        + "        }\n"
        + "      ]\n"
        + "    },\n"
        + "    {\n"
        + "      \"title\": \"RDDS Inaccuracy Complaint Form\",\n"
        + "      \"links\": [\n"
        + "        {\n"
        + "          \"rel\": \"related\",\n"
        + "          \"href\": \"https://www.icann.org/wicf\"\n"
        + "        }\n"
        + "      ]\n"
        + "    }\n"
        + "  ],\n"
        + "  \"nameservers\": [\n"
        + "    {\n"
        + "      \"handle\": \"DEF-LRMS\",\n"
        + "      \"status\": [\n"
        + "        \"active\",\n"
        + "        \"associated\"\n"
        + "      ],\n"
        + "      \"events\": [\n"
        + "        {\n"
        + "          \"eventAction\": \"registration\",\n"
        + "          \"eventDate\": \"2017-06-05T12:03:04.000Z\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"eventAction\": \"last changed\",\n"
        + "          \"eventDate\": \"2017-06-10T15:41:57.357Z\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"eventAction\": \"last update of RDAP database\",\n"
        + "          \"eventDate\": \"2021-04-09T16:51:22.976Z\"\n"
        + "        }\n"
        + "      ]\n"
        + "    },\n"
        + "    {\n"
        + "      \"handle\": \"ABC-LRMS\"\n"
        + "    }\n"
        + "  ],\n"
        + "  \"events\": [\n"
        + "    {\n"
        + "      \"eventAction\": \"registration\",\n"
        + "      \"eventDate\": \"2016-10-22T20:47:28.100Z\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"eventAction\": \"last changed\",\n"
        + "      \"eventDate\": \"2020-08-21T21:57:42.127Z\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"eventAction\": \"last update of RDAP database\",\n"
        + "      \"eventDate\": \"2021-04-09T16:51:22.976Z\"\n"
        + "    }\n"
        + "  ],\n"
        + "  \"entities\": [\n"
        + "    {\n"
        + "      \"vcardArray\": [\n"
        + "        \"vcard\",\n"
        + "        [\n"
        + "          [\n"
        + "            \"version\",\n"
        + "            {},\n"
        + "            \"text\",\n"
        + "            \"4.0\"\n"
        + "          ]\n"
        + "        ]\n"
        + "      ]\n"
        + "    }\n"
        + "  ]\n"
        + "}\n";
    String rdap2 = "{\n"
        + "  \"notices\": [\n"
        + "    {\n"
        + "      \"title\": \"RDDS Inaccuracy Complaint Form\",\n"
        + "      \"links\": [\n"
        + "        {\n"
        + "          \"rel\": \"related\",\n"
        + "          \"href\": \"https://www.icann.org/wicf\"\n"
        + "        }\n"
        + "      ]\n"
        + "    },\n"
        + "    {\n"
        + "      \"title\": \"Status Codes\",\n"
        + "      \"links\": [\n"
        + "        {\n"
        + "          \"rel\": \"related\",\n"
        + "          \"href\": \"https://icann.org/epp\"\n"
        + "        }\n"
        + "      ]\n"
        + "    }\n"
        + "  ],\n"
        + "  \"nameservers\": [\n"
        + "    {\n"
        + "      \"handle\": \"DEF-LRMS\",\n"
        + "      \"status\": [\n"
        + "        \"associated\",\n"
        + "        \"active\"\n"
        + "      ],\n"
        + "      \"events\": [\n"
        + "        {\n"
        + "          \"eventAction\": \"last changed\",\n"
        + "          \"eventDate\": \"2017-06-10T15:41:57.357Z\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"eventAction\": \"last update of RDAP database\",\n"
        + "          \"eventDate\": \"2022-04-09T16:51:22.976Z\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"eventAction\": \"registration\",\n"
        + "          \"eventDate\": \"2017-06-05T12:03:04.000Z\"\n"
        + "        }\n"
        + "      ]\n"
        + "    },\n"
        + "    {\n"
        + "      \"handle\": \"ABC-LRMS\"\n"
        + "    }\n"
        + "  ],\n"
        + "  \"events\": [\n"
        + "    {\n"
        + "      \"eventAction\": \"registration\",\n"
        + "      \"eventDate\": \"2016-10-22T20:47:28.100Z\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"eventAction\": \"last update of RDAP database\",\n"
        + "      \"eventDate\": \"2022-04-09T16:51:22.976Z\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"eventAction\": \"last changed\",\n"
        + "      \"eventDate\": \"2020-08-21T21:57:42.127Z\"\n"
        + "    }\n"
        + "  ],\n"
        + "  \"entities\": [\n"
        + "    {\n"
        + "      \"vcardArray\": [\n"
        + "        \"vcard\",\n"
        + "        [\n"
        + "          [\n"
        + "            \"version\",\n"
        + "            {},\n"
        + "            \"text\",\n"
        + "            \"4.0\"\n"
        + "          ]\n"
        + "        ]\n"
        + "      ]\n"
        + "    }\n"
        + "  ]\n"
        + "}\n";

    ObjectMapper mapper = new ObjectMapper();
    Comparator<JsonNode> rdapJsonComparator = new RDAPJsonComparator();
    assertThat(rdapJsonComparator.compare(mapper.readTree(rdap1), mapper.readTree(rdap2)))
        .isEqualTo(0);
  }

  @Test
  public void testRDAPJsonComparator_WithDifference_IsNotOk() throws JsonProcessingException {
    String rdap1 = "{\n"
        + "  \"nameservers\": [\n"
        + "    {\n"
        + "      \"handle\": \"DEF-LRMS\",\n"
        + "      \"status\": [\n"
        + "        \"active\",\n"
        + "        \"associated\"\n"
        + "      ],\n"
        + "      \"events\": [\n"
        + "        {\n"
        + "          \"eventAction\": \"registration\",\n"
        + "          \"eventDate\": \"2017-06-05T12:03:04.000Z\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"eventAction\": \"last changed\",\n"
        + "          \"eventDate\": \"2017-06-10T15:41:57.357Z\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"eventAction\": \"last update of RDAP database\",\n"
        + "          \"eventDate\": \"2021-04-09T16:51:22.976Z\"\n"
        + "        }\n"
        + "      ]\n"
        + "    },\n"
        + "    {\n"
        + "      \"handle\": \"ABC-LRMS\"\n"
        + "    }\n"
        + "  ]\n"
        + "}\n";
    String rdap2 = "{\n"
        + "  \"nameservers\": [\n"
        + "    {\n"
        + "      \"handle\": \"GHI-LRMS\",\n"
        + "      \"status\": [\n"
        + "        \"active\",\n"
        + "        \"associated\"\n"
        + "      ],\n"
        + "      \"events\": [\n"
        + "        {\n"
        + "          \"eventAction\": \"registration\",\n"
        + "          \"eventDate\": \"2017-06-05T12:03:04.000Z\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"eventAction\": \"last changed\",\n"
        + "          \"eventDate\": \"2017-06-10T15:41:57.357Z\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"eventAction\": \"last update of RDAP database\",\n"
        + "          \"eventDate\": \"2021-04-09T16:51:22.976Z\"\n"
        + "        }\n"
        + "      ]\n"
        + "    },\n"
        + "    {\n"
        + "      \"handle\": \"ABC-LRMS\"\n"
        + "    }\n"
        + "  ]\n"
        + "}\n";

    ObjectMapper mapper = new ObjectMapper();
    Comparator<JsonNode> rdapJsonComparator = new RDAPJsonComparator();
    assertThat(rdapJsonComparator.compare(mapper.readTree(rdap1), mapper.readTree(rdap2)))
        .isEqualTo(1);
  }

  @Test
  public void testRDAPJsonComparator_WithUnorderedVCardArray_IsNotEqual()
      throws JsonProcessingException {
    String rdap1 = "{\n"
        + "  \"entities\": [\n"
        + "    {\n"
        + "      \"vcardArray\": [\n"
        + "        \"vcard\",\n"
        + "        [\n"
        + "          [\n"
        + "            \"version\",\n"
        + "            {},\n"
        + "            \"text\",\n"
        + "            \"4.0\"\n"
        + "          ]\n"
        + "        ]\n"
        + "      ]\n"
        + "    }\n"
        + "  ]\n"
        + "}\n";
    String rdap2 = "{\n"
        + "  \"entities\": [\n"
        + "    {\n"
        + "      \"vcardArray\": [\n"
        + "        \"vcard\",\n"
        + "        [\n"
        + "          [\n"
        + "            \"text\",\n"
        + "            {},\n"
        + "            \"version\",\n"
        + "            \"4.0\"\n"
        + "          ]\n"
        + "        ]\n"
        + "      ]\n"
        + "    }\n"
        + "  ]\n"
        + "}\n";

    ObjectMapper mapper = new ObjectMapper();
    Comparator<JsonNode> rdapJsonComparator = new RDAPJsonComparator();
    assertThat(rdapJsonComparator.compare(mapper.readTree(rdap1), mapper.readTree(rdap2)))
        .isEqualTo(1);
  }
}
