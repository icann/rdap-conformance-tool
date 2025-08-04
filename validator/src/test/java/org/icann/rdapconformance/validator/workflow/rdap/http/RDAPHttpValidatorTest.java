package org.icann.rdapconformance.validator.workflow.rdap.http;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

public class RDAPHttpValidatorTest {

    private RDAPValidatorConfiguration mockConfig;
    private RDAPDatasetService mockDatasetService;

    @BeforeMethod
    public void setUp() {
        mockConfig = mock(RDAPValidatorConfiguration.class);
        mockDatasetService = mock(RDAPDatasetService.class);
    }

    @Test
    public void testConstructor_ValidParameters() {
        RDAPHttpValidator validator = new RDAPHttpValidator(mockConfig, mockDatasetService);

        assertThat(validator).isNotNull();
        assertThat(validator).isInstanceOf(RDAPValidator.class);
    }

    @Test
    public void testConstructor_NullConfig_ThrowsException() {
        assertThatThrownBy(() -> new RDAPHttpValidator(null, mockDatasetService))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConstructor_NullDatasetService_ThrowsException() {
        assertThatThrownBy(() -> new RDAPHttpValidator(mockConfig, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConstructor_BothParametersNull_ThrowsException() {
        assertThatThrownBy(() -> new RDAPHttpValidator(null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testInheritance() {
        RDAPHttpValidator validator = new RDAPHttpValidator(mockConfig, mockDatasetService);

        assertThat(validator).isInstanceOf(RDAPValidator.class);
    }
}