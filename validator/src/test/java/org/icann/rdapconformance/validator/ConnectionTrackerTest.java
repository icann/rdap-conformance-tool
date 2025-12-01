package org.icann.rdapconformance.validator;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collections;

import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResultFile;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.anyInt;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;

public class ConnectionTrackerTest {

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
        when(config.isGtldRegistrar()).thenReturn(true);
        when(config.isGtldRegistry()).thenReturn(false);
        when(config.getUri()).thenReturn(URI.create("https://example.com/rdap"));

        when(configFile.isError(anyInt())).thenReturn(false);
        when(configFile.isWarning(anyInt())).thenReturn(false);
        when(configFile.getDefinitionIgnore()).thenReturn(Collections.emptyList());
        when(configFile.getDefinitionNotes()).thenReturn(Collections.emptyList());
        when(configFile.getDefinitionIdentifier()).thenReturn("test-definition");

        // Create QueryContext-based instances instead of using singletons
        queryContext = QueryContext.forTesting(config);
        connectionTracker = queryContext.getConnectionTracker();
        results = queryContext.getResults();

        // Initialize result file through QueryContext
        queryContext.getResultFile().initialize(results, config, configFile, null);
    }

    @Test
    public void testIsResourceNotFoundNoteWarning_All404s_GtldProfile_NoErrors_AddsWarning() {
        // Use instance field: connectionTracker
        connectionTracker.reset();

        // Use class field config
        // Use class field configFile
        when(config.useRdapProfileFeb2024()).thenReturn(true);
        when(config.isGtldRegistrar()).thenReturn(false);  // Override setup to ensure isGtldRegistry() is called
        when(config.isGtldRegistry()).thenReturn(true);
        when(config.getUri()).thenReturn(URI.create("http://example.com/domain/example.com"));

        // Add a main connection with 404 status
        connectionTracker.startTrackingNewConnection(URI.create("http://example.com"), "GET", true, NetworkProtocol.IPv4);
        connectionTracker.completeCurrentConnection(404, ConnectionStatus.SUCCESS);
        boolean result = connectionTracker.isResourceNotFoundNoteWarning(queryContext, config);

        assertTrue(result, "Should return true for all 404s with gTLD profile");
        verify(config).useRdapProfileFeb2024();
        verify(config, atLeastOnce()).isGtldRegistry();
    }

    @Test
    public void testIsResourceNotFoundNoteWarning_ConnectionNot404_ReturnsFalse() {
        // Use instance field: connectionTracker
        connectionTracker.reset();

        // Use class field config
        // Use class field configFile

        // Add a main connection with 200 status (not 404)
        connectionTracker.startTrackingNewConnection(URI.create("http://example.com"), "GET", true, NetworkProtocol.IPv4);
        connectionTracker.completeCurrentConnection(200, ConnectionStatus.SUCCESS);
        boolean result = connectionTracker.isResourceNotFoundNoteWarning(queryContext, config);

        assertFalse(result, "Should return false when any connection is not 404");
    }

    @Test
    public void testIsResourceNotFoundNoteWarning_HeadMethodRelevant() {
        // Use instance field: connectionTracker
        connectionTracker.reset();

        // Use class field config
        // Use class field configFile

        // Add a HEAD connection with 404 status
        connectionTracker.startTracking(URI.create("http://example.com"), "1.2.3.4", NetworkProtocol.IPv4, "HEAD", false);
        connectionTracker.completeTrackingById(connectionTracker.getLastConnection().getTrackingId(), 404, ConnectionStatus.SUCCESS);
        boolean result = connectionTracker.isResourceNotFoundNoteWarning(queryContext, config);


        assertTrue(result, "HEAD method should be considered relevant");
    }

    @Test
    public void testIsResourceNotFoundNoteWarning_NoRelevantConnections_ReturnsFalse() {
        // Use instance field: connectionTracker
        connectionTracker.reset();

        // Use class field config
        // Use class field configFile

        boolean foundRelevant = connectionTracker.isResourceNotFoundNoteWarning(queryContext, config);
        assertFalse(foundRelevant, "Should return false if no relevant connections");
    }

    // Tests for the new pure query method areAllRelevantQueriesNotFound()

    @Test
    public void testAreAllRelevantQueriesNotFound_All404s_ReturnsTrue() {
        connectionTracker.reset();

        // Add a main connection with 404 status
        connectionTracker.startTrackingNewConnection(URI.create("http://example.com"), "GET", true, NetworkProtocol.IPv4);
        connectionTracker.completeCurrentConnection(404, ConnectionStatus.SUCCESS);

        // Add a HEAD connection with 404 status
        connectionTracker.startTracking(URI.create("http://example.com"), "1.2.3.4", NetworkProtocol.IPv4, "HEAD", false);
        connectionTracker.completeTrackingById(connectionTracker.getLastConnection().getTrackingId(), 404, ConnectionStatus.SUCCESS);

        boolean result = connectionTracker.areAllRelevantQueriesNotFound();
        assertTrue(result, "Should return true when all relevant queries returned 404");
    }

    @Test
    public void testAreAllRelevantQueriesNotFound_Mixed404And200_ReturnsFalse() {
        connectionTracker.reset();

        // Add a main connection with 404 status
        connectionTracker.startTrackingNewConnection(URI.create("http://example.com"), "GET", true, NetworkProtocol.IPv4);
        connectionTracker.completeCurrentConnection(404, ConnectionStatus.SUCCESS);

        // Add a HEAD connection with 200 status (not 404)
        connectionTracker.startTracking(URI.create("http://example.com"), "1.2.3.4", NetworkProtocol.IPv4, "HEAD", false);
        connectionTracker.completeTrackingById(connectionTracker.getLastConnection().getTrackingId(), 200, ConnectionStatus.SUCCESS);

        boolean result = connectionTracker.areAllRelevantQueriesNotFound();
        assertFalse(result, "Should return false when any connection is not 404");
    }

    @Test
    public void testAreAllRelevantQueriesNotFound_NoConnections_ReturnsFalse() {
        connectionTracker.reset();

        boolean result = connectionTracker.areAllRelevantQueriesNotFound();
        assertFalse(result, "Should return false when no relevant connections exist");
    }

    @Test
    public void testAreAllRelevantQueriesNotFound_OnlyNonRelevantConnections_ReturnsFalse() {
        connectionTracker.reset();

        // Add a non-main, non-HEAD connection (should be ignored)
        connectionTracker.startTracking(URI.create("http://example.com/help"), "1.2.3.4", NetworkProtocol.IPv4, "GET", false);
        connectionTracker.completeTrackingById(connectionTracker.getLastConnection().getTrackingId(), 200, ConnectionStatus.SUCCESS);

        boolean result = connectionTracker.areAllRelevantQueriesNotFound();
        assertFalse(result, "Should return false when only non-relevant connections exist");
    }

    @Test
    public void testAreAllRelevantQueriesNotFound_MainGet404_ReturnsTrue() {
        connectionTracker.reset();

        // Add only a main GET connection with 404
        connectionTracker.startTrackingNewConnection(URI.create("http://example.com"), "GET", true, NetworkProtocol.IPv4);
        connectionTracker.completeCurrentConnection(404, ConnectionStatus.SUCCESS);

        boolean result = connectionTracker.areAllRelevantQueriesNotFound();
        assertTrue(result, "Should return true for single main GET with 404");
    }

    @Test
    public void testAreAllRelevantQueriesNotFound_HeadOnly404_ReturnsTrue() {
        connectionTracker.reset();

        // Add only a HEAD connection with 404
        connectionTracker.startTracking(URI.create("http://example.com"), "1.2.3.4", NetworkProtocol.IPv4, "HEAD", false);
        connectionTracker.completeTrackingById(connectionTracker.getLastConnection().getTrackingId(), 404, ConnectionStatus.SUCCESS);

        boolean result = connectionTracker.areAllRelevantQueriesNotFound();
        assertTrue(result, "Should return true for single HEAD with 404");
    }

    @Test
    public void testAreAllRelevantQueriesNotFound_PureMethod_NoSideEffects() {
        connectionTracker.reset();

        // Add a main connection with 404
        connectionTracker.startTrackingNewConnection(URI.create("http://example.com"), "GET", true, NetworkProtocol.IPv4);
        connectionTracker.completeCurrentConnection(404, ConnectionStatus.SUCCESS);

        int resultCountBefore = results.getResultCount();

        // Call the pure method - should not add any results
        boolean result = connectionTracker.areAllRelevantQueriesNotFound();

        int resultCountAfter = results.getResultCount();

        assertTrue(result, "Should return true for 404");
        assertEquals(resultCountBefore, resultCountAfter, "Pure method should not add any results");
    }
}