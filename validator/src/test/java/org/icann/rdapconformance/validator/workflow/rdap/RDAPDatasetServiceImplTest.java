package org.icann.rdapconformance.validator.workflow.rdap;

import org.icann.rdapconformance.validator.ProgressCallback;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.DNSSecAlgNumbers;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class RDAPDatasetServiceImplTest {

    private FileSystem mockFileSystem;
    private RDAPDatasetServiceImpl datasetService;
    private ProgressCallback mockProgressCallback;

    @BeforeMethod
    public void setUp() {
        // Clear the singleton instance before each test
        clearSingletonInstance();
        
        mockFileSystem = mock(FileSystem.class);
        mockProgressCallback = mock(ProgressCallback.class);
        datasetService = RDAPDatasetServiceImpl.getInstance(mockFileSystem);
    }
    
    @AfterMethod
    public void tearDown() {
        // Clear singleton instance after each test
        clearSingletonInstance();
    }
    
    private void clearSingletonInstance() {
        try {
            java.lang.reflect.Field instanceField = RDAPDatasetServiceImpl.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            // Ignore if field access fails
        }
    }

    @Test
    public void testDownloadSuccess() throws Exception {
        // Mock filesystem operations to succeed
        doNothing().when(mockFileSystem).mkdir(anyString());
        doNothing().when(mockFileSystem).download(any(URI.class), anyString());
        
        boolean result = datasetService.download(false);
        
        assertThat(result).isTrue();
        verify(mockFileSystem, atLeastOnce()).mkdir(anyString());
        verify(mockFileSystem, atLeastOnce()).download(any(URI.class), anyString());
    }

    @Test
    public void testDownloadWithLocalDatasets() throws Exception {
        // Mock filesystem operations - all files exist locally
        doNothing().when(mockFileSystem).mkdir(anyString());
        when(mockFileSystem.exists(anyString())).thenReturn(true);
        
        boolean result = datasetService.download(true);
        
        assertThat(result).isTrue();
        verify(mockFileSystem, atLeastOnce()).mkdir(anyString());
        // Should not download if files exist locally
        verify(mockFileSystem, never()).download(any(URI.class), anyString());
    }

    @Test
    public void testDownloadWithProgressCallback() throws Exception {
        // Mock filesystem operations to succeed
        doNothing().when(mockFileSystem).mkdir(anyString());
        doNothing().when(mockFileSystem).download(any(URI.class), anyString());
        
        boolean result = datasetService.download(false, mockProgressCallback);
        
        assertThat(result).isTrue();
        verify(mockFileSystem, atLeastOnce()).mkdir(anyString());
        verify(mockFileSystem, atLeastOnce()).download(any(URI.class), anyString());
        verify(mockProgressCallback, atLeastOnce()).onDatasetDownloadStarted(anyString());
    }

    @Test
    public void testDownloadWithLocalDatasetsAndProgressCallback() throws Exception {
        // Mock filesystem operations - all files exist locally
        doNothing().when(mockFileSystem).mkdir(anyString());
        when(mockFileSystem.exists(anyString())).thenReturn(true);
        
        boolean result = datasetService.download(true, mockProgressCallback);
        
        assertThat(result).isTrue();
        verify(mockFileSystem, atLeastOnce()).mkdir(anyString());
        // Should not download if files exist locally
        verify(mockFileSystem, never()).download(any(URI.class), anyString());
        verify(mockProgressCallback, atLeastOnce()).onDatasetDownloadStarted(anyString());
    }

    @Test
    public void testGetDatasetByClass() {
        // Before parsing, datasetValidatorModels is null, so get() throws NPE
        assertThatThrownBy(() -> datasetService.get(DNSSecAlgNumbers.class))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testGetNonExistentDataset() {
        // Before parsing, datasetValidatorModels is null, so get() throws NPE
        assertThatThrownBy(() -> datasetService.get(String.class))
            .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    public void testDownloadFailure() throws Exception {
        // Mock filesystem operations to fail
        doNothing().when(mockFileSystem).mkdir(anyString());
        doThrow(new IOException("Network error")).when(mockFileSystem).download(any(URI.class), anyString());
        
        boolean result = datasetService.download(false);
        
        assertThat(result).isFalse();
        verify(mockFileSystem, atLeastOnce()).mkdir(anyString());
        verify(mockFileSystem, atLeastOnce()).download(any(URI.class), anyString());
    }
    
    @Test
    public void testGetInstanceWithoutFileSystem() {
        clearSingletonInstance();
        
        assertThatThrownBy(() -> RDAPDatasetServiceImpl.getInstance())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("RDAPDatasetServiceImpl has not been initialized");
    }

    @Test
    public void testDatasetServiceSingleton() {
        RDAPDatasetServiceImpl instance1 = RDAPDatasetServiceImpl.getInstance(mockFileSystem);
        RDAPDatasetServiceImpl instance2 = RDAPDatasetServiceImpl.getInstance(mockFileSystem);
        
        assertThat(instance1).isSameAs(instance2);
    }

}