package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.DNSSecAlgNumbers;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPDatasetModel;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class RDAPDatasetTest {

    private FileSystem mockFileSystem;
    private DNSSecAlgNumbersDataset dataset;

    @BeforeMethod
    public void setUp() {
        mockFileSystem = mock(FileSystem.class);
        dataset = new DNSSecAlgNumbersDataset(mockFileSystem);
    }

    @Test
    public void testDatasetCreation() {
        assertThat(dataset.getName()).isEqualTo("dnsSecAlgNumbers");
        assertThat(dataset.getData()).isNotNull();
        assertThat(dataset.getData()).isInstanceOf(DNSSecAlgNumbers.class);
    }

    @Test
    public void testDownloadWithLocalDatasetExists() throws IOException {
        String expectedPath = "datasets/dns-sec-alg-numbers.xml";
        when(mockFileSystem.exists(contains(expectedPath))).thenReturn(true);

        boolean result = dataset.download(true);

        assertThat(result).isTrue();
        verify(mockFileSystem).exists(contains(expectedPath));
        verify(mockFileSystem, never()).download(any(URI.class), anyString());
    }

    @Test
    public void testDownloadWithLocalDatasetNotExists() throws IOException {
        String expectedPath = "datasets/dns-sec-alg-numbers.xml";
        when(mockFileSystem.exists(contains(expectedPath))).thenReturn(false);

        boolean result = dataset.download(true);

        assertThat(result).isTrue();
        verify(mockFileSystem).exists(contains(expectedPath));
        verify(mockFileSystem).download(any(URI.class), contains(expectedPath));
    }

    @Test
    public void testDownloadWithoutLocalDataset() throws IOException {
        String expectedPath = "datasets/dns-sec-alg-numbers.xml";

        boolean result = dataset.download(false);

        assertThat(result).isTrue();
        verify(mockFileSystem, never()).exists(anyString());
        verify(mockFileSystem).download(any(URI.class), contains(expectedPath));
    }

    @Test
    public void testDownloadFailure() throws IOException {
        String expectedPath = "datasets/dns-sec-alg-numbers.xml";
        doThrow(new IOException("Network error")).when(mockFileSystem).download(any(URI.class), contains(expectedPath));

        boolean result = dataset.download(false);

        assertThat(result).isFalse();
        verify(mockFileSystem).download(any(URI.class), contains(expectedPath));
    }

    @Test
    public void testParseSuccess() throws Exception {
        // The dataset file exists in the main datasets directory
        // This tests the successful parsing path
        boolean result = dataset.parse();

        // The parse should succeed because the actual XML file exists
        assertThat(result).isTrue();
    }

    // Test for unsupported file extension
    private static class UnsupportedDataset extends RDAPDataset<DNSSecAlgNumbers> {
        public UnsupportedDataset(FileSystem fileSystem) {
            super("test", URI.create("https://example.com/test.txt"), fileSystem, DNSSecAlgNumbers.class);
        }
    }

    @Test
    public void testUnsupportedFileExtension() {
        assertThatThrownBy(() -> new UnsupportedDataset(mockFileSystem))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported file type: txt");
    }

    // Test for model instantiation failure  
    private static class InvalidModel implements RDAPDatasetModel {}

    private static class InvalidDataset extends RDAPDataset<InvalidModel> {
        public InvalidDataset(FileSystem fileSystem) {
            super("test", URI.create("https://example.com/test.xml"), fileSystem, InvalidModel.class);
        }
    }

    @Test
    public void testModelInstantiationFailure() {
        assertThatThrownBy(() -> new InvalidDataset(mockFileSystem))
                .isInstanceOf(RuntimeException.class);
    }
}