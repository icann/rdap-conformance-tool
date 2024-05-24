package org.icann.rdapconformance.validator.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;

public class ConfigurationFileParserImpl implements ConfigurationFileParser {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public ConfigurationFile parse(InputStream configuration) throws IOException {
    return objectMapper.readValue(configuration, ConfigurationFile.class);
  }
}
