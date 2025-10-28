package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot1DotXAndRelated5Test extends
    ResponseValidation2Dot7Dot1DotXAndRelatedTest {

  /**
   * 8.8.1.5
   */
  @Test
  public void moreThanOneEntityWithRegistrantRole() {
    entitiesWithRole("registrant");
    // add again the registrant
    jsonObject.getJSONArray("entities").put(jsonObject.getJSONArray("entities").getJSONObject(0));
    // verify we have two entity with registrant role:
    assertThat((List<String>) getValue("$.entities[*].roles[?(@ == 'registrant')]")).hasSize(2);

    String registrant = "#/entities/0:" + jsonObject.query("#/entities/0");
    validate(-52104, registrant, "More than one entity with the following roles were found: "
        + "registrant, administrative, technical and billing.");
  }

  /**
   * 8.8.1.5
   */
  @Test
  public void moreThanOneEntityWithDifferentRoles() {
    entitiesWithRole("registrant");
    // add another role:
    jsonObject.getJSONArray("entities").put(jsonObject.getJSONArray("entities").getJSONObject(0));
    replaceValue("$['entities'][1]['roles'][0]", "administrative");
    // verify we have two entity with different roles:
    assertThat((List<String>) getValue("$.entities[*].roles[?(@ == 'registrant' || @ == 'administrative')]")).hasSize(2);

    validate();
  }

  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot7Dot1DotXAndRelated5(jsonObject.toString(), results,
        queryType, config);
  }
}
