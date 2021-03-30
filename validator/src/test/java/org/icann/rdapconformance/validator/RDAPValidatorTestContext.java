package org.icann.rdapconformance.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;

public class RDAPValidatorTestContext extends RDAPValidatorContext {

  public RDAPValidatorTestContext(ConfigurationFile configurationFile) {
    super(configurationFile);
  }

  public String getResource(String path) throws IOException {
    URL jsonUri = this.getClass().getResource(path);
    assert null != jsonUri;
    try (InputStream is = this.getClass().getResourceAsStream(path)) {
      assert null != is;
      try (InputStreamReader isr = new InputStreamReader(is);
          BufferedReader reader = new BufferedReader(isr)) {
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
      }
    }
  }
}
