package org.icann.rdapconformance.validator.schemavalidator;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorDsDataTest extends SchemaValidatorForArrayTest {

  public SchemaValidatorDsDataTest() {
    super("rdap_secureDNS.json",
        "/validators/dsData/valid.json");
    validationName = "stdRdapSecureDnsValidation";
  }

  @BeforeMethod
  @Override
  public void setUp() throws IOException {
    Locale.setDefault(Locale.US);
    super.setUp();
    name = "dsData";
  }

  /**
   * 7.2.16.9.1
   */
  @Test
  public void invalid() {
    invalid(-12008);
  }

  /**
   * 7.2.16.9.2
   */
  @Test
  public void unauthorizedKey() {
    validateAuthorizedKeys(-12009, List.of("algorithm", "digest", "digestType", "events",
        "keyTag", "links"
    ));
  }

  /**
   * 7.2.16.9.4
   */
  @Test
  public void algorithmDoesNotExist() {
    removeKey("algorithm");
    validateKeyMissing(-12011, "algorithm");
  }

  /**
   * 7.2.16.9.4
   */
  @Test
  public void digestDoesNotExist() {
    removeKey("digest");
    validateKeyMissing(-12011, "digest");
  }

  /**
   * 7.2.16.9.4
   */
  @Test
  public void digestTypeDoesNotExist() {
    removeKey("digestType");
    validateKeyMissing(-12011, "digestType");
  }

  /**
   * 7.2.16.9.4
   */
  @Test
  public void keyTagDoesNotExist() {
    removeKey("keyTag");
    validateKeyMissing(-12011, "keyTag");
  }

  /**
   * 7.2.16.9.5
   */
  @Test
  public void keyTagNotNumber() {
    validate(-12012, replaceArrayProperty("keyTag", "not-a-number"),
        "The JSON value is not a number between 1 and 65,535.");
  }

  /**
   * 7.2.16.9.6
   */
  @Test
  public void algorithmNotInEnum() {
    validate(-12013, replaceArrayProperty("algorithm", 253),
        "The JSON value is not listed with Zone Signing=Y in "
            + "dnsSecAlgNumbers, or it's 253 or 254.");
  }

  /**
   * 7.2.16.9.7
   */
  @Test
  public void digestNotJsonStringHexadecimal() {
    validate(-12014, replaceArrayProperty("digest", 0),
        "The JSON value is not a string of case-insensitive hexadecimal digits. Whitespace is allowed within the hexadecimal text.");
  }

  /**
   * 7.2.16.9.8
   */
  @Test
  public void digestTypeNotInEnum() {
    validate(-12015, replaceArrayProperty("digestType", 7),
        "The JSON value is not assigned in dsRrTypes.");
  }

  /**
   * 7.2.16.9.9
   */
  @Test
  public void stdRdapEventsValidation() {
    stdRdapEventsValidation(-12016);
  }

  /**
   * 7.2.16.9.10
   */
  @Test
  public void stdRdapLinksValidation() {
    stdRdapLinksValidation(-12017);
  }
}
