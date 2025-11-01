package org.icann.rdapconformance.validator.workflow.profile;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class RegistrarEntityPublicIdsValidationTest extends ProfileJsonValidationTestBase {

  protected static final String innerEntities = "\"entities\":[{\"objectClassName\":\"entity\","
      + "\"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"\"],"
      + "[\"tel\",{\"type\":\"voice\"},\"uri\",\"tel:+1.9999999999\"],"
      + "[\"email\",{},\"text\",\"abusecomplaints@example.com\"],"
      + "[\"adr\",{\"type\":\"work\"},\"text\",[\"\",\"Suite 1234\",\"4321 Rue Somewhere\","
      + "\"Quebec\",\"QC\",\"G1V 2M2\",\"\"]]]],\"roles\":[\"abuse\"],\"handle\":\"292\"}],";
  protected final RDAPQueryType baseQueryType;
  protected RDAPQueryType queryType;

  public RegistrarEntityPublicIdsValidationTest(String validJsonResourcePath, String testGroupName,
      RDAPQueryType baseQueryType) {
    super(validJsonResourcePath, testGroupName);
    this.baseQueryType = baseQueryType;
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    this.queryType = baseQueryType;
  }

  public abstract RegistrarEntityPublicIdsValidation getProfileValidation();

  @Test
  public void testValidate_RegistrarEntityWithoutPublicIds_AddErrorCode() {
    removeKey("$['entities'][0]['publicIds']");
    validate(getProfileValidation().code,
        String.format("#/entities/0:{\"objectClassName\":\"entity\",\"vcardArray\":[\"vcard\","
            + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Inc.\"]]],%s"
            + "\"roles\":[\"registrar\"],\"handle\":\"292\"}", innerEntities),
        "A publicIds member is not included in the entity with the registrar role.");
  }

  @Test
  public void testValidate_RegistrarEntityWithPublicIdIdentifierNotAPositiveInteger_AddErrorCode() {
    replaceValue("$['entities'][0]['publicIds'][0]['identifier']", "abc");
    validate(getProfileValidation().code - 1,
        "#/entities/0/publicIds/0:{\"identifier\":\"abc\",\"type\":\"IANA Registrar ID\"}",
        "The identifier of the publicIds member of the entity with the registrar role is not a positive integer.");
  }

}