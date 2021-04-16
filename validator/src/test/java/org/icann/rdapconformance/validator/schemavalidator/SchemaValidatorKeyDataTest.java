package org.icann.rdapconformance.validator.schemavalidator;

import java.io.IOException;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorKeyDataTest extends SchemaValidatorForArrayTest {

  public SchemaValidatorKeyDataTest() {
    super("rdap_secureDNS.json",
        "/validators/keyData/valid.json");
  }

  @BeforeMethod
  @Override
  public void setUp() throws IOException {
    super.setUp();
    name = "keyData";
  }

  /**
   * 7.2.16.10.1
   */
  @Test
  public void invalid() {
    invalid(-12018);
  }

  /**
   * 7.2.16.10.2
   */
  @Test
  public void unauthorizedKey() {
    validateAuthorizedKeys(-12019, List.of("algorithm", "events", "flags", "links",
        "protocol", "publicKey"
    ));
  }

  /**
   * 7.2.16.10.4
   */
  @Test
  public void flagsDoesNotExist() {
    validateKeyMissing(-12021, removeKey("flags"));
  }

  /**
   * 7.2.16.10.4
   */
  @Test
  public void protocolDoesNotExist() {
    validateKeyMissing(-12021, removeKey("protocol"));
  }

  /**
   * 7.2.16.10.4
   */
  @Test
  public void publicKeyDoesNotExist() {
    validateKeyMissing(-12021, removeKey("publicKey"));
  }

  /**
   * 7.2.16.10.4
   */
  @Test
  public void algorithmDoesNotExist() {
    validateKeyMissing(-12021, removeKey("algorithm"));
  }

  /**
   * 7.2.16.10.5
   */
  @Test
  public void flagsNotInEnum() {
    validateNotEnum(-12022, "the associated",
        replaceArrayProperty("flags", 1));
  }

  /**
   * 7.2.16.10.6
   */
  @Test
  public void wrongProtocolConst() {
    validate(-12023, replaceArrayProperty("protocol", 1), "The JSON value is not 3.");
  }

  /**
   * 7.2.16.10.7
   */
  @Test
  public void publicKeyNotStringHexadecimal() {
    validate(-12024, replaceArrayProperty("publicKey", 0), "The JSON value is not a string of "
        + "case-insensitive hexadecimal digits. Whitespace is allowed within the hexadecimal text.");
  }

  /**
   * 7.2.16.10.8
   */
  @Test
  public void algorithmNotInEnum() {
    validateNotEnum(-12025, "the associated", replaceArrayProperty("algorithm", 253));
  }

  /**
   * 7.2.16.10.9
   */
  @Test
  public void stdRdapEventsValidation() {
    stdRdapEventsValidation(-12026);
  }

  /**
   * 7.2.16.10.10
   */
  @Test
  public void stdRdapLinksValidation() {
    stdRdapLinksValidation(-12027);
  }
}
