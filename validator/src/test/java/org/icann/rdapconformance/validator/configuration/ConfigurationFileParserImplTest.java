package org.icann.rdapconformance.validator.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import org.testng.annotations.Test;

public class ConfigurationFileParserImplTest {

  private final ConfigurationFileParser configParser = new ConfigurationFileParserImpl();

  @Test
  public void testParse() throws IOException {
    String configStr = "{"
        + "\"definitionIdentifier\": \"gTLD Profile Version 1.0\","
        + "\"definitionError\": [{"
        + "  \"code\": -1102,"
        + "  \"notes\": \"If the gTLD is a legacy gTLD, this may not indicate an error, review by "
        + "a person is required.\""
        + "}],"
        + "\"definitionWarning\": [{"
        + "  \"code\": -2186,"
        + "  \"notes\": \"This only applies for a few gTLDs.\""
        + "}]"
        + ",\"definitionIgnore\": [-2323, -2345, -2346],"
        + "\"definitionNotes\": ["
        + "\"This is a configuration definition for a legacy gTLD.\","
        + "\"Developed by ICANN.\"]}";

    ConfigurationFile expectedConfig = new ConfigurationFile("gTLD Profile Version 1.0",
        List.of(new DefinitionError(-1102,
            "If the gTLD is a legacy gTLD, this may not indicate an error, "
                + "review by a person is required.")),
        List.of(new DefinitionWarning(-2186, "This only applies for a few gTLDs.")),
        List.of(-2323, -2345, -2346),
        List.of("This is a configuration definition for a legacy gTLD.", "Developed by ICANN."),
            false, false, false, false, false);

    ConfigurationFile config = configParser.parse(new ByteArrayInputStream(configStr.getBytes()));

    assertThat(config).usingRecursiveComparison().isEqualTo(expectedConfig);
  }

  @Test
  public void testParse_MissingDefinitionIdentifier_ThrowIOException() throws IOException {
    String configStr = "{"
        + "\"definitionError\": [{"
        + "  \"code\": -1102,"
        + "  \"notes\": \"If the gTLD is a legacy gTLD, this may not indicate an error, review by "
        + "a person is required.\""
        + "}],"
        + "\"definitionWarning\": [{"
        + "  \"code\": -2186,"
        + "  \"notes\": \"This only applies for a few gTLDs.\""
        + "}]"
        + ",\"definitionIgnore\": [-2323, -2345, -2346],"
        + "\"definitionNotes\": ["
        + "\"This is a configuration definition for a legacy gTLD.\","
        + "\"Developed by ICANN.\"]}";

    assertThatExceptionOfType(MismatchedInputException.class)
        .isThrownBy(() -> configParser.parse(new ByteArrayInputStream(configStr.getBytes())))
        .withMessageStartingWith("Missing required creator property 'definitionIdentifier'");
  }

  @Test
  public void testParse_MissingDefinitionErrorNotes_ThrowIOException() throws IOException {
    String configStr = "{"
        + "\"definitionIdentifier\": \"gTLD Profile Version 1.0\","
        + "\"definitionError\": [{"
        + "  \"code\": -1102"
        + "}],"
        + "\"definitionWarning\": [{"
        + "  \"code\": -2186,"
        + "  \"notes\": \"This only applies for a few gTLDs.\""
        + "}]"
        + ",\"definitionIgnore\": [-2323, -2345, -2346],"
        + "\"definitionNotes\": ["
        + "\"This is a configuration definition for a legacy gTLD.\","
        + "\"Developed by ICANN.\"]}";

    assertThatExceptionOfType(MismatchedInputException.class)
        .isThrownBy(() -> configParser.parse(new ByteArrayInputStream(configStr.getBytes())))
        .withMessageStartingWith("Missing required creator property 'notes'");
  }

  @Test
  public void testParse_MandatoryFieldsOnly_IsOk() throws IOException {
    String configStr = "{\"definitionIdentifier\": \"gTLD Profile Version 1.0\"}";

    ConfigurationFile expectedConfig = new ConfigurationFile("gTLD Profile Version 1.0",
        null, null, null, null, false,
            false, false, false, false);

    ConfigurationFile config = configParser.parse(new ByteArrayInputStream(configStr.getBytes()));

    assertThat(config).usingRecursiveComparison().isEqualTo(expectedConfig);
  }

  @Test
  public void testParse_InvalidJson_ThrowsException() {
    String invalidJson = "{ invalid json }";
    
    assertThatExceptionOfType(IOException.class)
        .isThrownBy(() -> configParser.parse(new ByteArrayInputStream(invalidJson.getBytes())));
  }

  @Test
  public void testParse_EmptyJson_ThrowsException() {
    String emptyJson = "{}";
    
    assertThatExceptionOfType(MismatchedInputException.class)
        .isThrownBy(() -> configParser.parse(new ByteArrayInputStream(emptyJson.getBytes())))
        .withMessageStartingWith("Missing required creator property 'definitionIdentifier'");
  }

  @Test
  public void testParse_NullStream_ThrowsException() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> configParser.parse(null));
  }

  @Test
  public void testParse_MalformedJson_ThrowsException() {
    String malformedJson = "{ \"definitionIdentifier\": \"test\", }"; // Trailing comma
    
    assertThatExceptionOfType(IOException.class)
        .isThrownBy(() -> configParser.parse(new ByteArrayInputStream(malformedJson.getBytes())));
  }
}