package org.icann.rdapconformance.validator;

import java.net.URI;
import java.util.Collections;

import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyInt;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for CommonUtils utility methods.
 */
public class CommonUtilsTest {

    private QueryContext queryContext;
    private ConnectionTracker connectionTracker;
    private RDAPValidatorResults results;
    private RDAPValidatorConfiguration config;
    private ConfigurationFile configFile;

    @BeforeMethod
    public void setUp() {
        config = mock(RDAPValidatorConfiguration.class);
        configFile = mock(ConfigurationFile.class);

        when(config.useRdapProfileFeb2024()).thenReturn(true);
        when(config.isGtldRegistrar()).thenReturn(false);
        when(config.isGtldRegistry()).thenReturn(true);
        when(config.getUri()).thenReturn(URI.create("https://example.com/rdap/domain/test.com"));

        when(configFile.isError(anyInt())).thenReturn(false);
        when(configFile.isWarning(anyInt())).thenReturn(false);
        when(configFile.getDefinitionIgnore()).thenReturn(Collections.emptyList());
        when(configFile.getDefinitionNotes()).thenReturn(Collections.emptyList());
        when(configFile.getDefinitionIdentifier()).thenReturn("test-definition");

        // Create QueryContext-based instances
        queryContext = QueryContext.forTesting(config);
        connectionTracker = queryContext.getConnectionTracker();
        results = queryContext.getResults();

        // Initialize result file through QueryContext
        queryContext.getResultFile().initialize(results, config, configFile, null);
    }

    @Test
    public void testHandleResourceNotFoundWarning_All404s_ReturnsTrue_AddsWarning() {
        connectionTracker.reset();

        // Add a main connection with 404 status
        connectionTracker.startTrackingNewConnection(URI.create("http://example.com"), "GET", true, NetworkProtocol.IPv4);
        connectionTracker.completeCurrentConnection(404, ConnectionStatus.SUCCESS);

        int resultCountBefore = results.getResultCount();

        boolean result = CommonUtils.handleResourceNotFoundWarning(queryContext, config);

        assertTrue(result, "Should return true when all relevant queries returned 404");
        assertEquals(results.getResultCount(), resultCountBefore + 1, "Should add one warning result");

        // Verify the warning code is -13020
        boolean hasWarningCode = results.getAll().stream()
            .anyMatch(r -> r.getCode() == CommonUtils.RESOURCE_NOT_FOUND_WARNING_CODE);
        assertTrue(hasWarningCode, "Should add warning with code -13020");
    }

    @Test
    public void testHandleResourceNotFoundWarning_Not404_ReturnsFalse_NoWarning() {
        connectionTracker.reset();

        // Add a main connection with 200 status
        connectionTracker.startTrackingNewConnection(URI.create("http://example.com"), "GET", true, NetworkProtocol.IPv4);
        connectionTracker.completeCurrentConnection(200, ConnectionStatus.SUCCESS);

        int resultCountBefore = results.getResultCount();

        boolean result = CommonUtils.handleResourceNotFoundWarning(queryContext, config);

        assertFalse(result, "Should return false when connection is not 404");
        assertEquals(results.getResultCount(), resultCountBefore, "Should not add any results");
    }

    @Test
    public void testHandleResourceNotFoundWarning_NoConnections_ReturnsFalse() {
        connectionTracker.reset();

        int resultCountBefore = results.getResultCount();

        boolean result = CommonUtils.handleResourceNotFoundWarning(queryContext, config);

        assertFalse(result, "Should return false when no connections exist");
        assertEquals(results.getResultCount(), resultCountBefore, "Should not add any results");
    }

