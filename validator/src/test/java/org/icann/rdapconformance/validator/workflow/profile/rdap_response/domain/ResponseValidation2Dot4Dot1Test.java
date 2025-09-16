package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import java.util.List;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class ResponseValidation2Dot4Dot1Test extends ResponseDomainValidationTestBase {


  public ResponseValidation2Dot4Dot1Test() {
    super("rdapResponseProfile_2_4_1_Validation");
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot4Dot1(jsonObject.toString(), results, config, queryType);
  }

  @Test
  public void testValidate_NoEntities_AddResults47300() {
    removeKey("entities");
    validate(-47300, jsonObject.toString(),
        "An entity with the registrar role was not found in the domain topmost object.");
  }

  @Test
  public void testValidate_NoRegistrarEntity_AddResults47300() {
    replaceValue("$['entities'][0]['roles'][0]", "registry");
    validate(-47300, jsonObject.toString(),
        "An entity with the registrar role was not found in the domain topmost object.");
  }

  @Test
  public void testValidate_MoreThanOneRegistrarEntities_AddResults47301() {
    replaceValue("entities",
        List.of(new JSONObject("{\"objectClassName\":\"entity\",\"handle\":\"292\","
                + "\"roles\":[\"registrar\"],\"publicIds\":[{\"type\":\"IANA Registrar ID\","
                + "\"identifier\":\"292\"}],\"vcardArray\":[\"vcard\","
                + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Inc.\"]]]}")
                .toMap(),
            new JSONObject("{\"objectClassName\":\"entity\",\"handle\":\"292\","
                + "\"roles\":[\"registrar\"],\"publicIds\":[{\"type\":\"IANA Registrar ID\","
                + "\"identifier\":\"292\"}],\"vcardArray\":[\"vcard\","
                + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Inc.\"]]]}")
                .toMap()));
    validate(-47301, jsonObject.toString(),
        "More than one entities with the registrar role were found in the domain topmost object.");
  }

  @Test
  public void testValidate_EntityWithVcardWithoutFn_AddResults47302() {
    removeKey("$['entities'][0]['vcardArray'][1][1]");
    validate(-47302, "#/entities/0/vcardArray:[\"vcard\",[[\"version\",{},\"text\",\"4.0\"]]]",
        "An fn member was not found in one or more vcard objects of the entity with the registrar role.");
  }
}