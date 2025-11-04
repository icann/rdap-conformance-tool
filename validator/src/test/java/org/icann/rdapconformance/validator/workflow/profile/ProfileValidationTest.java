package org.icann.rdapconformance.validator.workflow.profile;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

public class ProfileValidationTest {

    private RDAPValidatorResults mockResults;

    @BeforeMethod
    public void setUp() {
        mockResults = mock(RDAPValidatorResults.class);
    }

    @Test
    public void testValidate_DoLaunchFalse_ReturnsTrue() {
        TestProfileValidation validation = new TestProfileValidation(mockResults) {
            public boolean doLaunch() {
                return false;
            }
        };

        boolean result = validation.validate();

        assertThat(result).isTrue();
        verify(mockResults, never()).addGroup("TestGroup");
        verify(mockResults, never()).addGroupErrorWarning("TestGroup");
    }

    @Test
    public void testValidate_DoValidateReturnsTrue_Success() {
        TestProfileValidation validation = new TestProfileValidation(mockResults) {
            protected boolean doValidate() {
                return true;
            }
        };

        boolean result = validation.validate();

        assertThat(result).isTrue();
        verify(mockResults, times(1)).addGroup("TestGroup");
        verify(mockResults, never()).addGroupErrorWarning("TestGroup");
    }

    @Test
    public void testValidate_DoValidateReturnsFalse_Failure() {
        TestProfileValidation validation = new TestProfileValidation(mockResults) {
            protected boolean doValidate() {
                return false;
            }
        };

        boolean result = validation.validate();

        assertThat(result).isFalse();
        verify(mockResults, times(1)).addGroup("TestGroup");
        verify(mockResults, times(1)).addGroupErrorWarning("TestGroup");
    }

    @Test
    public void testValidate_DoValidateThrowsException_Failure() {
        TestProfileValidation validation = new TestProfileValidation(mockResults) {
            protected boolean doValidate() throws Exception {
                throw new RuntimeException("Validation error");
            }
        };

        boolean result = validation.validate();

        assertThat(result).isFalse();
        verify(mockResults, times(1)).addGroup("TestGroup");
        verify(mockResults, times(1)).addGroupErrorWarning("TestGroup");
    }

    @Test
    public void testValidate_DoValidateThrowsCheckedException_Failure() {
        TestProfileValidation validation = new TestProfileValidation(mockResults) {
            protected boolean doValidate() throws Exception {
                throw new Exception("Checked exception");
            }
        };

        boolean result = validation.validate();

        assertThat(result).isFalse();
        verify(mockResults, times(1)).addGroup("TestGroup");
        verify(mockResults, times(1)).addGroupErrorWarning("TestGroup");
    }

    @Test
    public void testValidate_MultipleCallsConsistent() {
        TestProfileValidation validation = new TestProfileValidation(mockResults) {
            protected boolean doValidate() {
                return true;
            }
        };

        boolean result1 = validation.validate();
        boolean result2 = validation.validate();

        assertThat(result1).isTrue();
        assertThat(result2).isTrue();
        verify(mockResults, times(2)).addGroup("TestGroup");
        verify(mockResults, never()).addGroupErrorWarning("TestGroup");
    }

    @Test
    public void testValidate_DefaultDoLaunchReturnsTrue() {
        TestProfileValidation validation = new TestProfileValidation(mockResults) {
            protected boolean doValidate() {
                return true;
            }
        };

        // doLaunch() should return true by default
        assertThat(validation.doLaunch()).isTrue();

        boolean result = validation.validate();

        assertThat(result).isTrue();
        verify(mockResults, times(1)).addGroup("TestGroup");
    }

    @Test
    public void testValidate_NullResults_ThrowsNullPointerException() {
        TestProfileValidation validation = new TestProfileValidation(null) {
            protected boolean doValidate() {
                return true;
            }
        };

        // Should throw NPE when trying to call results.addGroup with null results
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> validation.validate())
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testGetGroupName_ReturnsCorrectName() {
        TestProfileValidation validation = new TestProfileValidation(mockResults);

        String groupName = validation.getGroupName();

        assertThat(groupName).isEqualTo("TestGroup");
    }

    // Test implementation of ProfileValidation for testing purposes
    private static class TestProfileValidation extends ProfileValidation {

        public TestProfileValidation(RDAPValidatorResults results) {
            super(results);
        }

        public String getGroupName() {
            return "TestGroup";
        }

        protected boolean doValidate() throws Exception {
            // Default implementation returns false
            return false;
        }
    }

    // Additional test implementation that can throw different types of exceptions
    private static class ExceptionThrowingValidation extends ProfileValidation {

        private final Exception exceptionToThrow;

        public ExceptionThrowingValidation(RDAPValidatorResults results, Exception exceptionToThrow) {
            super(results);
            this.exceptionToThrow = exceptionToThrow;
        }

        public String getGroupName() {
            return "ExceptionTestGroup";
        }

        protected boolean doValidate() throws Exception {
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
            return true;
        }
    }

    @Test
    public void testValidate_SpecificExceptionTypes() {
        // Test with different exception types
        Exception[] exceptions = {
            new IllegalArgumentException("Invalid argument"),
            new IllegalStateException("Invalid state"),
            new UnsupportedOperationException("Unsupported operation"),
            new RuntimeException("Runtime error")
        };

        for (Exception exception : exceptions) {
            ExceptionThrowingValidation validation = new ExceptionThrowingValidation(mockResults, exception);
            
            boolean result = validation.validate();
            
            assertThat(result).isFalse();
        }

        // Verify that addGroup and addGroupErrorWarning were called for each exception
        verify(mockResults, times(exceptions.length)).addGroup("ExceptionTestGroup");
        verify(mockResults, times(exceptions.length)).addGroupErrorWarning("ExceptionTestGroup");
    }
}