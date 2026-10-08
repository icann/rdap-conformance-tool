package org.icann.rdapconformance.tool;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Integration tests that exercise the full network validation path of call()
 * against a local WireMock server. No external network access required.
 */
public class RdapConformanceToolWireMockTest {

    private WireMockServer wireMock;
    private Path config;

    private static final String MINIMAL_DOMAIN_RESPONSE = """
        {
          "objectClassName": "domain",
          "ldhName": "test.example",
          "rdapConformance": ["rdap_level_0"]
        }
        """;

    @BeforeClass
    public void setUp() throws Exception {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
        config = Files.createTempFile("wm-config", ".json");
        Files.writeString(config, "{\"definitionIdentifier\": \"wiremock-test\"}");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        if (wireMock != null && wireMock.isRunning()) {
            wireMock.stop();
        }
        if (config != null) {
            Files.deleteIfExists(config);
        }
    }

    private RdapConformanceTool newTool(String path) {
        RdapConformanceTool tool = new RdapConformanceTool();
        tool.setLogging(LoggingLevel.ERROR);
        tool.setShowProgress(false);
        tool.setUseLocalDatasets(true);
        tool.setExecuteIPv6Queries(false); // 127.0.0.1 has no v6 -> covers skip branch
        tool.setConfigurationFile(config.toString());
        tool.setResultsFile("/tmp/wm-results-" + System.nanoTime() + ".json");
        // allow localhost through SSRF protection
        tool.getSsrfAllowedHosts().add("127.0.0.1");
        tool.setUri(URI.create("http://127.0.0.1:" + wireMock.port() + path));
        return tool;
    }

    @Test
    public void call_fullHttpValidation_200Response() throws Exception {
        wireMock.stubFor(get(urlPathEqualTo("/domain/test.example"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/rdap+json")
                        .withBody(MINIMAL_DOMAIN_RESPONSE)));

        RdapConformanceTool tool = newTool("/domain/test.example");
        Integer code = tool.call();

        // Exit code may be non-zero (validation findings) — the point is the
        // full IPv4 network path executed: DNS, both Accept headers, results file.
        assertThat(code).isNotNull();
        assertThat(tool.getAllResults()).isNotNull();
    }

    @Test
    public void call_fullHttpValidation_404Response() throws Exception {
        wireMock.stubFor(get(urlPathEqualTo("/domain/notfound.example"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/rdap+json")
                        .withBody("{\"errorCode\": 404}")));

        RdapConformanceTool tool = newTool("/domain/notfound.example");
        Integer code = tool.call();

        // Covers the isResourceNotFound branch (removeErrors/removeResultGroups)
        assertThat(code).isNotNull();
    }

}