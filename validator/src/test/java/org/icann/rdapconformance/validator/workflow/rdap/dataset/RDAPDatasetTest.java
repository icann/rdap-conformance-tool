package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import jakarta.xml.bind.JAXBException;
import org.icann.rdapconformance.validator.workflow.Deserializer;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.DNSSecAlgNumbers;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPDatasetModel;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class RDAPDatasetTest {

    private FileSystem mockFileSystem;
    private TestableRDAPDataset dataset;

    @BeforeMethod
    public void setUp() {
        mockFileSystem = mock(FileSystem.class);
        dataset = new TestableRDAPDataset(mockFileSystem);
    }

    @Test
    public void testDatasetCreation() {
        assertThat(dataset.getName()).isEqualTo("testDataset");
        assertThat(dataset.getData()).isNotNull();
        assertThat(dataset.getData()).isInstanceOf(TestModel.class);
    }

    @Test
    public void testDownloadWithLocalDatasetExists() throws IOException {
        String expectedPath = "datasets/test.xml";
        when(mockFileSystem.exists(contains(expectedPath))).thenReturn(true);

        boolean result = dataset.download(true);

        assertThat(result).isTrue();
        verify(mockFileSystem).exists(contains(expectedPath));
        verify(mockFileSystem, never()).download(any(URI.class), anyString());
    }

    @Test
    public void testDownloadWithLocalDatasetNotExists() throws IOException {
        String expectedPath = "datasets/test.xml";
        when(mockFileSystem.exists(contains(expectedPath))).thenReturn(false);

        boolean result = dataset.download(true);

        assertThat(result).isTrue();
        verify(mockFileSystem).exists(contains(expectedPath));
        verify(mockFileSystem).download(any(URI.class), contains(expectedPath));
    }

    @Test
    public void testDownloadWithoutLocalDataset() throws IOException {
        String expectedPath = "datasets/test.xml";

        boolean result = dataset.download(false);

        assertThat(result).isTrue();
        verify(mockFileSystem, never()).exists(anyString());
        verify(mockFileSystem).download(any(URI.class), contains(expectedPath));
    }

    @Test
    public void testDownloadFailure() throws IOException {
        String expectedPath = "datasets/test.xml";
        doThrow(new IOException("Network error")).when(mockFileSystem).download(any(URI.class), contains(expectedPath));

        boolean result = dataset.download(false);

        assertThat(result).isFalse();
        verify(mockFileSystem).download(any(URI.class), contains(expectedPath));
    }

    @Test
    public void testParseSuccess() throws Exception {
        TestModel expectedModel = new TestModel();
        dataset.setMockDeserializer(mockDeserializer -> {
            try {
                when(mockDeserializer.deserialize(any(File.class))).thenReturn(expectedModel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        boolean result = dataset.parse();

        assertThat(result).isTrue();
        assertThat(dataset.getData()).isSameAs(expectedModel);
    }

    @Test
    public void testParseIOException() throws Exception {
        dataset.setMockDeserializer(mockDeserializer -> {
            try {
                when(mockDeserializer.deserialize(any(File.class))).thenThrow(new IOException("File read error"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        boolean result = dataset.parse();

        assertThat(result).isFalse();
    }

    @Test
    public void testParseJAXBException() throws Exception {
        dataset.setMockDeserializer(mockDeserializer -> {
            try {
                when(mockDeserializer.deserialize(any(File.class))).thenThrow(new JAXBException("Parse error"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        boolean result = dataset.parse();

        assertThat(result).isFalse();
    }

    // Test for unsupported file extension
    private static class UnsupportedDataset extends RDAPDataset<TestModel> {
        public UnsupportedDataset(FileSystem fileSystem) {
            super("test", URI.create("https://example.com/test.txt"), fileSystem, TestModel.class);
        }
    }

    @Test
    public void testUnsupportedFileExtension() {
        assertThatThrownBy(() -> new UnsupportedDataset(mockFileSystem))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported file type: txt");
    }

    // Test for model instantiation failure  
    private static class InvalidModel implements RDAPDatasetModel {
        // Private constructor to cause instantiation failure
        private InvalidModel() {}
    }

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

    @Test 
    public void testJsonFileExtension() {
        TestableRDAPDataset jsonDataset = new TestableRDAPDataset(
            mockFileSystem, 
            "testJson",
            URI.create("https://example.com/test.json")
        );
        
        assertThat(jsonDataset.getName()).isEqualTo("testJson");
        // Should create without throwing exception for JSON files
    }

    // Helper classes for testing
    public static class TestModel implements RDAPDatasetModel {
        public TestModel() {}
    }

    // Testable implementation that allows mocking the deserializer
    private static class TestableRDAPDataset extends RDAPDataset<TestModel> {
        private Deserializer<TestModel> mockDeserializer;

        public TestableRDAPDataset(FileSystem fileSystem) {
            super("testDataset", URI.create("https://example.com/test.xml"), fileSystem, TestModel.class);
        }

        public TestableRDAPDataset(FileSystem fileSystem, String name, URI uri) {
            super(name, uri, fileSystem, TestModel.class);
        }

        public void setMockDeserializer(java.util.function.Consumer<Deserializer<TestModel>> setup) {
            this.mockDeserializer = mock(Deserializer.class);
            setup.accept(this.mockDeserializer);
        }

        @Override
        public boolean parse() {
            if (mockDeserializer != null) {
                try {
                    TestModel result = mockDeserializer.deserialize(new File("dummy"));
                    // Use reflection to set the modelInstance field
                    java.lang.reflect.Field field = RDAPDataset.class.getDeclaredField("modelInstance");
                    field.setAccessible(true);
                    field.set(this, result);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            return super.parse();
        }
    }
}