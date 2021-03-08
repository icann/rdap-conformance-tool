package org.icann.rdap.conformance.validator.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class ConfigurationFileParser {

  private final ObjectMapper objectMapper;

  public ConfigurationFileParser() {
    this.objectMapper = new ObjectMapper();
  }

  public ConfigurationFile parse(File configuration) throws IOException {
    return objectMapper.readValue(configuration, ConfigurationFile.class);
  }
}
