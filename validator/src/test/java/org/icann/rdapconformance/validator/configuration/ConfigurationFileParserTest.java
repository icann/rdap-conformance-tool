package org.icann.rdapconformance.validator.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.json.JSONException;
import org.testng.annotations.Test;

public class ConfigurationFileParserTest {

  private final FileSystem fs = mock(FileSystem.class);
  private final ConfigurationFileParser configParser = new ConfigurationFileParser(fs);

  @Test
  public void testParse() throws IOException {
    doReturn("{"
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
        + "\"Developed by ICANN.\"]}").when(fs).readFile(any());

    ConfigurationFile expectedConfig = ConfigurationFile.builder()
        .definitionIdentifier("gTLD Profile Version 1.0")
        .definitionError(List.of(DefinitionAlerts.builder()
            .code(-1102)
            .notes(
                "If the gTLD is a legacy gTLD, this may not indicate an error, review by a person is required.")
            .build()))
        .definitionWarning(List.of(DefinitionAlerts.builder()
            .code(-2186)
            .notes("This only applies for a few gTLDs.")
            .build()))
        .definitionIgnore(List.of(-2323, -2345, -2346))
        .definitionNotes(List.of("This is a configuration definition for a legacy gTLD.",
            "Developed by ICANN."))
        .build();

    ConfigurationFile config = configParser.parse(new File("config"));

    assertThat(config).usingRecursiveComparison().isEqualTo(expectedConfig);
  }

  @Test
  public void testParse_MissingDefinitionIdentifier_ThrowIOException() throws IOException {
    doReturn("{"
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
        + "\"Developed by ICANN.\"]}").when(fs).readFile(any());

    assertThatExceptionOfType(JSONException.class)
        .isThrownBy(() -> configParser.parse(new File("config")))
        .withMessage("JSONObject[\"definitionIdentifier\"] not found.");
  }

  @Test
  public void testParse_MandatoryFieldsOnly_IsOk() throws IOException {
    doReturn("{\"definitionIdentifier\": \"gTLD Profile Version 1.0\"}").when(fs).readFile(any());

    ConfigurationFile expectedConfig = ConfigurationFile.builder()
        .definitionIdentifier("gTLD Profile Version 1.0")
        .build();

    ConfigurationFile config = configParser.parse(new File("config"));

    assertThat(config).usingRecursiveComparison().isEqualTo(expectedConfig);
  }
}