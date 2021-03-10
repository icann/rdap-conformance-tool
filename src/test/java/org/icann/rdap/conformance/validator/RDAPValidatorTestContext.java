package org.icann.rdap.conformance.validator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.icann.rdap.conformance.validator.configuration.ConfigurationFile;
import org.icann.rdap.conformance.validator.validators.Validator;

public class RDAPValidatorTestContext extends RDAPValidatorContext {

  private final Map<String, Validator> mockedValidators = new HashMap<>();
  private RDAPDeserializer mockedDeserializer = null;

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

  public <T extends Validator> T mockValidator(String name, Class<T> validator) {
    T mockedValidator = mock(validator);
    this.mockedValidators.put(name, mockedValidator);
    return mockedValidator;
  }

  public RDAPDeserializer spyDeserializer() {
    this.mockedDeserializer = spy(this.getDeserializer());
    return this.mockedDeserializer;
  }

  public RDAPDeserializer getDeserializer() {
    if (null != this.mockedDeserializer) {
      return this.mockedDeserializer;
    }
    return super.getDeserializer();
  }

  public Validator getValidator(String name) {
    return this.mockedValidators.getOrDefault(name, super.getValidator(name));
  }

}
