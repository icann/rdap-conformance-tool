package org.icann.rdapconformance.validator;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collections;

import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResultFile;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class ConnectionTrackerTest {

    @BeforeMethod
    public void setUp() {
        RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
        ConfigurationFile configFile = mock(ConfigurationFile.class);

        when(config.useRdapProfileFeb2024()).thenReturn(true);
        when(config.isGtldRegistrar()).thenReturn(true); // or false, as needed
        when(config.isGtldRegistry()).thenReturn(false); // or true, as needed
        when(config.getUri()).thenReturn(URI.create("https://example.com/rdap"));

        when(configFile.isError(anyInt())).thenReturn(false);
        when(configFile.isWarning(anyInt())).thenReturn(false);
        when(configFile.getDefinitionIgnore()).thenReturn(Collections.emptyList());
        when(configFile.getDefinitionNotes()).thenReturn(Collections.emptyList());
        when(configFile.getDefinitionIdentifier()).thenReturn("test-definition");

        ConnectionTracker.getInstance().reset();
        RDAPValidatorResultsImpl.reset();
        RDAPValidationResultFile.reset();
        RDAPValidationResultFile.getInstance()
                                .initialize(RDAPValidatorResultsImpl.getInstance(), config, configFile, null);
    }

    @AfterMethod
    public void tearDown() {
        ConnectionTracker.getInstance().reset();
    }

    @Test
    public void testIsResourceNotFoundNoteWarning_All404s_GtldProfile_NoErrors_AddsWarning() {
        ConnectionTracker tracker = ConnectionTracker.getInstance();
        tracker.reset();

        RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
        ConfigurationFile configFile = mock(ConfigurationFile.class);
        RDAPValidationResultFile resultFile = mock(RDAPValidationResultFile.class);

        when(config.useRdapProfileFeb2024()).thenReturn(true);
        when(config.isGtldRegistry()).thenReturn(true);
        when(config.getUri()).thenReturn(URI.create("http://example.com/domain/example.com"));
        when(resultFile.getErrorCount()).thenReturn(0);

        // Use reflection to set the mock in singleton instance
        try {
            Field instanceField = RDAPValidationResultFile.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, resultFile);
        } catch (Exception e) {
            fail("Failed to set mock RDAPValidationResultFile: " + e.getMessage());
        }

        // Add a main connection with 404 status
        tracker.startTrackingNewConnection(URI.create("http://example.com"), "GET", true);
        tracker.completeCurrentConnection(404, ConnectionStatus.SUCCESS);
        boolean result = tracker.isResourceNotFoundNoteWarning(config);

        assertTrue(result, "Should return true for all 404s with gTLD profile");
        verify(config).useRdapProfileFeb2024();
        verify(config, atLeastOnce()).isGtldRegistry();
        verify(resultFile).getErrorCount();
    }

    @Test
    public void testIsResourceNotFoundNoteWarning_ConnectionNot404_ReturnsFalse() {
        ConnectionTracker tracker = ConnectionTracker.getInstance();
        tracker.reset();

        RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
        ConfigurationFile configFile = mock(ConfigurationFile.class);

        // Add a main connection with 200 status (not 404)
        tracker.startTrackingNewConnection(URI.create("http://example.com"), "GET", true);
        tracker.completeCurrentConnection(200, ConnectionStatus.SUCCESS);
        boolean result = tracker.isResourceNotFoundNoteWarning(config);

        assertFalse(result, "Should return false when any connection is not 404");
    }

    @Test
    public void testIsResourceNotFoundNoteWarning_HeadMethodRelevant() {
        ConnectionTracker tracker = ConnectionTracker.getInstance();
        tracker.reset();

        RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
        ConfigurationFile configFile = mock(ConfigurationFile.class);

        // Add a HEAD connection with 404 status
        tracker.startTracking(URI.create("http://example.com"), "1.2.3.4", NetworkProtocol.IPv4, "HEAD", false);
        tracker.completeTrackingById(tracker.getLastConnection().getTrackingId(), 404, ConnectionStatus.SUCCESS);
        boolean result = tracker.isResourceNotFoundNoteWarning(config);


        assertTrue(result, "HEAD method should be considered relevant");
    }

    @Test
    public void testIsResourceNotFoundNoteWarning_NoRelevantConnections_ReturnsFalse() {
        ConnectionTracker tracker = ConnectionTracker.getInstance();
        tracker.reset();

        RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
        ConfigurationFile configFile = mock(ConfigurationFile.class);

        boolean foundRelevant = tracker.isResourceNotFoundNoteWarning(config);
        assertFalse(foundRelevant, "Should return false if no relevant connections");
    }
}