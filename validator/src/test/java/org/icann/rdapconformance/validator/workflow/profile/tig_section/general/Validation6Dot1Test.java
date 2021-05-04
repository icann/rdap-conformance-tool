package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.registry.Validation6Dot1;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Validation6Dot1Test {

  private final ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor
      .forClass(RDAPValidationResult.class);
  private RDAPValidatorResults results;

  @BeforeMethod
  public void setUp() throws IOException {
    results = mock(RDAPValidatorResults.class);
  }

  @Test
  public void validate() {
    JSONObject rdapResponseJson = new JSONObject("{\n"
        + "  \"entities\": [\n"
        + "    {\n"
        + "      \"roles\": [\n"
        + "        \"abuse\"\n"
        + "      ],\n"
        + "      \"publicIds\": [\n"
        + "        {\n"
        + "          \"type\": \"IANA Registrar ID\",\n"
        + "          \"identifier\": \"292\"\n"
        + "        }\n"
        + "      ]\n"
        + "    },\n"
        + "    {\n"
        + "      \"roles\": [\n"
        + "        \"test\",\n"
        + "        \"registrar\"\n"
        + "      ],\n"
        + "      \"publicIds\": [\n"
        + "        {\n"
        + "          \"type\": \"IANA Registrar ID\",\n"
        + "          \"identifier\": \"292\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"type\": \"IANA Registrar ID\",\n"
        + "          \"identifier\": \"293\"\n"
        + "        }\n"
        + "      ]\n"
        + "    }\n"
        + "  ]\n"
        + "}");

    Validation6Dot1 validation = new Validation6Dot1(rdapResponseJson.toString(), results);

    assertThat(validation.validate()).isTrue();
    verify(results).addGroup("tigSection_6_1_Validation", false);
    verifyNoMoreInteractions(results);
  }

  @Test
  public void testValidate_RegistrarEntityWithoutPublicIds_AddResults23300() {
    JSONObject rdapResponseJson = new JSONObject("{\n"
        + "  \"entities\": [\n"
        + "    {\n"
        + "      \"roles\": [\n"
        + "        \"abuse\"\n"
        + "      ],\n"
        + "      \"publicIds\": [\n"
        + "        {\n"
        + "          \"type\": \"IANA Registrar ID\",\n"
        + "          \"identifier\": \"292\"\n"
        + "        }\n"
        + "      ]\n"
        + "    },\n"
        + "    {\n"
        + "      \"roles\": [\n"
        + "        \"test\",\n"
        + "        \"registrar\"\n"
        + "      ]\n"
        + "    }\n"
        + "  ]\n"
        + "}");
    Validation6Dot1 validation = new Validation6Dot1(rdapResponseJson.toString(), results);

    assertThat(validation.validate()).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -23300)
        .hasFieldOrPropertyWithValue("value", "{\"roles\":[\"test\",\"registrar\"]}")
        .hasFieldOrPropertyWithValue("message",
            "A publicIds member is not included in the entity with the registrar role.");
    verify(results).addGroup("tigSection_6_1_Validation", true);
  }

  @Test
  public void testValidate_RegistrarEntityWithPublicIdIdentifierNotAPositiveInteger_AddResults23301() {
    JSONObject rdapResponseJson = new JSONObject("{\n"
        + "  \"entities\": [\n"
        + "    {\n"
        + "      \"roles\": [\n"
        + "        \"abuse\"\n"
        + "      ],\n"
        + "      \"publicIds\": [\n"
        + "        {\n"
        + "          \"type\": \"IANA Registrar ID\",\n"
        + "          \"identifier\": \"292\"\n"
        + "        }\n"
        + "      ]\n"
        + "    },\n"
        + "    {\n"
        + "      \"roles\": [\n"
        + "        \"test\",\n"
        + "        \"registrar\"\n"
        + "      ],\n"
        + "      \"publicIds\": [\n"
        + "        {\n"
        + "          \"type\": \"IANA Registrar ID\",\n"
        + "          \"identifier\": \"292\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"type\": \"IANA Registrar ID\",\n"
        + "          \"identifier\": \"abc\"\n"
        + "        }\n"
        + "      ]\n"
        + "    }\n"
        + "  ]\n"
        + "}");
    Validation6Dot1 validation = new Validation6Dot1(rdapResponseJson.toString(), results);

    assertThat(validation.validate()).isFalse();
    verify(results).add(resultCaptor.capture());
    RDAPValidationResult result = resultCaptor.getValue();
    assertThat(result).hasFieldOrPropertyWithValue("code", -23301)
        .hasFieldOrPropertyWithValue("value",
            "{\"type\":\"IANA Registrar ID\",\"identifier\":\"abc\"}")
        .hasFieldOrPropertyWithValue("message",
            "The identifier of the publicIds member of the entity with the registrar role is not a positive integer.");
    verify(results).addGroup("tigSection_6_1_Validation", true);
  }
}