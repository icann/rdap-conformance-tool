package org.icann.rdapconformance.validator.workflow.rdap.file;

import org.icann.rdapconformance.validator.ToolResult;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RDAPFileQueryTypeProcessorTest {

    private RDAPValidatorConfiguration mockConfig;
    private RDAPDatasetService mockDatasetService;

    @BeforeMethod
    public void setUp() {
        mockConfig = mock(RDAPValidatorConfiguration.class);
        mockDatasetService = mock(RDAPDatasetService.class);
        
        when(mockConfig.getQueryType()).thenReturn(RDAPQueryType.DOMAIN);
    }

    @Test
    public void testGetInstance_ReturnsSingleton() {
        RDAPFileQueryTypeProcessor processor1 = RDAPFileQueryTypeProcessor.getInstance(mockConfig);
        RDAPFileQueryTypeProcessor processor2 = RDAPFileQueryTypeProcessor.getInstance(mockConfig);
        
        assertThat(processor1).isSameAs(processor2);
        assertThat(processor1).isNotNull();
    }

    @Test
    public void testGetInstance_WithDifferentConfigs_ReturnsSameInstance() {
        RDAPValidatorConfiguration config1 = mock(RDAPValidatorConfiguration.class);
        RDAPValidatorConfiguration config2 = mock(RDAPValidatorConfiguration.class);
        
        when(config1.getQueryType()).thenReturn(RDAPQueryType.DOMAIN);
        when(config2.getQueryType()).thenReturn(RDAPQueryType.ENTITY);
        
        RDAPFileQueryTypeProcessor processor1 = RDAPFileQueryTypeProcessor.getInstance(config1);
        RDAPFileQueryTypeProcessor processor2 = RDAPFileQueryTypeProcessor.getInstance(config2);
        
        assertThat(processor1).isSameAs(processor2);
        // But the configuration should be updated to the latest one
        assertThat(processor2.getQueryType()).isEqualTo(RDAPQueryType.ENTITY);
    }

    @Test
    public void testSetConfiguration_UpdatesConfig() {
        RDAPFileQueryTypeProcessor processor = RDAPFileQueryTypeProcessor.getInstance(mockConfig);
        
        RDAPValidatorConfiguration newConfig = mock(RDAPValidatorConfiguration.class);
        when(newConfig.getQueryType()).thenReturn(RDAPQueryType.NAMESERVER);
        
        processor.setConfiguration(newConfig);
        
        assertThat(processor.getQueryType()).isEqualTo(RDAPQueryType.NAMESERVER);
    }

    @Test
    public void testCheck_AlwaysReturnsTrue() {
        RDAPFileQueryTypeProcessor processor = RDAPFileQueryTypeProcessor.getInstance(mockConfig);
        
        boolean result = processor.check(mockDatasetService);
        
        assertThat(result).isTrue();
    }

    @Test
    public void testCheck_WithNullDatasetService_ReturnsTrue() {
        RDAPFileQueryTypeProcessor processor = RDAPFileQueryTypeProcessor.getInstance(mockConfig);
        
        boolean result = processor.check(null);
        
        assertThat(result).isTrue();
    }

    @Test
    public void testGetErrorStatus_ReturnsNull() {
        RDAPFileQueryTypeProcessor processor = RDAPFileQueryTypeProcessor.getInstance(mockConfig);
        
        ToolResult errorStatus = processor.getErrorStatus();
        
        assertThat(errorStatus).isNull();
    }

    @Test
    public void testGetQueryType_ReturnsConfiguredQueryType() {
        RDAPFileQueryTypeProcessor processor = RDAPFileQueryTypeProcessor.getInstance(mockConfig);
        
        RDAPQueryType queryType = processor.getQueryType();
        
        assertThat(queryType).isEqualTo(RDAPQueryType.DOMAIN);
    }

    @Test
    public void testGetQueryType_AfterConfigurationChange() {
        RDAPFileQueryTypeProcessor processor = RDAPFileQueryTypeProcessor.getInstance(mockConfig);
        
        // Change the configuration
        when(mockConfig.getQueryType()).thenReturn(RDAPQueryType.IP_NETWORK);
        processor.setConfiguration(mockConfig);
        
        RDAPQueryType queryType = processor.getQueryType();
        
        assertThat(queryType).isEqualTo(RDAPQueryType.IP_NETWORK);
    }

    @Test
    public void testGetQueryType_WithNullConfig_ThrowsException() {
        RDAPFileQueryTypeProcessor processor = RDAPFileQueryTypeProcessor.getInstance(mockConfig);
        processor.setConfiguration(null);
        
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> processor.getQueryType())
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testGetInstance_WithNullConfig() {
        RDAPFileQueryTypeProcessor processor = RDAPFileQueryTypeProcessor.getInstance(null);
        
        assertThat(processor).isNotNull();
        
        // Getting query type should throw NPE with null config
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> processor.getQueryType())
            .isInstanceOf(NullPointerException.class);
    }
}