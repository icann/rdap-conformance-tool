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
    public void testDownloadSuccess() {
        boolean result = datasetService.download(false);
        assertThat(result).isTrue();
    }

    @Test
    public void testDownloadWithLocalDatasets() {
        boolean result = datasetService.download(true);
        assertThat(result).isTrue();
    }

    @Test
    public void testDownloadWithProgressCallback() {
        boolean result = datasetService.download(false, mockProgressCallback);
        assertThat(result).isTrue();
    }

    @Test
    public void testDownloadWithLocalDatasetsAndProgressCallback() {
        boolean result = datasetService.download(true, mockProgressCallback);
        assertThat(result).isTrue();
    }

    @Test
    public void testGetDatasetByClass() {
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