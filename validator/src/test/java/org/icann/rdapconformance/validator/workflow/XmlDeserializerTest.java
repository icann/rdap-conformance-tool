package org.icann.rdapconformance.validator.workflow;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class XmlDeserializerTest {

    @XmlRootElement
    public static class TestXmlClass {
        private String name;
        private int value;

        public TestXmlClass() {}

        public TestXmlClass(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    private XmlDeserializer<TestXmlClass> deserializer;
    private Path tempDir;

    @BeforeMethod
    public void setUp() throws IOException {
        deserializer = new XmlDeserializer<>(TestXmlClass.class);
        tempDir = Files.createTempDirectory("xml-deserializer-test");
    }

    @Test
    public void testConstructor() {
        XmlDeserializer<String> stringDeserializer = new XmlDeserializer<>(String.class);
        
        assertThat(stringDeserializer).isNotNull();
    }

    @Test
    public void testDeserialize_ValidXml_Success() throws IOException, JAXBException {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<testXmlClass>\n" +
            "    <name>Test Name</name>\n" +
            "    <value>42</value>\n" +
            "</testXmlClass>";
        
        Path xmlFile = tempDir.resolve("test.xml");
        Files.writeString(xmlFile, xmlContent);
        
        TestXmlClass result = deserializer.deserialize(xmlFile.toFile());
        
        assertThat(result.getName()).isEqualTo("Test Name");
        assertThat(result.getValue()).isEqualTo(42);
    }

    @Test
    public void testDeserialize_EmptyXml_Success() throws IOException, JAXBException {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<testXmlClass/>";
        
        Path xmlFile = tempDir.resolve("empty.xml");
        Files.writeString(xmlFile, xmlContent);
        
        TestXmlClass result = deserializer.deserialize(xmlFile.toFile());
        
        assertThat(result).isNotNull();
        assertThat(result.getName()).isNull();
        assertThat(result.getValue()).isEqualTo(0);
    }

    @Test
    public void testDeserialize_NonExistentFile_ThrowsException() {
        File nonExistentFile = new File("/non/existent/file.xml");
        
        assertThatThrownBy(() -> deserializer.deserialize(nonExistentFile))
            .isInstanceOf(Exception.class);
    }

    @Test
    public void testDeserialize_InvalidXml_ThrowsJAXBException() throws IOException {
        String invalidXml = "not valid xml content";
        
        Path xmlFile = tempDir.resolve("invalid.xml");
        Files.writeString(xmlFile, invalidXml);
        
        assertThatThrownBy(() -> deserializer.deserialize(xmlFile.toFile()))
            .isInstanceOf(JAXBException.class);
    }

    @Test
    public void testDeserialize_MalformedXml_ThrowsJAXBException() throws IOException {
        String malformedXml = "<?xml version=\"1.0\"?><testXmlClass><name>Test</name>";
        
        Path xmlFile = tempDir.resolve("malformed.xml");
        Files.writeString(xmlFile, malformedXml);
        
        assertThatThrownBy(() -> deserializer.deserialize(xmlFile.toFile()))
            .isInstanceOf(JAXBException.class);
    }

    @Test
    public void testDeserialize_WrongRootElement_ThrowsJAXBException() throws IOException {
        String wrongXml = "<?xml version=\"1.0\"?><wrongElement><name>Test</name></wrongElement>";
        
        Path xmlFile = tempDir.resolve("wrong.xml");
        Files.writeString(xmlFile, wrongXml);
        
        assertThatThrownBy(() -> deserializer.deserialize(xmlFile.toFile()))
            .isInstanceOf(JAXBException.class);
    }

    @Test
    public void testDeserialize_NullFile_ThrowsException() {
        assertThatThrownBy(() -> deserializer.deserialize(null))
            .isInstanceOf(Exception.class);
    }

    @Test
    public void testDeserialize_Directory_ThrowsException() {
        assertThatThrownBy(() -> deserializer.deserialize(tempDir.toFile()))
            .isInstanceOf(Exception.class);
    }

    @Test
    public void testGenericTypeHandling() {
        XmlDeserializer<TestXmlClass> classDeserializer = new XmlDeserializer<>(TestXmlClass.class);
        XmlDeserializer<String> stringDeserializer = new XmlDeserializer<>(String.class);
        
        assertThat(classDeserializer).isNotNull();
        assertThat(stringDeserializer).isNotNull();
    }
}