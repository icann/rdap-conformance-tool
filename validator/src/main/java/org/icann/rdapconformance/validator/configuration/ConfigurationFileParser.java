package org.icann.rdapconformance.validator.configuration;

import java.io.IOException;
import java.io.InputStream;

public interface ConfigurationFileParser {

  ConfigurationFile parse(InputStream configuration) throws IOException;
}
