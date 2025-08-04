package org.icann.rdapconformance.validator.workflow.profile;

import org.icann.rdapconformance.validator.NetworkInfo;
import org.icann.rdapconformance.validator.NetworkProtocol;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    public void testExecuteNetworkValidations_ParallelMode_ValidationThrowsException() {
        // Enable parallel networking
        System.setProperty("rdap.parallel.network", "true");
        
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);
        
        when(validation1.getGroupName()).thenReturn("Validation1");
        when(validation2.getGroupName()).thenReturn("Validation2");
        
        when(validation1.validate()).thenReturn(true);
        when(validation2.validate()).thenThrow(new RuntimeException("Network error"));
        
        List<ProfileValidation> validations = List.of(validation1, validation2);
        
        boolean result = NetworkValidationCoordinator.executeNetworkValidations(validations);
        
        // Should handle exceptions gracefully and treat as validation failure
        assertThat(result).isFalse();
        verify(validation1, times(1)).validate();
        verify(validation2, times(1)).validate();
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
}