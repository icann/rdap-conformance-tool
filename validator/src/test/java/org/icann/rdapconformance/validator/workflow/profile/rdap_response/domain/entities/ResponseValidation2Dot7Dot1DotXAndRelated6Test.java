package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Map;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot1DotXAndRelated6Test extends
    ResponseValidation2Dot7Dot1DotXAndRelatedTest {

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    replaceValue("$..vcardArray[1][?(@[0] == 'adr')][1]", Map.of("cc", "US"));
  }

  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot7Dot1DotXAndRelated6(jsonObject.toString(), results,
        queryType, config);
  }

  /**
   * 8.8.1.6
   */
  @Test
  public void ccParameterNotIncluded() {
    replaceValue("$['entities'][0]['entities'][0]['roles'][0]", "registrant");
    assertThat((String) getValue("$['entities'][0]['entities'][0]['roles'][0]"))
        .isEqualTo("registrant");
    removeKey("$..vcardArray[1][?(@[0] == 'adr')][1]['cc']");
    validate(-52105, "#/entities/0/entities/0:" + jsonObject.query("#/entities/0/entities/0"),
        "An entity with the registrant role without the CC parameter "
            + "was found. See section 2.7.3.1 of the RDAP_Response_Profile_2_1.");
  }
}