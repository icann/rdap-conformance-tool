package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

public class ResponseValidation2Dot4Dot5Test extends ResponseDomainValidationTestBase {

  public ResponseValidation2Dot4Dot5Test() {
    super("rdapResponseProfile_2_4_5_Validation");
  }


  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot4Dot5(jsonObject.toString(), results, queryType);
  }

  @Test
  public void testValidate_NoEntitiesWithAbuseRole_AddResults47500() {
    removeKey("$['entities'][0]['entities'][0]");
    validate(-47500,
        "#/entities/0:{\"objectClassName\":\"entity\",\"publicIds\":[{\"identifier\":\"292\","
            + "\"type\":\"IANA Registrar ID\"}],\"vcardArray\":[\"vcard\","
            + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Inc.\"]]],"
            + "\"entities\":[],\"roles\":[\"registrar\"],\"handle\":\"292\"}",
        "Tel and email members were not found for the entity within the entity with the "
            + "abuse role in the topmost domain object.");
  }

  @Test
  public void testValidate_EntitiesWithAbuseRoleAndVcardWithoutEmail_AddResults47500() {
    removeKey("$['entities'][0]['entities'][0]['vcardArray'][1][3]");
    validate(-47500,
        "#/entities/0:{\"objectClassName\":\"entity\",\"publicIds\":[{\"identifier\":\"292\","
            + "\"type\":\"IANA Registrar ID\"}],\"vcardArray\":[\"vcard\","
            + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Inc.\"]]],"
            + "\"entities\":[{\"objectClassName\":\"entity\",\"vcardArray\":[\"vcard\","
            + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"\"],"
            + "[\"tel\",{\"type\":\"voice\"},\"uri\",\"tel:+1.9999999999\"],"
            + "[\"adr\",{\"type\":\"work\"},\"text\",[\"\",\"Suite 1234\",\"4321 Rue Somewhere\","
            + "\"Quebec\",\"QC\",\"G1V 2M2\",\"\"]]]],\"roles\":[\"abuse\"],\"handle\":\"292\"}],"
            + "\"roles\":[\"registrar\"],\"handle\":\"292\"}",
        "Tel and email members were not found for the entity within the entity with the "
            + "abuse role in the topmost domain object.");
  }

  @Test
  public void testValidate_EntitiesWithAbuseRoleAndVcardWithoutTel_AddResults47500() {
    removeKey("$['entities'][0]['entities'][0]['vcardArray'][1][2]");
    validate(-47500,
        "#/entities/0:{\"objectClassName\":\"entity\",\"publicIds\":[{\"identifier\":\"292\","
            + "\"type\":\"IANA Registrar ID\"}],\"vcardArray\":[\"vcard\","
            + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Inc.\"]]],"
            + "\"entities\":[{\"objectClassName\":\"entity\",\"vcardArray\":[\"vcard\","
            + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"\"],"
            + "[\"email\",{},\"text\",\"abusecomplaints@example.com\"],"
            + "[\"adr\",{\"type\":\"work\"},\"text\",[\"\",\"Suite 1234\",\"4321 Rue Somewhere\","
            + "\"Quebec\",\"QC\",\"G1V 2M2\",\"\"]]]],\"roles\":[\"abuse\"],\"handle\":\"292\"}],"
            + "\"roles\":[\"registrar\"],\"handle\":\"292\"}",
        "Tel and email members were not found for the entity within the entity with the "
            + "abuse role in the topmost domain object.");
  }
}