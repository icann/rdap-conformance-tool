package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.profile.RegistrarEntityPublicIdsValidation;
import org.icann.rdapconformance.validator.workflow.profile.RegistrarEntityPublicIdsValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarId;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot4Dot2And2Dot4Dot3Test extends
    RegistrarEntityPublicIdsValidationTest {

  private RDAPDatasetService datasetService;
  private RegistrarId registrarId;

  public ResponseValidation2Dot4Dot2And2Dot4Dot3Test() {
    super("rdapResponseProfile_2_4_2_and_2_4_3_Validation");
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    datasetService = mock(RDAPDatasetService.class);
    registrarId = mock(RegistrarId.class);
    doReturn(registrarId).when(datasetService).get(RegistrarId.class);
    doReturn(true).when(registrarId).containsId(292);
  }

  @Override
  public RegistrarEntityPublicIdsValidation getProfileValidation() {
    return new ResponseValidation2Dot4Dot2And2Dot4Dot3(jsonObject.toString(), results,
        datasetService, queryType);
  }

  @Test
  public void testValidate_RegistrarEntityWithHandleNotAPositiveInteger_AddResults47402() {
    replaceValue("$['entities'][0]['handle']", "abc");
    validate(-47402, String.format(
        "#/entities/0:{\"objectClassName\":\"entity\",\"publicIds\":[{\"identifier\":\"292\","
            + "\"type\":\"IANA Registrar ID\"}],\"vcardArray\":[\"vcard\","
            + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Inc.\"]]],%s"
            + "\"roles\":[\"registrar\"],\"handle\":\"abc\"}", innerEntities),
        "The handle of the entity with the registrar role is not a positive integer.");
  }

  @Test
  public void testValidate_PublicIdsIdentifierNotEqualsHandle_AddResults47403() {
    replaceValue("$['entities'][0]['publicIds'][0]['identifier']", "293");
    validate(-47403, String.format(
        "{\"objectClassName\":\"entity\",\"publicIds\":[{\"identifier\":\"293\","
            + "\"type\":\"IANA Registrar ID\"}],\"vcardArray\":[\"vcard\","
            + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Inc.\"]]],%s"
            + "\"roles\":[\"registrar\"],\"handle\":\"292\"}", innerEntities),
        "The identifier of the publicIds member of the entity with the registrar role is not equal to the handle member.");
  }

  @Test
  public void testValidate_HandleNotInRegistrarId_AddResults47404() {
    doReturn(false).when(registrarId).containsId(292);
    validate(-47404, String.format(
        "#/entities/0:{\"objectClassName\":\"entity\",\"publicIds\":[{\"identifier\":\"292\","
            + "\"type\":\"IANA Registrar ID\"}],\"vcardArray\":[\"vcard\","
            + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Inc.\"]]],%s"
            + "\"roles\":[\"registrar\"],\"handle\":\"292\"}", innerEntities),
        "The handle references an IANA Registrar ID that does not exist in the registrarId.");
  }

  @Test
  public void testDoLaunch() {
    queryType = RDAPQueryType.HELP;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVERS;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVER;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.ENTITY;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.DOMAIN;
    assertThat(getProfileValidation().doLaunch()).isTrue();
  }

}