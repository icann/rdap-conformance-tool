package org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.RegistrarEntityPublicIdsValidation;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.RegistrarEntityValidationTest;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidation2Dot4Dot2And2Dot4Dot3;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.Test;

public class ResponseValidation4Dot3Test extends
    RegistrarEntityValidationTest {

  public ResponseValidation4Dot3Test() {
    super("/validators/nameserver/valid.json", "rdapResponseProfile_4_3_Validation",
        RDAPQueryType.NAMESERVER);
  }

  @Override
  public RegistrarEntityPublicIdsValidation getProfileValidation() {
    QueryContext nameserverContext = new QueryContext(
        queryContext.getQueryId(),
        queryContext.getConfig(),
        queryContext.getDatasetService(),
        queryContext.getQuery(),
        queryContext.getResults(),
        RDAPQueryType.NAMESERVER
    );
    nameserverContext.setRdapResponseData(queryContext.getRdapResponseData());
    return new ResponseValidation4Dot3(nameserverContext);
  }

  @Test
  public void testValidate_RegistrarEntityWithHandleNotApplicableWithPublicIds_AddResults49205() {
    replaceValue("$['entities'][0]['handle']", "not applicable");
    validate(-49205, String.format(
        "#/entities/0:{\"objectClassName\":\"entity\",\"publicIds\":[{\"identifier\":\"292\","
            + "\"type\":\"IANA Registrar ID\"}],\"vcardArray\":[\"vcard\","
            + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Inc.\"]]],%s"
            + "\"roles\":[\"registrar\"],\"handle\":\"not applicable\"}", innerEntities),
        "A publicIds member is included in the entity with the registrar role.");
  }

  @Test
  public void testDoLaunch_NoRegistrarEntity_IsFalse() {
    replaceValue("$['entities'][0]['roles'][0]", "registrant");
    assertThat(getProfileValidation().doLaunch()).isFalse();
  }
}