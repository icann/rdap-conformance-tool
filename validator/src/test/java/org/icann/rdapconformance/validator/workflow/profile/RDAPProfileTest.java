package org.icann.rdapconformance.validator.workflow.profile;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class RDAPProfileTest {

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
    public void testConstructor_SingleValidationList() {
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);
        List<ProfileValidation> validations = List.of(validation1, validation2);
        
        RDAPProfile profile = new RDAPProfile(validations);
        
        assertThat(profile).isNotNull();
    }

    @Test
    public void testConstructor_SeparateParallelAndSequentialLists() {
        ProfileValidation parallelValidation = mock(ProfileValidation.class);
        ProfileValidation sequentialValidation = mock(ProfileValidation.class);
        
        List<ProfileValidation> parallelValidations = List.of(parallelValidation);
        List<ProfileValidation> sequentialValidations = List.of(sequentialValidation);
        
        RDAPProfile profile = new RDAPProfile(parallelValidations, sequentialValidations);
        
        assertThat(profile).isNotNull();
    }

    @Test
    public void testValidate_SingleList_SequentialMode_AllValidationsPass() {
        // Disable parallel networking
        System.setProperty("rdap.parallel.network", "false");
        
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);
        
        when(validation1.getGroupName()).thenReturn("Validation1");
        when(validation2.getGroupName()).thenReturn("Validation2");
        when(validation1.validate()).thenReturn(true);
        when(validation2.validate()).thenReturn(true);
        
        List<ProfileValidation> validations = List.of(validation1, validation2);
        RDAPProfile profile = new RDAPProfile(validations);
        
        boolean result = profile.validate();
        
        assertThat(result).isTrue();
        verify(validation1, times(1)).validate();
        verify(validation2, times(1)).validate();
    }

    @Test
    public void testValidate_SingleList_SequentialMode_OneValidationFails() {
        // Disable parallel networking
        System.setProperty("rdap.parallel.network", "false");
        
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);
        
        when(validation1.getGroupName()).thenReturn("Validation1");
        when(validation2.getGroupName()).thenReturn("Validation2");
        when(validation1.validate()).thenReturn(true);
        when(validation2.validate()).thenReturn(false);
        
        List<ProfileValidation> validations = List.of(validation1, validation2);
        RDAPProfile profile = new RDAPProfile(validations);
        
        boolean result = profile.validate();
        
        assertThat(result).isFalse();
        verify(validation1, times(1)).validate();
        verify(validation2, times(1)).validate();
    }

    @Test
    public void testValidate_SingleList_ParallelMode_NetworkAndNonNetworkValidations() {
        // Enable parallel networking
        System.setProperty("rdap.parallel.network", "true");
        
        // Create mock network validation (contains "network" or "tig" keywords)
        ProfileValidation networkValidation = mock(ProfileValidation.class);
        when(networkValidation.getGroupName()).thenReturn("NetworkValidation");
        when(networkValidation.getClass()).thenReturn((Class) MockNetworkValidation.class);
        when(networkValidation.validate()).thenReturn(true);
        
        // Create mock non-network validation
        ProfileValidation nonNetworkValidation = mock(ProfileValidation.class);
        when(nonNetworkValidation.getGroupName()).thenReturn("ResponseValidation");
        when(nonNetworkValidation.getClass()).thenReturn((Class) MockResponseValidation.class);
        when(nonNetworkValidation.validate()).thenReturn(true);
        
        List<ProfileValidation> validations = List.of(networkValidation, nonNetworkValidation);
        RDAPProfile profile = new RDAPProfile(validations);
        
        boolean result = profile.validate();
        
        assertThat(result).isTrue();
        verify(networkValidation, times(1)).validate();
        verify(nonNetworkValidation, times(1)).validate();
    }

    @Test
    public void testValidate_SeparateLists_AllValidationsPass() {
        ProfileValidation parallelValidation = mock(ProfileValidation.class);
        ProfileValidation sequentialValidation = mock(ProfileValidation.class);
        
        when(parallelValidation.getGroupName()).thenReturn("ParallelValidation");
        when(sequentialValidation.getGroupName()).thenReturn("SequentialValidation");
        when(parallelValidation.validate()).thenReturn(true);
        when(sequentialValidation.validate()).thenReturn(true);
        
        List<ProfileValidation> parallelValidations = List.of(parallelValidation);
        List<ProfileValidation> sequentialValidations = List.of(sequentialValidation);
        
        RDAPProfile profile = new RDAPProfile(parallelValidations, sequentialValidations);
        
        boolean result = profile.validate();
        
        assertThat(result).isTrue();
        verify(parallelValidation, times(1)).validate();
        verify(sequentialValidation, times(1)).validate();
    }

    @Test
    public void testValidate_SeparateLists_ParallelValidationFails() {
        ProfileValidation parallelValidation = mock(ProfileValidation.class);
        ProfileValidation sequentialValidation = mock(ProfileValidation.class);
        
        when(parallelValidation.getGroupName()).thenReturn("ParallelValidation");
        when(sequentialValidation.getGroupName()).thenReturn("SequentialValidation");
        when(parallelValidation.validate()).thenReturn(false);
        when(sequentialValidation.validate()).thenReturn(true);
        
        List<ProfileValidation> parallelValidations = List.of(parallelValidation);
        List<ProfileValidation> sequentialValidations = List.of(sequentialValidation);
        
        RDAPProfile profile = new RDAPProfile(parallelValidations, sequentialValidations);
        
        boolean result = profile.validate();
        
        assertThat(result).isFalse();
        verify(parallelValidation, times(1)).validate();
        verify(sequentialValidation, times(1)).validate();
    }

    @Test
    public void testValidate_SeparateLists_SequentialValidationFails() {
        ProfileValidation parallelValidation = mock(ProfileValidation.class);
        ProfileValidation sequentialValidation = mock(ProfileValidation.class);
        
        when(parallelValidation.getGroupName()).thenReturn("ParallelValidation");
        when(sequentialValidation.getGroupName()).thenReturn("SequentialValidation");
        when(parallelValidation.validate()).thenReturn(true);
        when(sequentialValidation.validate()).thenReturn(false);
        
        List<ProfileValidation> parallelValidations = List.of(parallelValidation);
        List<ProfileValidation> sequentialValidations = List.of(sequentialValidation);
        
        RDAPProfile profile = new RDAPProfile(parallelValidations, sequentialValidations);
        
        boolean result = profile.validate();
        
        assertThat(result).isFalse();
        verify(parallelValidation, times(1)).validate();
        verify(sequentialValidation, times(1)).validate();
    }

    @Test
    public void testValidate_SeparateLists_EmptyParallelList() {
        ProfileValidation sequentialValidation = mock(ProfileValidation.class);
        
        when(sequentialValidation.getGroupName()).thenReturn("SequentialValidation");
        when(sequentialValidation.validate()).thenReturn(true);
        
        List<ProfileValidation> parallelValidations = new ArrayList<>();
        List<ProfileValidation> sequentialValidations = List.of(sequentialValidation);
        
        RDAPProfile profile = new RDAPProfile(parallelValidations, sequentialValidations);
        
        boolean result = profile.validate();
        
        assertThat(result).isTrue();
        verify(sequentialValidation, times(1)).validate();
    }

    @Test
    public void testValidate_SeparateLists_EmptySequentialList() {
        ProfileValidation parallelValidation = mock(ProfileValidation.class);
        
        when(parallelValidation.getGroupName()).thenReturn("ParallelValidation");
        when(parallelValidation.validate()).thenReturn(true);
        
        List<ProfileValidation> parallelValidations = List.of(parallelValidation);
        List<ProfileValidation> sequentialValidations = new ArrayList<>();
        
        RDAPProfile profile = new RDAPProfile(parallelValidations, sequentialValidations);
        
        boolean result = profile.validate();
        
        assertThat(result).isTrue();
        verify(parallelValidation, times(1)).validate();
    }

    @Test
    public void testValidate_SeparateLists_BothListsEmpty() {
        List<ProfileValidation> parallelValidations = new ArrayList<>();
        List<ProfileValidation> sequentialValidations = new ArrayList<>();
        
        RDAPProfile profile = new RDAPProfile(parallelValidations, sequentialValidations);
        
        boolean result = profile.validate();
        
        assertThat(result).isTrue();
    }

    @Test
    public void testValidate_SingleList_EmptyList() {
        List<ProfileValidation> validations = new ArrayList<>();
        RDAPProfile profile = new RDAPProfile(validations);
        
        boolean result = profile.validate();
        
        assertThat(result).isTrue();
    }

    @Test
    public void testValidate_SingleList_ValidationThrowsException() {
        // Disable parallel networking
        System.setProperty("rdap.parallel.network", "false");
        
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);
        
        when(validation1.getGroupName()).thenReturn("Validation1");
        when(validation2.getGroupName()).thenReturn("Validation2");
        when(validation1.validate()).thenReturn(true);
        when(validation2.validate()).thenThrow(new RuntimeException("Validation error"));
        
        List<ProfileValidation> validations = List.of(validation1, validation2);
        RDAPProfile profile = new RDAPProfile(validations);
        
        boolean result = profile.validate();
        
        // Should handle exceptions gracefully and treat as validation failure
        assertThat(result).isFalse();
        verify(validation1, times(1)).validate();
        verify(validation2, times(1)).validate();
    }

    @Test
    public void testValidate_DefaultParallelProperty() {
        // Don't set the property - should default to false (sequential mode)
        System.clearProperty("rdap.parallel.network");
        
        ProfileValidation validation = mock(ProfileValidation.class);
        when(validation.getGroupName()).thenReturn("TestValidation");
        when(validation.validate()).thenReturn(true);
        
        List<ProfileValidation> validations = List.of(validation);
        RDAPProfile profile = new RDAPProfile(validations);
        
        boolean result = profile.validate();
        
        assertThat(result).isTrue();
        verify(validation, times(1)).validate();
    }

    // Mock classes to help test network validation detection
    public static class MockNetworkValidation extends ProfileValidation {
        public MockNetworkValidation() {
            super(null);
        }
        
        @Override
        protected boolean doValidate() {
            return true;
        }
        
        @Override
        public String getGroupName() {
            return "NetworkValidation";
        }
    }
    
    public static class MockResponseValidation extends ProfileValidation {
        public MockResponseValidation() {
            super(null);
        }
        
        @Override
        protected boolean doValidate() {
            return true;
        }
        
        @Override
        public String getGroupName() {
            return "ResponseValidation";
        }
    }
}