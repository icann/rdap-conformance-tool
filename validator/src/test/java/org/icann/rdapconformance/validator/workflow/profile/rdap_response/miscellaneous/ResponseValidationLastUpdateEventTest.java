package org.icann.rdapconformance.validator.workflow.profile.rdap_response.miscellaneous;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidationLastUpdateEventTest extends ProfileJsonValidationTestBase {

  private RDAPQueryType queryType;

  protected ResponseValidationLastUpdateEventTest() {
    super(
        "/validators/events/rdapResponseProfile_2_3_1_3_and_2_7_6_and_3_3_and_4_4_Validation.json",
        "rdapResponseProfile_2_3_1_3_and_2_7_6_and_3_3_and_4_4_Validation");
  }

  @Override
  @BeforeMethod
  public void setUp() throws java.io.IOException {
    super.setUp();
    queryType = RDAPQueryType.DOMAIN;
  }

  @Override
  public ProfileJsonValidation getTigValidation() {
    return new ResponseValidationLastUpdateEvent(jsonObject.toString(), results, queryType);
  }

  @Test
  public void lastUpdateOfRdapDatabaseEventActionMissing() {
    replaceValue("$.events[0].eventAction", "registration");
    validate(-43100, "[{\"eventAction\":\"registration\",\"eventDate\":\"1997-09-15T04:00:00Z\"}]",
        "An eventAction type last update of RDAP database does not "
            + "exists in the topmost events data structure. See section 2.3.1.3, 2.7.6, 3.3 and "
            + "4.4 of the RDAP_Response_Profile_2_1.");
  }

  @Test
  public void testDoLaunch() {
    queryType = RDAPQueryType.HELP;
    assertThat(getTigValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVERS;
    assertThat(getTigValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.DOMAIN;
    assertThat(getTigValidation().doLaunch()).isTrue();
    queryType = RDAPQueryType.NAMESERVER;
    assertThat(getTigValidation().doLaunch()).isTrue();
    queryType = RDAPQueryType.ENTITY;
    assertThat(getTigValidation().doLaunch()).isTrue();
  }
}