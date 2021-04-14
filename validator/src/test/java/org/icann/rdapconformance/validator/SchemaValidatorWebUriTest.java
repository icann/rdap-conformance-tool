package org.icann.rdapconformance.validator;

import java.io.IOException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorWebUriTest extends SchemaValidatorTest {

  public SchemaValidatorWebUriTest() {
    super(
        "test_rdap_general_tests.json",
        "/validators/webUri/valid.json");
  }

  @BeforeMethod
  @Override
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

  @Test
  public void notHttpNorHttps() {
    jsonObject.put("webUri", "ftp://example.com");
    validate(-10401, "#/webUri:ftp://example.com",
        "The scheme of the URI is not 'http' nor 'https'");
  }
}
