package org.icann.rdapconformance.tool;

import org.icann.rdapconformance.validator.QueryContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.spy;
import static org.testng.Assert.*;

public class IPv4v6ValidationSkipTest {

    private static final String TEST_HOST = "rdap.icann.org";
    private static final String TEST_PATH = "/domain/example.com";
    private static final String TEST_URI = "https://" + TEST_HOST + TEST_PATH;
    private static final String CONFIG_JSON = "{\"definitionIdentifier\": \"test\"}";
    private static final int IPV4_ERROR_CODE = -20400;
    private static final int IPV6_ERROR_CODE = -20401;

    @BeforeMethod
    public void setUp() {
        // No setup needed since we get results from tool's QueryContext
    }

    @Test
    public void shouldRecordError20400AndSkipValidation_whenNoIPv4AddressesAvailable() throws Exception {
        try (TempFileResource configFile = new TempFileResource(createTempConfigFile())) {
            RdapConformanceTool tool = spy(new RdapConformanceTool());
            tool.setConfigurationFile(configFile.toString());
            tool.setUri(new URI(TEST_URI));
            tool.setExecuteIPv4Queries(true);
            tool.setExecuteIPv6Queries(false);
            tool.call();

            // Get results from the tool's QueryContext instead of singleton
            QueryContext queryContext = tool.getQueryContext();
            assertNotNull(queryContext, "QueryContext should be available after tool.call()");
            var allResults = queryContext.getResults().getAll();
            assertEquals(1, allResults.size(), "Should have exactly one validation result");
            assertTrue(allResults.stream().anyMatch(r -> r.getCode() == IPV4_ERROR_CODE));
        }
    }

    @Test
    public void shouldRecordError20401AndSkipValidation_whenNoIPv6AddressesAvailable() throws Exception {
        try (TempFileResource configFile = new TempFileResource(createTempConfigFile())) {
            RdapConformanceTool tool = spy(new RdapConformanceTool());

            tool.setConfigurationFile(configFile.toString());
            tool.setUri(new URI(TEST_URI));
            tool.setExecuteIPv4Queries(false);
            tool.setExecuteIPv6Queries(true);
            tool.call();

            // Get results from the tool's QueryContext instead of singleton
            QueryContext queryContext = tool.getQueryContext();
            assertNotNull(queryContext, "QueryContext should be available after tool.call()");
            var allResults = queryContext.getResults().getAll();
            assertEquals(1, allResults.size(), "Should have exactly one validation result");
            assertTrue(allResults.stream().anyMatch(r -> r.getCode() == IPV6_ERROR_CODE));
        }
    }

    private Path createTempConfigFile() throws Exception {
        Path tempConfigFile = Files.createTempFile("rdapct_config", ".json");
        Files.writeString(tempConfigFile, CONFIG_JSON);
        tempConfigFile.toFile().deleteOnExit();
        return tempConfigFile;
    }

    // Custom AutoCloseable to ensure temp files are deleted
    private static class TempFileResource implements AutoCloseable {
        private final Path path;

        public TempFileResource(Path path) {
            this.path = path;
        }

        @Override
        public void close() throws Exception {
            Files.deleteIfExists(path);
        }

        @Override
        public String toString() {
            return path.toString();
        }
    }
}