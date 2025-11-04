package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TigValidation3Dot2Test extends ProfileJsonValidationTestBase {

  private RDAPValidatorConfiguration config;
  private RDAPDatasetService datasetService;
  private RegistrarId registrarId;

  public TigValidation3Dot2Test() {
    super("/validators/profile/tig_section/links/valid.json",
        "tigSection_3_2_Validation");
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    config = mock(RDAPValidatorConfiguration.class);
    doReturn(true).when(config).isGtldRegistry();

    datasetService = mock(RDAPDatasetService.class);
    registrarId = mock(RegistrarId.class);
    doReturn(registrarId).when(datasetService).get(RegistrarId.class);

    // Create QueryContext with our mocked config and datasetService
    queryContext = org.icann.rdapconformance.validator.QueryContext.forTesting(rdapContent, results, config, datasetService);
    queryContext.setQueryType(RDAPQueryType.DOMAIN);
  }

  public ProfileJsonValidation getProfileValidation() {
    return new TigValidation3Dot2(queryContext);
  }

  @Test
  public void testValidate_NoLinksInTopmostObject_AddResults23200() {
    jsonObject.remove("links");
    validate(-23200, "",
        "A links data structure in the topmost object exists, and the links object shall "
            + "contain the elements rel:related and href, but they were not found. "
            + "See section 3.2 of the RDAP_Technical_Implementation_Guide_2_1.");
  }

  @Test
  public void testValidate_NoRelatedLink_AddResults23200() {
    jsonObject.getJSONArray("links").forEach(l -> {
      JSONObject link = (JSONObject) l;
      if (link.getString("rel").equals("related")) {
        link.put("rel", "self");
      }
    });

    validate(-23200, jsonObject.getJSONArray("links").toString(),
        "A links data structure in the topmost object exists, and the links object shall "
            + "contain the elements rel:related and href, but they were not found. "
            + "See section 3.2 of the RDAP_Technical_Implementation_Guide_2_1.");
  }

  @Test
  public void testValidate_RelatedLinkWithoutHref_AddResults23200() {
    jsonObject.getJSONArray("links").forEach(l -> {
      JSONObject link = (JSONObject) l;
      if (link.getString("rel").equals("related")) {
        link.remove("href");
      }
    });

    validate(-23200, jsonObject.getJSONArray("links").toString(),
        "A links data structure in the topmost object exists, and the links object shall "
            + "contain the elements rel:related and href, but they were not found. "
            + "See section 3.2 of the RDAP_Technical_Implementation_Guide_2_1.");
  }

  @Test
  public void testDoLaunch_NotARegistry_IsFalse() {
    doReturn(false).when(config).isGtldRegistry();
    assertThat(getProfileValidation().doLaunch()).isFalse();
  }

  @Test
  public void testDoLaunch_NotADomainQuery_IsFalse() {
    doReturn(true).when(config).isGtldRegistry();
    queryContext.setQueryType(RDAPQueryType.NAMESERVER);
    assertThat(getProfileValidation().doLaunch()).isFalse();
  }

  // RCT-104 only apply if the query is for a gtld registry and the value is not in excluded range (9994-9999)
  @Test
  public void testValidate_NoLinksInTopmostObjectWithRegistrar9999_AddResults23200() {
    JSONObject identifier = new JSONObject();
    identifier.put("identifier", "9999");

    JSONArray publicIdArray = new JSONArray();
    publicIdArray.put(identifier);

    JSONObject publicIds = new JSONObject();
    publicIds.put("publicIds", publicIdArray);

    JSONArray entities = new JSONArray();
    entities.put(publicIds);
    jsonObject.put("entities", entities);

    // Update QueryContext with modified JSON data
    updateQueryContextJsonData();

    TigValidation3Dot2 tigValidation3Dot2 = new TigValidation3Dot2(queryContext);
    assertThat(tigValidation3Dot2.isExcludedRegistrarId()).isTrue();
  }

  // RCT-104 only apply if the query is for a gtld registry and the value is not in excluded range (9994-9999)
  @Test
  public void testValidate_NoLinksInTopmostObjectWithRegistrar9998_AddResults23200() {
    JSONObject identifier = new JSONObject();
    identifier.put("identifier", "9998");

    JSONArray publicIdArray = new JSONArray();
    publicIdArray.put(identifier);

    JSONObject publicIds = new JSONObject();
    publicIds.put("publicIds", publicIdArray);

    JSONArray entities = new JSONArray();
    entities.put(publicIds);
    jsonObject.put("entities", entities);

    // Update QueryContext with modified JSON data
    updateQueryContextJsonData();

    TigValidation3Dot2 tigValidation3Dot2 = new TigValidation3Dot2(queryContext);
    assertThat(tigValidation3Dot2.isExcludedRegistrarId()).isTrue();
  }

  @Test
  public void testValidate_ExcludedRegistrar9994_IsExcluded() {
    JSONObject identifier = new JSONObject();
    identifier.put("identifier", "9994");

    JSONArray publicIdArray = new JSONArray();
    publicIdArray.put(identifier);

    JSONObject publicIds = new JSONObject();
    publicIds.put("publicIds", publicIdArray);

    JSONArray entities = new JSONArray();
    entities.put(publicIds);
    jsonObject.put("entities", entities);

    // Update QueryContext with modified JSON data
    updateQueryContextJsonData();

    TigValidation3Dot2 tigValidation3Dot2 = new TigValidation3Dot2(queryContext);
    assertThat(tigValidation3Dot2.isExcludedRegistrarId()).isTrue();
  }

  @Test
  public void testValidate_ExcludedRegistrar9995_IsExcluded() {
    JSONObject identifier = new JSONObject();
    identifier.put("identifier", "9995");

    JSONArray publicIdArray = new JSONArray();
    publicIdArray.put(identifier);

    JSONObject publicIds = new JSONObject();
    publicIds.put("publicIds", publicIdArray);

    JSONArray entities = new JSONArray();
    entities.put(publicIds);
    jsonObject.put("entities", entities);

    // Update QueryContext with modified JSON data
    updateQueryContextJsonData();

    TigValidation3Dot2 tigValidation3Dot2 = new TigValidation3Dot2(queryContext);
    assertThat(tigValidation3Dot2.isExcludedRegistrarId()).isTrue();
  }

  @Test
  public void testValidate_ExcludedRegistrar9996_IsExcluded() {
    JSONObject identifier = new JSONObject();
    identifier.put("identifier", "9996");

    JSONArray publicIdArray = new JSONArray();
    publicIdArray.put(identifier);

    JSONObject publicIds = new JSONObject();
    publicIds.put("publicIds", publicIdArray);

    JSONArray entities = new JSONArray();
    entities.put(publicIds);
    jsonObject.put("entities", entities);

    // Update QueryContext with modified JSON data
    updateQueryContextJsonData();

    TigValidation3Dot2 tigValidation3Dot2 = new TigValidation3Dot2(queryContext);
    assertThat(tigValidation3Dot2.isExcludedRegistrarId()).isTrue();
  }

  @Test
  public void testValidate_ExcludedRegistrar9997_IsExcluded() {
    JSONObject identifier = new JSONObject();
    identifier.put("identifier", "9997");

    JSONArray publicIdArray = new JSONArray();
    publicIdArray.put(identifier);

    JSONObject publicIds = new JSONObject();
    publicIds.put("publicIds", publicIdArray);

    JSONArray entities = new JSONArray();
    entities.put(publicIds);
    jsonObject.put("entities", entities);

    // Update QueryContext with modified JSON data
    updateQueryContextJsonData();

    TigValidation3Dot2 tigValidation3Dot2 = new TigValidation3Dot2(queryContext);
    assertThat(tigValidation3Dot2.isExcludedRegistrarId()).isTrue();
  }

  @Test
  public void testValidate_NonExcludedRegistrar1000_NotExcluded() {
    JSONObject identifier = new JSONObject();
    identifier.put("identifier", "1000");

    JSONArray publicIdArray = new JSONArray();
    publicIdArray.put(identifier);

    JSONObject publicIds = new JSONObject();
    publicIds.put("publicIds", publicIdArray);

    JSONArray entities = new JSONArray();
    entities.put(publicIds);
    jsonObject.put("entities", entities);

    TigValidation3Dot2 tigValidation3Dot2 = new TigValidation3Dot2(queryContext);
    assertThat(tigValidation3Dot2.isExcludedRegistrarId()).isFalse();
  }

  @Test
  public void testValidate_NoEntities_NotExcluded() {
    jsonObject.remove("entities");

    TigValidation3Dot2 tigValidation3Dot2 = new TigValidation3Dot2(queryContext);
    assertThat(tigValidation3Dot2.isExcludedRegistrarId()).isFalse();
  }

  @Test
  public void testValidate_EmptyEntities_NotExcluded() {
    jsonObject.put("entities", new JSONArray());

    TigValidation3Dot2 tigValidation3Dot2 = new TigValidation3Dot2(queryContext);
    assertThat(tigValidation3Dot2.isExcludedRegistrarId()).isFalse();
  }
}