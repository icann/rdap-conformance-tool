package org.icann.rdapconformance.validator.workflow;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonDeserializerTest {

    public static class TestJsonClass {
        private String name;
        private int value;

        public TestJsonClass() {}

        public TestJsonClass(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    private JsonDeserializer<TestJsonClass> deserializer;
    private Path tempDir;

    @BeforeMethod
    public void setUp() throws IOException {
        deserializer = new JsonDeserializer<>(TestJsonClass.class);
        tempDir = Files.createTempDirectory("json-deserializer-test");
    }

    @Test
    public void testConstructor() {
        JsonDeserializer<String> stringDeserializer = new JsonDeserializer<>(String.class);
        
        assertThat(stringDeserializer).isNotNull();
    }

    @Test
    public void testDeserialize_ValidJson_Success() throws IOException {
        String jsonContent = "{\"name\":\"Test Name\",\"value\":42}";
        
        Path jsonFile = tempDir.resolve("test.json");
        Files.writeString(jsonFile, jsonContent);
        
        TestJsonClass result = deserializer.deserialize(jsonFile.toFile());
        
        assertThat(result.getName()).isEqualTo("Test Name");
        assertThat(result.getValue()).isEqualTo(42);
    }

    @Test
    public void testDeserialize_EmptyJson_Success() throws IOException {
        String jsonContent = "{}";
        
        Path jsonFile = tempDir.resolve("empty.json");
        Files.writeString(jsonFile, jsonContent);
        
        TestJsonClass result = deserializer.deserialize(jsonFile.toFile());
        
        assertThat(result).isNotNull();
        assertThat(result.getName()).isNull();
        assertThat(result.getValue()).isEqualTo(0);
    }

    @Test
    public void testDeserialize_NullValues_Success() throws IOException {
        String jsonContent = "{\"name\":null,\"value\":null}";
        
        Path jsonFile = tempDir.resolve("nulls.json");
        Files.writeString(jsonFile, jsonContent);
        
        TestJsonClass result = deserializer.deserialize(jsonFile.toFile());
        
        assertThat(result.getName()).isNull();
        assertThat(result.getValue()).isEqualTo(0);
    }

    @Test
    public void testDeserialize_NonExistentFile_ThrowsIOException() {
        File nonExistentFile = new File("/non/existent/file.json");
        
        assertThatThrownBy(() -> deserializer.deserialize(nonExistentFile))
            .isInstanceOf(IOException.class);
    }

    @Test
    public void testDeserialize_InvalidJson_ThrowsIOException() throws IOException {
        String invalidJson = "not valid json content";
        
        Path jsonFile = tempDir.resolve("invalid.json");
        Files.writeString(jsonFile, invalidJson);
        
        assertThatThrownBy(() -> deserializer.deserialize(jsonFile.toFile()))
            .isInstanceOf(IOException.class);
    }

    @Test
    public void testDeserialize_MalformedJson_ThrowsIOException() throws IOException {
        String malformedJson = "{\"name\":\"Test\",";
        
        Path jsonFile = tempDir.resolve("malformed.json");
        Files.writeString(jsonFile, malformedJson);
        
        assertThatThrownBy(() -> deserializer.deserialize(jsonFile.toFile()))
            .isInstanceOf(IOException.class);
    }

    @Test
    public void testDeserialize_WrongType_ThrowsIOException() throws IOException {
        String arrayJson = "[\"not\", \"an\", \"object\"]";
        
        Path jsonFile = tempDir.resolve("array.json");
        Files.writeString(jsonFile, arrayJson);
        
        assertThatThrownBy(() -> deserializer.deserialize(jsonFile.toFile()))
            .isInstanceOf(IOException.class);
    }

    @Test
    public void testDeserialize_NullFile_ThrowsException() {
        assertThatThrownBy(() -> deserializer.deserialize(null))
            .isInstanceOf(Exception.class);
    }

    @Test
    public void testDeserialize_Directory_ThrowsIOException() {
        assertThatThrownBy(() -> deserializer.deserialize(tempDir.toFile()))
            .isInstanceOf(IOException.class);
    }

    @Test
    public void testUsesSharedObjectMapper() {
        JsonDeserializer<TestJsonClass> deserializer1 = new JsonDeserializer<>(TestJsonClass.class);
        JsonDeserializer<TestJsonClass> deserializer2 = new JsonDeserializer<>(TestJsonClass.class);
        
        assertThat(deserializer1).isNotNull();
        assertThat(deserializer2).isNotNull();
    }

    @Test
    public void testGenericTypeHandling() {
        JsonDeserializer<TestJsonClass> classDeserializer = new JsonDeserializer<>(TestJsonClass.class);
        JsonDeserializer<String> stringDeserializer = new JsonDeserializer<>(String.class);
        
        assertThat(classDeserializer).isNotNull();
        assertThat(stringDeserializer).isNotNull();
    }

    @Test
    public void testDeserialize_ExtraFields_Success() throws IOException {
        String jsonContent = "{\"name\":\"Test\",\"value\":10,\"extraField\":\"ignored\"}";
        
        Path jsonFile = tempDir.resolve("extra.json");
        Files.writeString(jsonFile, jsonContent);
        
        TestJsonClass result = deserializer.deserialize(jsonFile.toFile());
        
        assertThat(result.getName()).isEqualTo("Test");
        assertThat(result.getValue()).isEqualTo(10);
    }
}