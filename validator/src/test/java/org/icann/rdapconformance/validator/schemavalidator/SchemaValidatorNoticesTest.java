package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.NoticeAndRemarkJsonValues;
import org.testng.annotations.Test;

public class SchemaValidatorNoticesTest extends SchemaValidatorForArrayTest {

  public SchemaValidatorNoticesTest() {
    super("test_rdap_notices.json",
        "/validators/notices/valid.json");
    validationName = "stdRdapNoticesRemarksValidation";
  }

  /**
   * 7.2.3.1.
   */
  @Test
  public void invalid() {
    invalid(-10700);
  }

  /**
   * 7.2.3.2.1
   */
  @Test
  public void unauthorizedKey() {
    validateArrayAuthorizedKeys(-10701, List.of("description", "links", "title", "type"));
  }

  /**
   * 7.2.3.2.3
   */
  @Test
  public void titleNotJsonString() {
    arrayItemKeyIsNotString("title", -10703);
  }

  /**
   * 7.2.3.2.4
   */
  @Test
  public void linksViolatesLinksValidation() {
    linksViolatesLinksValidation(-10704);
  }

  /**
   * 7.2.3.2.5
   */
  @Test
  public void typeNotJsonString() {
    arrayItemKeyIsNotString("type", -10705);
  }

  /**
   * 7.2.3.2.6
   */
  @Test
  public void typeNotInEnum() {
    doReturn(true).when(datasets.get(NoticeAndRemarkJsonValues.class))
        .isInvalid("not-in-enum");
    validate(-10706, replaceArrayProperty("type", "not-in-enum"),
        "The JSON string is not included as a Value"
        + " with Type=\"notice  and remark type\" in the RDAPJSONValues dataset.");
  }

  /**
   * 7.2.3.2.7
   */
  @Test
  public void descriptionDoesNotExist() {
    keyDoesNotExistInArray("description", -10707);
  }

  /**
   * 7.2.3.2.8
   */
  @Test
  public void descriptionNotJsonArray() {
    replaceArrayProperty("description", 0);
    Assertions.assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll()).filteredOn(r -> r.getCode() == -10708)
        .hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("value", "#/notices/0/description:0")
        .hasFieldOrPropertyWithValue("message",
            "The #/notices/0/description structure is not syntactically valid.");
  }

  /**
   * 7.2.3.2.9
   */
  @Test
  public void descriptionNotArrayOfString() {
    replaceArrayProperty("description", List.of(0));
    validateIsNotAJsonString(-10709, "#/notices/0/description/0:0");
  }

  /**
   * 8.1.7.
   */
  @Test
  public void tigSection_3_3_and_3_4_Validation() {
    removeKey("links");
    validateWithoutGroupTests(-20700, jsonObject.getJSONArray("notices").toString(), "A links object was not found in the "
        + "notices object in the "
        + "topmost object. See section 3.3 and 3.4 of the "
        + "RDAP_Technical_Implementation_Guide_2_1.");
  }
}
