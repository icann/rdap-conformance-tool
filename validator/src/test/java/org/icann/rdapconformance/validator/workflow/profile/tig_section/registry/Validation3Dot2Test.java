package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Validation3Dot2Test {

  private final RDAPDatasetService datasetService = mock(RDAPDatasetService.class);
  private final ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
      .forClass(RDAPValidationResult.class);
  private JSONObject rdapResponseJson;
  private RDAPValidatorResults results;

  @BeforeMethod
  public void setUp() throws IOException {
    results = mock(RDAPValidatorResults.class);
    rdapResponseJson = new JSONObject(getResource("/validators/links/valid.json"));
  }

  @Test
  public void validate() {
    Validation3Dot2 validation = new Validation3Dot2(rdapResponseJson.toString(), results,
        datasetService);

    assertThat(validation.validate()).isTrue();
    verify(results).addGroup("tigSection_3_2_Validation", false);
    verifyNoMoreInteractions(results);
  }

  @Test
  public void testValidate_NoLinksInTopmostObject_AddResults23200() {
    rdapResponseJson.remove("links");

    Validation3Dot2 validation = new Validation3Dot2(rdapResponseJson.toString(), results,
        datasetService);

    assertThat(validation.validate()).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -23200)
        .hasFieldOrPropertyWithValue("value", "")
        .hasFieldOrPropertyWithValue("message",
            "A links data structure in the topmost object exists, and the links object shall "
                + "contain the elements rel:related and href, but they were not found. "
                + "See section 3.2 of the RDAP_Technical_Implementation_Guide_2_1.");
    verify(results).addGroup("tigSection_3_2_Validation", true);
  }

  @Test
  public void testValidate_NoRelatedLink_AddResults23200() {
    rdapResponseJson.getJSONArray("links").forEach(l -> {
      JSONObject link = (JSONObject) l;
      if (link.getString("rel").equals("related")) {
        link.put("rel", "self");
      }
    });

    Validation3Dot2 validation = new Validation3Dot2(rdapResponseJson.toString(), results,
        datasetService);

    assertThat(validation.validate()).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -23200)
        .hasFieldOrPropertyWithValue("value", rdapResponseJson.getJSONArray("links").toString())
        .hasFieldOrPropertyWithValue("message",
            "A links data structure in the topmost object exists, and the links object shall "
                + "contain the elements rel:related and href, but they were not found. "
                + "See section 3.2 of the RDAP_Technical_Implementation_Guide_2_1.");
    verify(results).addGroup("tigSection_3_2_Validation", true);
  }

  @Test
  public void testValidate_RelatedLinkWithoutHref_AddResults23200() {
    rdapResponseJson.getJSONArray("links").forEach(l -> {
      JSONObject link = (JSONObject) l;
      if (link.getString("rel").equals("related")) {
        link.remove("href");
      }
    });

    Validation3Dot2 validation = new Validation3Dot2(rdapResponseJson.toString(), results,
        datasetService);

    assertThat(validation.validate()).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -23200)
        .hasFieldOrPropertyWithValue("value", rdapResponseJson.getJSONArray("links").toString())
        .hasFieldOrPropertyWithValue("message",
            "A links data structure in the topmost object exists, and the links object shall "
                + "contain the elements rel:related and href, but they were not found. "
                + "See section 3.2 of the RDAP_Technical_Implementation_Guide_2_1.");
    verify(results).addGroup("tigSection_3_2_Validation", true);
  }
}