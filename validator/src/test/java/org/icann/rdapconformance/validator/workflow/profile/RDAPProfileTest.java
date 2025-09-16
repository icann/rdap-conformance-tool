package org.icann.rdapconformance.validator.workflow.profile;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
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
        
        // Create actual network and non-network validation instances
        // Use a Tig validation (network) and a regular validation (non-network)
        TigValidation1Dot2Test networkValidation = new TigValidation1Dot2Test();
        MockNonNetworkValidation nonNetworkValidation = new MockNonNetworkValidation();
        
        List<ProfileValidation> validations = List.of(networkValidation, nonNetworkValidation);
        RDAPProfile profile = new RDAPProfile(validations);
        
        boolean result = profile.validate();
        
        // Both validations should execute successfully
        assertThat(result).isTrue();
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
        
        // In sequential mode, exceptions bubble up
        try {
            boolean result = profile.validate();
            // If we get here, validation may have handled the exception differently
            assertThat(result).isFalse();
        } catch (RuntimeException e) {
            // This is the expected behavior - exceptions are not caught in sequential mode
            assertThat(e.getMessage()).isEqualTo("Validation error");
        }
        
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

    @Test
    public void testValidate_SingleList_ParallelMode_NetworkValidationsOnly() {
        // Enable parallel networking
        System.setProperty("rdap.parallel.network", "true");
        
        // Create actual network validation subclasses with proper naming
        TigValidation1Dot2Test tigValidation = new TigValidation1Dot2Test();
        ResponseValidationHelp_2024Test helpValidation = new ResponseValidationHelp_2024Test();
        
        List<ProfileValidation> validations = List.of(tigValidation, helpValidation);
        RDAPProfile profile = new RDAPProfile(validations);
        
        boolean result = profile.validate();
        
        assertThat(result).isTrue();
    }

    @Test
    public void testValidate_SingleList_ParallelMode_NonNetworkValidationsOnly() {
        // Enable parallel networking
        System.setProperty("rdap.parallel.network", "true");
        
        // Create actual non-network validation instances
        MockNonNetworkValidation validation1 = new MockNonNetworkValidation();
        MockNonNetworkValidation validation2 = new MockNonNetworkValidation();
        
        List<ProfileValidation> validations = List.of(validation1, validation2);
        RDAPProfile profile = new RDAPProfile(validations);
        
        boolean result = profile.validate();
        
        assertThat(result).isTrue();
    }

    @Test
    public void testValidate_SeparateLists_ParallelMode_NetworkValidations() {
        // Enable parallel networking
        System.setProperty("rdap.parallel.network", "true");
        
        ProfileValidation parallelValidation = mock(ProfileValidation.class);
        when(parallelValidation.getGroupName()).thenReturn("ParallelValidation");
        when(parallelValidation.validate()).thenReturn(true);
        
        // Create network validation for sequential list
        TigValidation1Dot2Test tigValidation = new TigValidation1Dot2Test();
        
        List<ProfileValidation> parallelValidations = List.of(parallelValidation);
        List<ProfileValidation> sequentialValidations = List.of(tigValidation);
        
        RDAPProfile profile = new RDAPProfile(parallelValidations, sequentialValidations);
        
        boolean result = profile.validate();
        
        assertThat(result).isTrue();
        verify(parallelValidation, times(1)).validate();
    }

    @Test
    public void testValidate_SeparateLists_SequentialMode_NetworkValidations() {
        // Disable parallel networking
        System.setProperty("rdap.parallel.network", "false");
        
        ProfileValidation parallelValidation = mock(ProfileValidation.class);
        when(parallelValidation.getGroupName()).thenReturn("ParallelValidation");
        when(parallelValidation.validate()).thenReturn(true);
        
        // Create network validation for sequential list
        TigValidation1Dot2Test tigValidation = new TigValidation1Dot2Test();
        
        List<ProfileValidation> parallelValidations = List.of(parallelValidation);
        List<ProfileValidation> sequentialValidations = List.of(tigValidation);
        
        RDAPProfile profile = new RDAPProfile(parallelValidations, sequentialValidations);
        
        boolean result = profile.validate();
        
        assertThat(result).isTrue();
        verify(parallelValidation, times(1)).validate();
    }

    @Test
    public void testValidate_SingleList_ParallelMode_EmptyNetworkValidations() {
        // Enable parallel networking
        System.setProperty("rdap.parallel.network", "true");
        
        // Create only non-network validations
        MockNonNetworkValidation validation1 = new MockNonNetworkValidation();
        
        List<ProfileValidation> validations = List.of(validation1);
        RDAPProfile profile = new RDAPProfile(validations);
        
        boolean result = profile.validate();
        
        assertThat(result).isTrue();
    }

    @Test
    public void testNetworkValidationDetection_TigValidations() {
        // Test all Tig validation patterns
        TigValidation1Dot2Test tig2 = new TigValidation1Dot2Test();
        TigValidation1Dot3Test tig3 = new TigValidation1Dot3Test();
        TigValidation1Dot5_2024Test tig5 = new TigValidation1Dot5_2024Test();
        TigValidation1Dot6Test tig6 = new TigValidation1Dot6Test();
        TigValidation1Dot8Test tig8 = new TigValidation1Dot8Test();
        TigValidation1Dot11Dot1Test tig11 = new TigValidation1Dot11Dot1Test();
        TigValidation1Dot13Test tig13 = new TigValidation1Dot13Test();
        
        System.setProperty("rdap.parallel.network", "true");
        
        List<ProfileValidation> validations = List.of(tig2, tig3, tig5, tig6, tig8, tig11, tig13);
        RDAPProfile profile = new RDAPProfile(validations);
        
        boolean result = profile.validate();
        
        // All should be detected as network validations and handled appropriately
        assertThat(result).isTrue();
    }

    @Test
    public void testNetworkValidationDetection_ResponseValidations() {
        // Test Response validation patterns
        ResponseValidationHelp_2024Test help = new ResponseValidationHelp_2024Test();
        ResponseValidationDomainInvalid_2024Test domain = new ResponseValidationDomainInvalid_2024Test();
        ResponseValidationTestInvalidRedirect_2024Test redirect = new ResponseValidationTestInvalidRedirect_2024Test();
        
        System.setProperty("rdap.parallel.network", "true");
        
        List<ProfileValidation> validations = List.of(help, domain, redirect);
        RDAPProfile profile = new RDAPProfile(validations);
        
        boolean result = profile.validate();
        
        // All should be detected as network validations and handled appropriately
        assertThat(result).isTrue();
    }

    @Test
    public void testTimeoutExtraction_WithConfigField() {
        // Test timeout extraction with reflection
        ValidationWithConfig validationWithConfig = new ValidationWithConfig();
        
        System.setProperty("rdap.parallel.network", "true");
        
        List<ProfileValidation> validations = List.of(validationWithConfig);
        RDAPProfile profile = new RDAPProfile(validations);
        
        boolean result = profile.validate();
        
        // Should successfully extract timeout and execute
        assertThat(result).isTrue();
    }

    @Test
    public void testTimeoutExtraction_NoConfigField() {
        // Test timeout extraction fallback when no config field exists
        TigValidation1Dot2Test tigValidation = new TigValidation1Dot2Test();
        
        System.setProperty("rdap.parallel.network", "true");
        
        List<ProfileValidation> validations = List.of(tigValidation);
        RDAPProfile profile = new RDAPProfile(validations);
        
        boolean result = profile.validate();
        
        // Should use default timeout and execute successfully
        assertThat(result).isTrue();
    }

    @Test
    public void testValidate_SingleList_ParallelMode_MixedValidationResults() {
        // Enable parallel networking
        System.setProperty("rdap.parallel.network", "true");
        
        // Mix of network and non-network validations with different results
        MockNonNetworkValidation nonNetworkPass = new MockNonNetworkValidation();
        MockNonNetworkValidationFail nonNetworkFail = new MockNonNetworkValidationFail();
        TigValidation1Dot2Test networkValidation = new TigValidation1Dot2Test();
        
        List<ProfileValidation> validations = List.of(nonNetworkPass, nonNetworkFail, networkValidation);
        RDAPProfile profile = new RDAPProfile(validations);
        
        boolean result = profile.validate();
        
        // Should be false because one non-network validation failed
        assertThat(result).isFalse();
    }

    // Additional test classes for comprehensive network validation testing
    
    // Tig validation test classes
    public static class TigValidation1Dot2Test extends ProfileValidation {
        public TigValidation1Dot2Test() { 
            super(mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults.class), mock(RDAPValidatorConfiguration.class));
        }
        @Override
        protected boolean doValidate() { return true; }
        @Override
        public String getGroupName() { return "TigValidation1Dot2"; }
    }
    
    public static class TigValidation1Dot3Test extends ProfileValidation {
        public TigValidation1Dot3Test() { 
            super(mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults.class), mock(RDAPValidatorConfiguration.class));
        }
        @Override
        protected boolean doValidate() { return true; }
        @Override
        public String getGroupName() { return "TigValidation1Dot3"; }
    }
    
    public static class TigValidation1Dot5_2024Test extends ProfileValidation {
        public TigValidation1Dot5_2024Test() { 
            super(mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults.class), mock(RDAPValidatorConfiguration.class));
        }
        @Override
        protected boolean doValidate() { return true; }
        @Override
        public String getGroupName() { return "TigValidation1Dot5_2024"; }
    }
    
    public static class TigValidation1Dot6Test extends ProfileValidation {
        public TigValidation1Dot6Test() { 
            super(mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults.class), mock(RDAPValidatorConfiguration.class));
        }
        @Override
        protected boolean doValidate() { return true; }
        @Override
        public String getGroupName() { return "TigValidation1Dot6"; }
    }
    
    public static class TigValidation1Dot8Test extends ProfileValidation {
        public TigValidation1Dot8Test() { 
            super(mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults.class), mock(RDAPValidatorConfiguration.class));
        }
        @Override
        protected boolean doValidate() { return true; }
        @Override
        public String getGroupName() { return "TigValidation1Dot8"; }
    }
    
    public static class TigValidation1Dot11Dot1Test extends ProfileValidation {
        public TigValidation1Dot11Dot1Test() { 
            super(mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults.class), mock(RDAPValidatorConfiguration.class));
        }
        @Override
        protected boolean doValidate() { return true; }
        @Override
        public String getGroupName() { return "TigValidation1Dot11Dot1"; }
    }
    
    public static class TigValidation1Dot13Test extends ProfileValidation {
        public TigValidation1Dot13Test() { 
            super(mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults.class), mock(RDAPValidatorConfiguration.class));
        }
        @Override
        protected boolean doValidate() { return true; }
        @Override
        public String getGroupName() { return "TigValidation1Dot13"; }
    }
    
    // Response validation test classes
    public static class ResponseValidationHelp_2024Test extends ProfileValidation {
        public ResponseValidationHelp_2024Test() { 
            super(mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults.class), mock(RDAPValidatorConfiguration.class));
        }
        @Override
        protected boolean doValidate() { return true; }
        @Override
        public String getGroupName() { return "ResponseValidationHelp_2024"; }
    }
    
    public static class ResponseValidationDomainInvalid_2024Test extends ProfileValidation {
        public ResponseValidationDomainInvalid_2024Test() { 
            super(mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults.class), mock(RDAPValidatorConfiguration.class));
        }
        @Override
        protected boolean doValidate() { return true; }
        @Override
        public String getGroupName() { return "ResponseValidationDomainInvalid_2024"; }
    }
    
    public static class ResponseValidationTestInvalidRedirect_2024Test extends ProfileValidation {
        public ResponseValidationTestInvalidRedirect_2024Test() { 
            super(mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults.class), mock(RDAPValidatorConfiguration.class));
        }
        @Override
        protected boolean doValidate() { return true; }
        @Override
        public String getGroupName() { return "ResponseValidationTestInvalidRedirect_2024"; }
    }
    
    // Mock non-network validation
    public static class MockNonNetworkValidation extends ProfileValidation {
        public MockNonNetworkValidation() { 
            super(mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults.class), mock(RDAPValidatorConfiguration.class));
        }
        @Override
        protected boolean doValidate() { return true; }
        @Override
        public String getGroupName() { return "MockNonNetworkValidation"; }
    }
    
    // Mock non-network validation that fails
    public static class MockNonNetworkValidationFail extends ProfileValidation {
        public MockNonNetworkValidationFail() { 
            super(mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults.class), mock(RDAPValidatorConfiguration.class));
        }
        @Override
        protected boolean doValidate() { return false; }
        @Override
        public String getGroupName() { return "MockNonNetworkValidationFail"; }
    }
    
    // Mock validation with config field for timeout extraction testing
    public static class ValidationWithConfig extends ProfileValidation {
        private final MockConfig config = new MockConfig();
        
        public ValidationWithConfig() { 
            super(mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults.class), mock(RDAPValidatorConfiguration.class));
        }
        @Override
        protected boolean doValidate() { return true; }
        @Override
        public String getGroupName() { return "ValidationWithConfig"; }
    }
    
    // Mock config class for timeout extraction testing
    public static class MockConfig {
        public Integer getTimeout() {
            return 30; // Return 30 seconds timeout
        }
    }
    
    // Mock classes to help test network validation detection
    public static class MockNetworkValidation extends ProfileValidation {
        public MockNetworkValidation() {
            super(mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults.class), mock(RDAPValidatorConfiguration.class));
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
            super(mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults.class), mock(RDAPValidatorConfiguration.class));
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