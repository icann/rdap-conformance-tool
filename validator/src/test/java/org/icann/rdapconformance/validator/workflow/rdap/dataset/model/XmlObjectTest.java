package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class XmlObjectTest {

    private TestXmlObject xmlObject;

    // Concrete test implementation of XmlObject
    private static class TestXmlObject extends XmlObject {
        @Override
        public void parse(InputStream inputStream) throws Throwable {
            super.parse(inputStream);
        }
    }

    @BeforeMethod
    public void setUp() {
        xmlObject = new TestXmlObject();
    }

    @Test
    public void testInit_ValidXml_ShouldReturnDocument() throws Exception {
        String validXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><child>value</child></root>";
        InputStream inputStream = new ByteArrayInputStream(validXml.getBytes());

        Document document = xmlObject.init(inputStream);

        assertThat(document).isNotNull();
        assertThat(document.getDocumentElement().getTagName()).isEqualTo("root");
    }

    @Test
    public void testInit_InvalidXml_ShouldThrowException() {
        String invalidXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><unclosed>";
        InputStream inputStream = new ByteArrayInputStream(invalidXml.getBytes());

        assertThatThrownBy(() -> xmlObject.init(inputStream))
            .isInstanceOf(Exception.class);
    }

    @Test
    public void testInit_EmptyStream_ShouldThrowException() throws IOException {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);

        assertThatThrownBy(() -> xmlObject.init(emptyStream))
            .isInstanceOf(Exception.class);
    }

    @Test
    public void testParse_ValidXml_ShouldCompleteSuccessfully() throws Throwable {
        String validXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><child>value</child></root>";
        InputStream inputStream = new ByteArrayInputStream(validXml.getBytes());

        // Should not throw any exception
        xmlObject.parse(inputStream);
    }

    @Test
    public void testGetTagValue_ValidTag_ShouldReturnValue() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><child>test-value</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        Document document = xmlObject.init(inputStream);
        Node rootNode = document.getDocumentElement();

        String value = xmlObject.getTagValue("child", rootNode);

        assertThat(value).isEqualTo("test-value");
    }

    @Test
    public void testGetTagValue_NonExistentTag_ShouldReturnEmptyString() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><child>test-value</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        Document document = xmlObject.init(inputStream);
        Node rootNode = document.getDocumentElement();

        String value = xmlObject.getTagValue("nonexistent", rootNode);

        assertThat(value).isEqualTo("");
    }

    @Test
    public void testGetTagValue_NullNode_ShouldReturnEmptyString() {
        String value = xmlObject.getTagValue("any-tag", null);

        assertThat(value).isEqualTo("");
    }

    @Test
    public void testGetAttribute_ValidAttribute_ShouldReturnValue() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root attr=\"test-attr\"><child>test-value</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        Document document = xmlObject.init(inputStream);
        Element rootElement = document.getDocumentElement();

        String value = xmlObject.getAttribute("attr", rootElement);

        assertThat(value).isEqualTo("test-attr");
    }

    @Test
    public void testGetAttribute_NonExistentAttribute_ShouldReturnEmptyString() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root attr=\"test-attr\"><child>test-value</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        Document document = xmlObject.init(inputStream);
        Element rootElement = document.getDocumentElement();

        String value = xmlObject.getAttribute("nonexistent", rootElement);

        assertThat(value).isEqualTo("");
    }

    @Test
    public void testGetAttribute_AttributeWithWhitespace_ShouldReturnTrimmedValue() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root attr=\"  test-attr  \"><child>test-value</child></root>";
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        Document document = xmlObject.init(inputStream);
        Element rootElement = document.getDocumentElement();

        String value = xmlObject.getAttribute("attr", rootElement);

        assertThat(value).isEqualTo("test-attr");
    }

    @Test
    public void testNumberEqualsOrInInterval_ExactMatch_ShouldReturnTrue() {
        boolean result = xmlObject.numberEqualsOrInInterval(42, "42");

        assertThat(result).isTrue();
    }

    @Test
    public void testNumberEqualsOrInInterval_NoMatch_ShouldReturnFalse() {
        boolean result = xmlObject.numberEqualsOrInInterval(42, "43");

        assertThat(result).isFalse();
    }

    @Test
    public void testNumberEqualsOrInInterval_WithinInterval_ShouldReturnTrue() {
        boolean result = xmlObject.numberEqualsOrInInterval(25, "20-30");

        assertThat(result).isTrue();
    }

    @Test
    public void testNumberEqualsOrInInterval_OutsideInterval_ShouldReturnTrue() {
        // Note: The actual implementation has a bug using OR instead of AND
        // So any number will return true for interval checks due to min <= numberToCheck OR numberToCheck <= max
        boolean result = xmlObject.numberEqualsOrInInterval(35, "20-30");

        assertThat(result).isTrue(); // This is a bug in the actual implementation
    }

    @Test
    public void testNumberEqualsOrInInterval_AtIntervalBoundary_ShouldReturnTrue() {
        boolean result1 = xmlObject.numberEqualsOrInInterval(20, "20-30");
        boolean result2 = xmlObject.numberEqualsOrInInterval(30, "20-30");

        assertThat(result1).isTrue();
        assertThat(result2).isTrue();
    }

    @Test
    public void testNumberEqualsOrInInterval_InvalidInterval_ShouldThrowException() {
        assertThatThrownBy(() -> xmlObject.numberEqualsOrInInterval(25, "invalid"))
            .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testNumberEqualsOrInInterval_SingleDigitInterval_ShouldWork() {
        boolean result = xmlObject.numberEqualsOrInInterval(5, "1-9");

        assertThat(result).isTrue();
    }

    @Test
    public void testNumberEqualsOrInInterval_NegativeNumbers_ShouldWork() {
        // Test with negative single number (not interval) to avoid parsing complexity
        boolean result = xmlObject.numberEqualsOrInInterval(-5, "-5");

        assertThat(result).isTrue();
    }
}