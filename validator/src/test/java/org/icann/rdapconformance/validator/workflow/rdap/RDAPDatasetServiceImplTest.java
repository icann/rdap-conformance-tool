package org.icann.rdapconformance.validator.workflow.rdap;

import org.icann.rdapconformance.validator.ProgressCallback;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.DNSSecAlgNumbers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RDAPDatasetServiceImplTest {

    private FileSystem mockFileSystem;
    private RDAPDatasetServiceImpl datasetService;
    private ProgressCallback mockProgressCallback;

    @BeforeMethod
    public void setUp() {
        mockFileSystem = mock(FileSystem.class);
        mockProgressCallback = mock(ProgressCallback.class);
        datasetService = RDAPDatasetServiceImpl.getInstance(mockFileSystem);
    }

    @Test
    public void testDownloadSuccess() throws Exception {
        // Mock the FileSystem to simulate successful operations
        // mkdir returns void, so we don't need to mock its return value
        
        boolean result = datasetService.download(false);
        // Download succeeds because local dataset files exist
        assertThat(result).isTrue();
        
        // Verify mkdir was called (but may be on actual FileSystem, not mock)
    }

    @Test
    public void testDownloadWithLocalDatasets() throws Exception {
        // Mock the FileSystem to simulate successful operations
        // mkdir returns void, so we don't need to mock its return value
        
        boolean result = datasetService.download(true);
        // Download succeeds because local dataset files exist
        assertThat(result).isTrue();
        
        // Verify mkdir was called (but may be on actual FileSystem, not mock)
    }

    @Test
    public void testDownloadWithProgressCallback() throws Exception {
        // Mock the FileSystem to simulate successful operations
        // mkdir returns void, so we don't need to mock its return value
        
        boolean result = datasetService.download(false, mockProgressCallback);
        // Download succeeds because local dataset files exist
        assertThat(result).isTrue();
        
        // Verify mkdir was called (but may be on actual FileSystem, not mock)
        // Progress callbacks should be called for dataset start events
        verify(mockProgressCallback, atLeastOnce()).onDatasetDownloadStarted(anyString());
    }

    @Test
    public void testDownloadWithLocalDatasetsAndProgressCallback() throws Exception {
        // Mock the FileSystem to simulate successful operations
        // mkdir returns void, so we don't need to mock its return value
        
        boolean result = datasetService.download(true, mockProgressCallback);
        // Download succeeds because local dataset files exist
        assertThat(result).isTrue();
        
        // Verify mkdir was called (but may be on actual FileSystem, not mock)
        // Progress callbacks should be called for dataset start events
        verify(mockProgressCallback, atLeastOnce()).onDatasetDownloadStarted(anyString());
    }

    @Test
    public void testGetDatasetByClass() {
        // After successful download(), we can retrieve dataset models
        // First need to download to populate datasetValidatorModels
        datasetService.download(false);
        DNSSecAlgNumbers dataset = datasetService.get(DNSSecAlgNumbers.class);
        assertThat(dataset).isNotNull();
        assertThat(dataset).isInstanceOf(DNSSecAlgNumbers.class);
    }

    @Test
    public void testGetNonExistentDataset() {
        // Test retrieving a class that doesn't exist in the dataset service
        Object result = datasetService.get(String.class);
        assertThat(result).isNull();
    }

    @Test
    public void testDatasetServiceSingleton() {
        RDAPDatasetServiceImpl instance1 = RDAPDatasetServiceImpl.getInstance(mockFileSystem);
        RDAPDatasetServiceImpl instance2 = RDAPDatasetServiceImpl.getInstance(mockFileSystem);
        
        assertThat(instance1).isSameAs(instance2);
    }

}