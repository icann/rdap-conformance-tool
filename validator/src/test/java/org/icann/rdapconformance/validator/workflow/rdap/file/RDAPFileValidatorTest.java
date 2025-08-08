package org.icann.rdapconformance.validator.workflow.rdap.file;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RDAPFileValidatorTest {

    private RDAPValidatorConfiguration mockConfig;
    private RDAPDatasetService mockDatasetService;

    @BeforeMethod
    public void setUp() {
        mockConfig = mock(RDAPValidatorConfiguration.class);
        mockDatasetService = mock(RDAPDatasetService.class);
        
        // Setup mock config to pass validation checks
        when(mockConfig.check()).thenReturn(true);
        when(mockConfig.isNetworkEnabled()).thenReturn(false);
        when(mockConfig.getUri()).thenReturn(URI.create("file:///tmp/test.json"));
    }

    @Test
    public void testConstructor_ValidParameters() {
        RDAPFileValidator validator = new RDAPFileValidator(mockConfig, mockDatasetService);

        assertThat(validator).isNotNull();
        assertThat(validator).isInstanceOf(RDAPValidator.class);
    }

    @Test
    public void testConstructor_NullConfig_ThrowsException() {
        assertThatThrownBy(() -> new RDAPFileValidator(null, mockDatasetService))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void testConstructor_NullDatasetService_CreatesValidator() {
        RDAPFileValidator validator = new RDAPFileValidator(mockConfig, null);
        
        assertThat(validator).isNotNull();
        assertThat(validator).isInstanceOf(RDAPValidator.class);
    }

    @Test
    public void testConstructor_BothParametersNull_ThrowsException() {
        assertThatThrownBy(() -> new RDAPFileValidator(null, null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void testInheritance() {
        RDAPFileValidator validator = new RDAPFileValidator(mockConfig, mockDatasetService);

        assertThat(validator).isInstanceOf(RDAPValidator.class);
    }
}