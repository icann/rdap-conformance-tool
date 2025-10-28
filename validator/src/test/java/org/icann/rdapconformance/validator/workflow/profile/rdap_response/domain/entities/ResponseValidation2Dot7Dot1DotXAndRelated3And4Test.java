package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EPPRoid;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot1DotXAndRelated3And4Test extends
    ResponseValidation2Dot7Dot1DotXAndRelatedTest {

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    replaceValue("['entities'][0]['handle']", "2138514_DOMAIN_COM-EXMP");
  }

  public ProfileValidation getProfileValidation() {
    SimpleHandleValidation simpleHandleValidation = new SimpleHandleValidation(config,
        jsonObject.toString(),
        results,
        datasets,
        queryType,
        -52102);
    return new ResponseValidation2Dot7Dot1DotXAndRelated3And4(jsonObject.toString(), results,
        queryType, config, simpleHandleValidation);
  }

  /**
   * 8.8.1.3
   */
  @Test
  public void invalidHandleFormat() {
    entitiesWithRole("registrant");
    replaceValue("['entities'][0]['handle']", "ABCD");
    validate(-52102, "#/entities/0/handle:ABCD", "The handle in the entity object does not comply "
        + "with the format "
        + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
  }

  /**
   * 8.8.1.4
   */
  @Test
  public void notInEppRoid() {
    entitiesWithRole("registrant");
    doReturn(true).when(datasets.get(EPPRoid.class)).isInvalid("EXMP");
    validate(-52103, "#/entities/0/handle:2138514_DOMAIN_COM-EXMP", "The globally unique "
        + "identifier in the entity object handle is not "
        + "registered in EPPROID.");
  }
}