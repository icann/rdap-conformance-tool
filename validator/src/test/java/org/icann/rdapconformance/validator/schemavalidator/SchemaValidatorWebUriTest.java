package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorWebUriTest extends SchemaValidatorTest {

  public SchemaValidatorWebUriTest() {
    super(
        "test_rdap_general_tests.json",
        "/validators/webUri/valid.json");
    validationName = "webUriValidation";
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    name = "webUri";
  }

  /**
   * 7.1.4.1
   */
  @Test
  public void invalid() {
    jsonObject.put("webUri", 0);
    validate(-10400, "#/webUri:0",
        "The URI is not syntactically valid according to RFC3986.");
  }

  /**
   * 7.1.4.2
   */
  @Test
  public void notHttpNorHttps() {
    jsonObject.put("webUri", "ftp://example.com");
    validate(-10401, "#/webUri:ftp://example.com",
        "The scheme of the URI is not 'http' nor 'https'");
  }

  /**
   * 7.1.4.3
   */
  @Test
  public void invalidHostnamePart() {
    jsonObject.put("webUri", "http://-wrong-host.tld");
    validate(-10402, "#/webUri:http://-wrong-host.tld",
        "The host does not pass Domain Name validation [domainNameValidation], IPv4 address validation [ipv4Validation] nor IPv6 address  validation [ipv6Validation].");
  }

  /**
   * 7.1.4.3
   */
  @Test
  public void invalidALabelHostnamePart() {
    jsonObject.put("webUri", "http://m\u200Cn.com");
    // we can't use assertJ to compare this type of string...
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll())
        .filteredOn("code", -10402)
        .hasSize(1)
        .first()
        .matches(v -> v.getValue().startsWith("#/webUri:"))
        .hasFieldOrPropertyWithValue("message", "The host does not pass Domain Name validation "
            + "[domainNameValidation], IPv4 address validation [ipv4Validation] nor IPv6 address  validation [ipv6Validation].");
  }

  /**
   * 7.1.4.3
   */
  @Test
  public void validIpv4HostnamePart() {
    jsonObject.put("webUri", "http://172.16.254.1#/subpath");
    assertThat(schemaValidator.validate(jsonObject.toString())).isTrue();
  }

  /**
   * 7.1.4.3
   */
  @Test
  public void validIpv6HostnamePart() {
    jsonObject.put("webUri", "http://2001:db8:0000:1:1:1:1:1/subpath");
    assertThat(schemaValidator.validate(jsonObject.toString())).isTrue();
  }
}