    @Test
    public void testHandleResourceNotFoundWarning_Mixed404And200_ReturnsFalse() {
        connectionTracker.reset();

        // Add a main connection with 404 status
        connectionTracker.startTrackingNewConnection(URI.create("http://example.com"), "GET", true, NetworkProtocol.IPv4);
        connectionTracker.completeCurrentConnection(404, ConnectionStatus.SUCCESS);

        // Add a HEAD connection with 200 status
        connectionTracker.startTracking(URI.create("http://example.com"), "1.2.3.4", NetworkProtocol.IPv4, "HEAD", false);
        connectionTracker.completeTrackingById(connectionTracker.getLastConnection().getTrackingId(), 200, ConnectionStatus.SUCCESS);

        int resultCountBefore = results.getResultCount();

        boolean result = CommonUtils.handleResourceNotFoundWarning(queryContext, config);

        assertFalse(result, "Should return false when any connection is not 404");
        assertEquals(results.getResultCount(), resultCountBefore, "Should not add any results");
    }

    @Test
    public void testHandleResourceNotFoundWarning_WarningMessageContent() {
        connectionTracker.reset();

        // Add a main connection with 404 status
        connectionTracker.startTrackingNewConnection(URI.create("http://example.com"), "GET", true, NetworkProtocol.IPv4);
        connectionTracker.completeCurrentConnection(404, ConnectionStatus.SUCCESS);

        CommonUtils.handleResourceNotFoundWarning(queryContext, config);

        // Verify warning message contains expected text
        var warningResult = results.getAll().stream()
            .filter(r -> r.getCode() == CommonUtils.RESOURCE_NOT_FOUND_WARNING_CODE)
            .findFirst();

        assertTrue(warningResult.isPresent(), "Warning should be present");
        assertTrue(warningResult.get().getMessage().contains("404"), "Message should mention 404");
        assertTrue(warningResult.get().getMessage().contains("validly formed"), "Message should mention validly formed");
    }

    @Test
    public void testHandleResourceNotFoundWarning_UsesConfigUri() {
        connectionTracker.reset();

        URI testUri = URI.create("https://test.example.com/rdap/domain/foo.com");
        when(config.getUri()).thenReturn(testUri);

        // Add a main connection with 404 status
        connectionTracker.startTrackingNewConnection(URI.create("http://example.com"), "GET", true, NetworkProtocol.IPv4);
        connectionTracker.completeCurrentConnection(404, ConnectionStatus.SUCCESS);

        CommonUtils.handleResourceNotFoundWarning(queryContext, config);

        // Verify warning uses the config URI as the value
        var warningResult = results.getAll().stream()
            .filter(r -> r.getCode() == CommonUtils.RESOURCE_NOT_FOUND_WARNING_CODE)
            .findFirst();

        assertTrue(warningResult.isPresent(), "Warning should be present");
        assertEquals(warningResult.get().getValue(), testUri.toString(), "Warning value should be the config URI");
    }

    @Test
    public void testHandleResourceNotFoundWarning_MultipleMainConnections_All404() {
        connectionTracker.reset();

        // Add multiple main connections all with 404
        connectionTracker.startTrackingNewConnection(URI.create("http://example.com"), "GET", true, NetworkProtocol.IPv4);
        connectionTracker.completeCurrentConnection(404, ConnectionStatus.SUCCESS);

        connectionTracker.startTrackingNewConnection(URI.create("http://example.com"), "GET", true, NetworkProtocol.IPv6);
        connectionTracker.completeCurrentConnection(404, ConnectionStatus.SUCCESS);

        boolean result = CommonUtils.handleResourceNotFoundWarning(queryContext, config);

        assertTrue(result, "Should return true when all main connections return 404");
    }

    @Test
    public void testResourceNotFoundConstants() {
        // Verify the constants are set correctly
        assertEquals(CommonUtils.RESOURCE_NOT_FOUND_WARNING_CODE, -13020, "Warning code should be -13020");
        assertNotNull(CommonUtils.RESOURCE_NOT_FOUND_MESSAGE, "Warning message should not be null");
        assertTrue(CommonUtils.RESOURCE_NOT_FOUND_MESSAGE.contains("404"), "Message should mention 404");
    }
}
