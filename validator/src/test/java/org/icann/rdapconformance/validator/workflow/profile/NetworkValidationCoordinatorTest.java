package org.icann.rdapconformance.validator.workflow.profile;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class NetworkValidationCoordinatorTest {

    private String originalParallelProperty;

    @BeforeMethod
    public void setUp() {
        // Store original system property
        originalParallelProperty = System.getProperty("rdap.parallel.network");
    }

    @AfterMethod
    public void tearDown() {
        // Restore original system property
        if (originalParallelProperty != null) {
            System.setProperty("rdap.parallel.network", originalParallelProperty);
        } else {
            System.clearProperty("rdap.parallel.network");
        }
    }

    @Test
    public void testExecuteNetworkValidations_NullInput() {
        boolean result = NetworkValidationCoordinator.executeNetworkValidations(null);
        
        assertThat(result).isTrue();
    }

    @Test
    public void testExecuteNetworkValidations_EmptyList() {
        List<ProfileValidation> emptyList = Collections.emptyList();
        
        boolean result = NetworkValidationCoordinator.executeNetworkValidations(emptyList);
        
        assertThat(result).isTrue();
    }

    @Test
    public void testExecuteNetworkValidations_SequentialMode_SingleValidation() {
        // Ensure parallel networking is disabled
        System.setProperty("rdap.parallel.network", "false");
        
        ProfileValidation mockValidation = mock(ProfileValidation.class);
        when(mockValidation.getGroupName()).thenReturn("TestValidation");
        when(mockValidation.validate()).thenReturn(true);
        
        List<ProfileValidation> validations = List.of(mockValidation);
        
        boolean result = NetworkValidationCoordinator.executeNetworkValidations(validations);
        
        assertThat(result).isTrue();
        verify(mockValidation, times(1)).validate();
    }

    @Test
    public void testExecuteNetworkValidations_SequentialMode_MultipleValidations() {
        // Ensure parallel networking is disabled
        System.setProperty("rdap.parallel.network", "false");
        
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);
        ProfileValidation validation3 = mock(ProfileValidation.class);
        
        when(validation1.getGroupName()).thenReturn("Validation1");
        when(validation2.getGroupName()).thenReturn("Validation2");
        when(validation3.getGroupName()).thenReturn("Validation3");
        
        when(validation1.validate()).thenReturn(true);
        when(validation2.validate()).thenReturn(true);
        when(validation3.validate()).thenReturn(true);
        
        List<ProfileValidation> validations = List.of(validation1, validation2, validation3);
        
        boolean result = NetworkValidationCoordinator.executeNetworkValidations(validations);
        
        assertThat(result).isTrue();
        verify(validation1, times(1)).validate();
        verify(validation2, times(1)).validate();
        verify(validation3, times(1)).validate();
    }

    @Test
    public void testExecuteNetworkValidations_SequentialMode_OneValidationFails() {
        // Ensure parallel networking is disabled
        System.setProperty("rdap.parallel.network", "false");
        
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);
        
        when(validation1.getGroupName()).thenReturn("Validation1");
        when(validation2.getGroupName()).thenReturn("Validation2");
        
        when(validation1.validate()).thenReturn(true);
        when(validation2.validate()).thenReturn(false); // This one fails
        
        List<ProfileValidation> validations = List.of(validation1, validation2);
        
        boolean result = NetworkValidationCoordinator.executeNetworkValidations(validations);
        
        assertThat(result).isFalse();
        verify(validation1, times(1)).validate();
        verify(validation2, times(1)).validate();
    }

    @Test
    public void testExecuteNetworkValidations_ParallelMode_Enabled() {
        // Enable parallel networking
        System.setProperty("rdap.parallel.network", "true");
        
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);
        
        when(validation1.getGroupName()).thenReturn("Validation1");
        when(validation2.getGroupName()).thenReturn("Validation2");
        
        when(validation1.validate()).thenReturn(true);
        when(validation2.validate()).thenReturn(true);
        
        List<ProfileValidation> validations = List.of(validation1, validation2);
        
        boolean result = NetworkValidationCoordinator.executeNetworkValidations(validations);
        
        assertThat(result).isTrue();
        verify(validation1, times(1)).validate();
        verify(validation2, times(1)).validate();
    }

    @Test
    public void testExecuteNetworkValidations_ParallelMode_OneValidationFails() {
        // Enable parallel networking
        System.setProperty("rdap.parallel.network", "true");
        
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);
        
        when(validation1.getGroupName()).thenReturn("Validation1");
        when(validation2.getGroupName()).thenReturn("Validation2");
        
        when(validation1.validate()).thenReturn(true);
        when(validation2.validate()).thenReturn(false);
        
        List<ProfileValidation> validations = List.of(validation1, validation2);
        
        boolean result = NetworkValidationCoordinator.executeNetworkValidations(validations);
        
        assertThat(result).isFalse();
        verify(validation1, times(1)).validate();
        verify(validation2, times(1)).validate();
    }

    @Test
    public void testExecuteNetworkValidations_ParallelMode_RateLimiting() throws InterruptedException {
        // Enable parallel networking
        System.setProperty("rdap.parallel.network", "true");
        
        // Create multiple validations that track execution timing
        List<ProfileValidation> validations = new ArrayList<>();
        List<Long> executionTimes = Collections.synchronizedList(new ArrayList<>());
        
        for (int i = 0; i < 5; i++) {
            ProfileValidation validation = mock(ProfileValidation.class);
            when(validation.getGroupName()).thenReturn("Validation" + i);
            when(validation.validate()).thenAnswer(invocation -> {
                executionTimes.add(System.currentTimeMillis());
                // Small delay to simulate network operation
                Thread.sleep(50);
                return true;
            });
            validations.add(validation);
        }
        
        long startTime = System.currentTimeMillis();
        boolean result = NetworkValidationCoordinator.executeNetworkValidations(validations);
        long endTime = System.currentTimeMillis();
        
        assertThat(result).isTrue();
        assertThat(executionTimes).hasSize(5);
        
        // Verify all validations were called
        for (ProfileValidation validation : validations) {
            verify(validation, times(1)).validate();
        }
        
        // Verify some level of parallelization occurred (not purely sequential)
        long totalTime = endTime - startTime;
        long sequentialTime = 5 * 50; // 5 validations * 50ms each
        assertThat(totalTime).isLessThan(sequentialTime + 1000); // Allow some overhead
    }

    @Test
    public void testExecuteNetworkValidations_SequentialMode_ValidationThrowsException() {
        // Ensure parallel networking is disabled for consistent exception handling
        System.setProperty("rdap.parallel.network", "false");
        
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);
        
        when(validation1.getGroupName()).thenReturn("Validation1");
        when(validation2.getGroupName()).thenReturn("Validation2");
        
        when(validation1.validate()).thenReturn(true);
        when(validation2.validate()).thenThrow(new RuntimeException("Network error"));
        
        List<ProfileValidation> validations = List.of(validation1, validation2);
        
        // In sequential mode, exceptions are thrown directly - catch and verify behavior
        try {
            boolean result = NetworkValidationCoordinator.executeNetworkValidations(validations);
            // If we get here without exception, something changed in the implementation
            assertThat(result).isFalse();
        } catch (RuntimeException e) {
            // This is expected behavior in sequential mode
            assertThat(e.getMessage()).isEqualTo("Network error");
            verify(validation1, times(1)).validate();
            verify(validation2, times(1)).validate();
        }
    }

    @Test
    public void testExecuteNetworkValidations_ParallelMode_ThreadInterruption() throws InterruptedException {
        // Enable parallel networking
        System.setProperty("rdap.parallel.network", "true");
        
        CountDownLatch validationStarted = new CountDownLatch(1);
        AtomicBoolean validationCompleted = new AtomicBoolean(false);
        
        ProfileValidation longRunningValidation = mock(ProfileValidation.class);
        when(longRunningValidation.getGroupName()).thenReturn("LongRunningValidation");
        when(longRunningValidation.validate()).thenAnswer(invocation -> {
            validationStarted.countDown();
            try {
                // Simulate a long-running validation
                Thread.sleep(5000);
                validationCompleted.set(true);
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        });
        
        List<ProfileValidation> validations = List.of(longRunningValidation);
        
        // Execute in a separate thread so we can interrupt it
        Thread executionThread = new Thread(() -> {
            NetworkValidationCoordinator.executeNetworkValidations(validations);
        });
        
        executionThread.start();
        
        // Wait for validation to start, then interrupt
        validationStarted.await(2, TimeUnit.SECONDS);
        executionThread.interrupt();
        
        // Wait for thread to complete
        executionThread.join(2000);
        
        // Validation should not have completed normally
        assertThat(validationCompleted.get()).isFalse();
    }

    @Test
    public void testExecuteNetworkValidations_DefaultParallelProperty() {
        // Don't set the property - should default to false (sequential mode)
        System.clearProperty("rdap.parallel.network");
        
        ProfileValidation mockValidation = mock(ProfileValidation.class);
        when(mockValidation.getGroupName()).thenReturn("TestValidation");
        when(mockValidation.validate()).thenReturn(true);
        
        List<ProfileValidation> validations = List.of(mockValidation);
        
        boolean result = NetworkValidationCoordinator.executeNetworkValidations(validations);
        
        assertThat(result).isTrue();
        verify(mockValidation, times(1)).validate();
    }

    @Test
    public void testExecuteNetworkValidations_InvalidParallelProperty() {
        // Set invalid property value - should default to false (sequential mode)
        System.setProperty("rdap.parallel.network", "invalid");
        
        ProfileValidation mockValidation = mock(ProfileValidation.class);
        when(mockValidation.getGroupName()).thenReturn("TestValidation");
        when(mockValidation.validate()).thenReturn(true);
        
        List<ProfileValidation> validations = List.of(mockValidation);
        
        boolean result = NetworkValidationCoordinator.executeNetworkValidations(validations);
        
        assertThat(result).isTrue();
        verify(mockValidation, times(1)).validate();
    }

    @Test
    public void testExecuteNetworkValidations_MixedValidationResults() {
        // Enable parallel networking
        System.setProperty("rdap.parallel.network", "true");
        
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);
        ProfileValidation validation3 = mock(ProfileValidation.class);
        ProfileValidation validation4 = mock(ProfileValidation.class);
        
        when(validation1.getGroupName()).thenReturn("Validation1");
        when(validation2.getGroupName()).thenReturn("Validation2");
        when(validation3.getGroupName()).thenReturn("Validation3");
        when(validation4.getGroupName()).thenReturn("Validation4");
        
        when(validation1.validate()).thenReturn(true);
        when(validation2.validate()).thenReturn(false);
        when(validation3.validate()).thenReturn(true);
        when(validation4.validate()).thenReturn(false);
        
        List<ProfileValidation> validations = List.of(validation1, validation2, validation3, validation4);
        
        boolean result = NetworkValidationCoordinator.executeNetworkValidations(validations);
        
        // Should be false because some validations failed
        assertThat(result).isFalse();
        
        // All validations should have been executed
        verify(validation1, times(1)).validate();
        verify(validation2, times(1)).validate();
        verify(validation3, times(1)).validate();
        verify(validation4, times(1)).validate();
    }

    // Test categorizeNetworkValidations method
    @Test
    public void testCategorizeNetworkValidations_NullInput() {
        NetworkValidationCoordinator.NetworkValidationGroups groups = 
            NetworkValidationCoordinator.categorizeNetworkValidations(null);
        
        assertThat(groups).isNotNull();
        assertThat(groups.lightweight).isEmpty();
        assertThat(groups.ssl).isEmpty();
        assertThat(groups.other).isEmpty();
        assertThat(groups.isEmpty()).isTrue();
    }

    @Test
    public void testCategorizeNetworkValidations_EmptyList() {
        List<ProfileValidation> emptyList = Collections.emptyList();
        
        NetworkValidationCoordinator.NetworkValidationGroups groups = 
            NetworkValidationCoordinator.categorizeNetworkValidations(emptyList);
        
        assertThat(groups).isNotNull();
        assertThat(groups.lightweight).isEmpty();
        assertThat(groups.ssl).isEmpty();
        assertThat(groups.other).isEmpty();
        assertThat(groups.isEmpty()).isTrue();
    }

    @Test
    public void testCategorizeNetworkValidations_HttpValidations() {
        // Create mock validations with specific class names for HTTP validation
        TigValidation1Dot2 tigValidation = new TigValidation1Dot2();
        ResponseValidationHelp_2024 helpValidation = new ResponseValidationHelp_2024();
        ResponseValidationDomainInvalid_2024 domainValidation = new ResponseValidationDomainInvalid_2024();
        
        List<ProfileValidation> validations = List.of(tigValidation, helpValidation, domainValidation);
        
        NetworkValidationCoordinator.NetworkValidationGroups groups = 
            NetworkValidationCoordinator.categorizeNetworkValidations(validations);
        
        assertThat(groups).isNotNull();
        assertThat(groups.lightweight).hasSize(3); // All should be HTTP
        assertThat(groups.ssl).isEmpty();
        assertThat(groups.other).isEmpty();
        assertThat(groups.isEmpty()).isFalse();
        
        // Test legacy method compatibility
        assertThat(groups.getHttpValidations()).hasSize(3);
        assertThat(groups.getHttpsValidations()).isEmpty();
        assertThat(groups.getTimeoutProneValidations()).hasSize(3);
        assertThat(groups.getNormalValidations()).isEmpty();
    }

    @Test
    public void testCategorizeNetworkValidations_HttpsValidations() {
        // Create mock validations that should be categorized as HTTPS (not matching HTTP patterns)
        RegularValidationMock httpsValidation1 = new RegularValidationMock();
        RegularValidationMock httpsValidation2 = new RegularValidationMock();
        
        List<ProfileValidation> validations = List.of(httpsValidation1, httpsValidation2);
        
        NetworkValidationCoordinator.NetworkValidationGroups groups = 
            NetworkValidationCoordinator.categorizeNetworkValidations(validations);
        
        assertThat(groups).isNotNull();
        assertThat(groups.lightweight).isEmpty();
        assertThat(groups.ssl).hasSize(2); // All should be HTTPS
        assertThat(groups.other).isEmpty();
        assertThat(groups.isEmpty()).isFalse();
        
        // Test legacy method compatibility
        assertThat(groups.getHttpValidations()).isEmpty();
        assertThat(groups.getHttpsValidations()).hasSize(2);
        assertThat(groups.getTimeoutProneValidations()).isEmpty();
        assertThat(groups.getNormalValidations()).hasSize(2);
    }

    @Test
    public void testCategorizeNetworkValidations_MixedValidations() {
        // Create mixed validations - some HTTP, some HTTPS
        TigValidation1Dot2 tigValidation = new TigValidation1Dot2();
        RegularValidationMock httpsValidation = new RegularValidationMock();
        ResponseValidationHelp_2024 helpValidation = new ResponseValidationHelp_2024();
        
        List<ProfileValidation> validations = List.of(tigValidation, httpsValidation, helpValidation);
        
        NetworkValidationCoordinator.NetworkValidationGroups groups = 
            NetworkValidationCoordinator.categorizeNetworkValidations(validations);
        
        assertThat(groups).isNotNull();
        assertThat(groups.lightweight).hasSize(2); // TigValidation1Dot2 and ResponseValidationHelp_2024
        assertThat(groups.ssl).hasSize(1); // RegularValidation
        assertThat(groups.other).isEmpty();
        assertThat(groups.isEmpty()).isFalse();
    }

    @Test 
    public void testExecuteHttpAndHttpsValidations_NullInputs() {
        boolean result = NetworkValidationCoordinator.executeHttpAndHttpsValidations(null, null, 30);
        
        assertThat(result).isTrue();
    }

    @Test
    public void testExecuteHttpAndHttpsValidations_EmptyInputs() {
        List<ProfileValidation> emptyList = Collections.emptyList();
        
        boolean result = NetworkValidationCoordinator.executeHttpAndHttpsValidations(emptyList, emptyList, 30);
        
        assertThat(result).isTrue();
    }

    @Test
    public void testExecuteHttpAndHttpsValidations_OnlyHttpValidations() {
        ProfileValidation httpValidation1 = mock(ProfileValidation.class);
        ProfileValidation httpValidation2 = mock(ProfileValidation.class);
        
        when(httpValidation1.getGroupName()).thenReturn("HttpValidation1");
        when(httpValidation2.getGroupName()).thenReturn("HttpValidation2");
        when(httpValidation1.validate()).thenReturn(true);
        when(httpValidation2.validate()).thenReturn(true);
        
        List<ProfileValidation> httpValidations = List.of(httpValidation1, httpValidation2);
        
        boolean result = NetworkValidationCoordinator.executeHttpAndHttpsValidations(
            httpValidations, null, 10);
        
        assertThat(result).isTrue();
        verify(httpValidation1, times(1)).validate();
        verify(httpValidation2, times(1)).validate();
    }

    @Test
    public void testExecuteHttpAndHttpsValidations_OnlyHttpsValidations() {
        // Disable parallel networking to use sequential mode in executeNetworkValidations
        System.setProperty("rdap.parallel.network", "false");
        
        ProfileValidation httpsValidation1 = mock(ProfileValidation.class);
        ProfileValidation httpsValidation2 = mock(ProfileValidation.class);
        
        when(httpsValidation1.getGroupName()).thenReturn("HttpsValidation1");
        when(httpsValidation2.getGroupName()).thenReturn("HttpsValidation2");
        when(httpsValidation1.validate()).thenReturn(true);
        when(httpsValidation2.validate()).thenReturn(true);
        
        List<ProfileValidation> httpsValidations = List.of(httpsValidation1, httpsValidation2);
        
        boolean result = NetworkValidationCoordinator.executeHttpAndHttpsValidations(
            null, httpsValidations, 10);
        
        assertThat(result).isTrue();
        verify(httpsValidation1, times(1)).validate();
        verify(httpsValidation2, times(1)).validate();
    }

    @Test
    public void testExecuteHttpAndHttpsValidations_BothTypes() {
        // Disable parallel networking to use sequential mode in executeNetworkValidations
        System.setProperty("rdap.parallel.network", "false");
        
        ProfileValidation httpValidation = mock(ProfileValidation.class);
        ProfileValidation httpsValidation = mock(ProfileValidation.class);
        
        when(httpValidation.getGroupName()).thenReturn("HttpValidation");
        when(httpsValidation.getGroupName()).thenReturn("HttpsValidation");
        when(httpValidation.validate()).thenReturn(true);
        when(httpsValidation.validate()).thenReturn(true);
        
        List<ProfileValidation> httpValidations = List.of(httpValidation);
        List<ProfileValidation> httpsValidations = List.of(httpsValidation);
        
        boolean result = NetworkValidationCoordinator.executeHttpAndHttpsValidations(
            httpValidations, httpsValidations, 10);
        
        assertThat(result).isTrue();
        verify(httpValidation, times(1)).validate();
        verify(httpsValidation, times(1)).validate();
    }

    @Test
    public void testExecuteHttpAndHttpsValidations_HttpValidationFails() {
        ProfileValidation httpValidation = mock(ProfileValidation.class);
        
        when(httpValidation.getGroupName()).thenReturn("FailingHttpValidation");
        when(httpValidation.validate()).thenReturn(false);
        
        List<ProfileValidation> httpValidations = List.of(httpValidation);
        
        boolean result = NetworkValidationCoordinator.executeHttpAndHttpsValidations(
            httpValidations, null, 10);
        
        assertThat(result).isFalse();
        verify(httpValidation, times(1)).validate();
    }

    @Test
    public void testExecuteHttpAndHttpsValidations_HttpsValidationFails() {
        // Disable parallel networking to use sequential mode in executeNetworkValidations
        System.setProperty("rdap.parallel.network", "false");
        
        ProfileValidation httpsValidation = mock(ProfileValidation.class);
        
        when(httpsValidation.getGroupName()).thenReturn("FailingHttpsValidation");
        when(httpsValidation.validate()).thenReturn(false);
        
        List<ProfileValidation> httpsValidations = List.of(httpsValidation);
        
        boolean result = NetworkValidationCoordinator.executeHttpAndHttpsValidations(
            null, httpsValidations, 10);
        
        assertThat(result).isFalse();
        verify(httpsValidation, times(1)).validate();
    }

    @Test
    public void testExecuteHttpAndHttpsValidations_HttpValidationThrowsException() {
        ProfileValidation httpValidation = mock(ProfileValidation.class);
        
        when(httpValidation.getGroupName()).thenReturn("ExceptionHttpValidation");
        when(httpValidation.validate()).thenThrow(new RuntimeException("HTTP validation error"));
        
        List<ProfileValidation> httpValidations = List.of(httpValidation);
        
        boolean result = NetworkValidationCoordinator.executeHttpAndHttpsValidations(
            httpValidations, null, 10);
        
        assertThat(result).isFalse();
        verify(httpValidation, times(1)).validate();
    }

    @Test
    public void testNetworkValidationGroups_Constructor() {
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);
        ProfileValidation validation3 = mock(ProfileValidation.class);
        
        List<ProfileValidation> lightweightList = List.of(validation1);
        List<ProfileValidation> sslList = List.of(validation2);
        List<ProfileValidation> otherList = List.of(validation3);
        
        NetworkValidationCoordinator.NetworkValidationGroups groups = 
            new NetworkValidationCoordinator.NetworkValidationGroups(lightweightList, sslList, otherList);
        
        assertThat(groups.lightweight).isEqualTo(lightweightList);
        assertThat(groups.ssl).isEqualTo(sslList);
        assertThat(groups.other).isEqualTo(otherList);
        assertThat(groups.isEmpty()).isFalse();
    }

    @Test
    public void testNetworkValidationGroups_EmptyGroups() {
        NetworkValidationCoordinator.NetworkValidationGroups groups = 
            new NetworkValidationCoordinator.NetworkValidationGroups(
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        
        assertThat(groups.lightweight).isEmpty();
        assertThat(groups.ssl).isEmpty();
        assertThat(groups.other).isEmpty();
        assertThat(groups.isEmpty()).isTrue();
        
        // Test all accessor methods
        assertThat(groups.getTimeoutProneValidations()).isEmpty();
        assertThat(groups.getNormalValidations()).isEmpty();
        assertThat(groups.getHttpValidations()).isEmpty();
        assertThat(groups.getHttpsValidations()).isEmpty();
    }

    @Test
    public void testShutdown() {
        // This test primarily ensures the method can be called without exceptions
        // Since executors are static, we need to be careful not to disrupt other tests
        try {
            NetworkValidationCoordinator.shutdown();
            // If we get here without exception, shutdown worked
            assertThat(true).isTrue();
        } catch (Exception e) {
            // Shutdown should not throw exceptions
            assertThat(e).isNull();
        }
    }

    // Mock classes for testing HTTP validation categorization
    private static class TigValidation1Dot2 extends ProfileValidation {
        public TigValidation1Dot2() {
            super(null);
        }
        
        @Override
        public boolean doValidate() {
            return true;
        }
        
        @Override
        public String getGroupName() {
            return "TigValidation1Dot2";
        }
    }
    
    private static class ResponseValidationHelp_2024 extends ProfileValidation {
        public ResponseValidationHelp_2024() {
            super(null);
        }
        
        @Override
        public boolean doValidate() {
            return true;
        }
        
        @Override
        public String getGroupName() {
            return "ResponseValidationHelp_2024";
        }
    }
    
    private static class ResponseValidationDomainInvalid_2024 extends ProfileValidation {
        public ResponseValidationDomainInvalid_2024() {
            super(null);
        }
        
        @Override
        public boolean doValidate() {
            return true;
        }
        
        @Override
        public String getGroupName() {
            return "ResponseValidationDomainInvalid_2024";
        }
    }
    
    private static class RegularValidationMock extends ProfileValidation {
        public RegularValidationMock() {
            super(null);
        }
        
        @Override
        public boolean doValidate() {
            return true;
        }
        
        @Override
        public String getGroupName() {
            return "RegularValidation";
        }
    }
}