package org.icann.rdap.conformance.validator.validators;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;
import org.icann.rdap.conformance.validator.configuration.ConfigurationFile;
import org.testng.annotations.Test;

public class StdRdapDomainLookupValidationTest {

  private final ConfigurationFile configurationFile = new ConfigurationFile();
  private Validator validator;

  private String getResource(String path) throws IOException {
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

  @Test
  public void testValidate_InvalidJson() throws IOException {
    String rdapContent = getResource("/validators/domain/invalid.json");

    validator = new StdRdapDomainLookupValidation(rdapContent, configurationFile);
    assertThat(validator.validate()).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", "-12199-12199 - 1 -12200")
        .hasFieldOrPropertyWithValue("value", rdapContent)
        .hasFieldOrPropertyWithValue("message", "The domain structure is not syntactically valid.");
  }

  @Test
  public void testValidate_InvalidKeyValuePair() throws IOException {
    // TODO put unknown property in an inner object to check that we don't catch that here
    String rdapContent = getResource("/validators/domain/unknown_key.json");

    validator = new StdRdapDomainLookupValidation(rdapContent, configurationFile);
    assertThat(validator.validate()).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", "-12199 - 2 -12201")
        .hasFieldOrPropertyWithValue("value", "unknown/[{test=value}]")
        .hasFieldOrPropertyWithValue("message",
            "The name in the name/value pair is not of: objectClassName, handle, ldhName, "
                + "unicodeName, variants, nameservers, secureDNS, entities, status, publicIds, "
                + "remarks, links, port43, events, notices or rdapConformance.");
  }
}