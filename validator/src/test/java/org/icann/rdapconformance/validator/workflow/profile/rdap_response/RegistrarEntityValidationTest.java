package org.icann.rdapconformance.validator.workflow.profile.rdap_response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.RegistrarEntityPublicIdsValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarId;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class RegistrarEntityValidationTest extends RegistrarEntityPublicIdsValidationTest {

  protected RDAPDatasetService datasetService;
  private RegistrarId registrarId;

  public RegistrarEntityValidationTest(String validJsonResourcePath, String testGroupName,
      RDAPQueryType baseQueryType) {
    super(validJsonResourcePath, testGroupName, baseQueryType);
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    datasetService = mock(RDAPDatasetService.class);
    registrarId = mock(RegistrarId.class);
    doReturn(registrarId).when(datasetService).get(RegistrarId.class);
    doReturn(true).when(registrarId).containsId(292);

    // Re-initialize queryContext with the custom mocked datasetService
    queryContext = QueryContext.forTesting(rdapContent, results, config, datasetService);
  }

  private RegistrarEntityValidation getRegistrarEntityValidation() {
    return (RegistrarEntityValidation) getProfileValidation();
  }

  @Test
  public void testValidate_RegistrarEntityWithHandleNotAPositiveInteger_AddResults47402() {
    replaceValue("$['entities'][0]['handle']", "abc");
    validate(getRegistrarEntityValidation().code - 2, String.format(
        "#/entities/0:{\"objectClassName\":\"entity\",\"publicIds\":[{\"identifier\":\"292\","
            + "\"type\":\"IANA Registrar ID\"}],\"vcardArray\":[\"vcard\","
            + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Inc.\"]]],%s"
            + "\"roles\":[\"registrar\"],\"handle\":\"abc\"}", innerEntities),
        "The handle of the entity with the registrar role is not a positive integer.");
  }

  @Test
  public void testValidate_PublicIdsIdentifierNotEqualsHandle_AddResults47403() {
    replaceValue("$['entities'][0]['publicIds'][0]['identifier']", "293");
    validate(getRegistrarEntityValidation().code - 3, String.format(
        "{\"objectClassName\":\"entity\",\"publicIds\":[{\"identifier\":\"293\","
            + "\"type\":\"IANA Registrar ID\"}],\"vcardArray\":[\"vcard\","
            + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Inc.\"]]],%s"
            + "\"roles\":[\"registrar\"],\"handle\":\"292\"}", innerEntities),
        "The identifier of the publicIds member of the entity with the registrar role is not equal to the handle member.");
  }

  @Test
  public void testValidate_HandleNotInRegistrarId_AddResults47404() {
    doReturn(false).when(registrarId).containsId(292);
    validate(getRegistrarEntityValidation().code - 4, String.format(
        "#/entities/0:{\"objectClassName\":\"entity\",\"publicIds\":[{\"identifier\":\"292\","
            + "\"type\":\"IANA Registrar ID\"}],\"vcardArray\":[\"vcard\","
            + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Inc.\"]]],%s"
            + "\"roles\":[\"registrar\"],\"handle\":\"292\"}", innerEntities),
        "The handle references an IANA Registrar ID that does not exist in the registrarId.");
  }

  @Test
  public void testDoLaunch() {
    for (RDAPQueryType queryTypeBeingTested : List
        .of(RDAPQueryType.HELP, RDAPQueryType.NAMESERVERS, RDAPQueryType.NAMESERVER,
            RDAPQueryType.ENTITY, RDAPQueryType.DOMAIN)) {
      queryType = queryTypeBeingTested;
      // Update QueryContext with new query type for proper validation behavior
      queryContext = QueryContext.forTesting(rdapContent, results, config, datasets);
      queryContext = new QueryContext(queryContext.getQueryId(),
                                     queryContext.getConfig(),
                                     queryContext.getDatasetService(),
                                     queryContext.getQuery(),
                                     queryContext.getResults(),
                                     queryType);
      queryContext.setRdapResponseData(rdapContent);

      if (queryType.equals(baseQueryType)) {
        assertThat(getProfileValidation().doLaunch()).isTrue();
      } else {
        assertThat(getProfileValidation().doLaunch()).isFalse();
      }
    }
  }
}