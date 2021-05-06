package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Validation3Dot2Test extends ProfileValidationTestBase {

  private final static RDAPQueryType QUERY_TYPE = RDAPQueryType.DOMAIN;
  private final RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
  private JSONObject rdapResponseJson;

  @BeforeMethod
  public void setUp() throws Throwable {
    super.setUp();
    doReturn(true).when(config).isGtldRegistry();
    rdapResponseJson = new JSONObject("{\n"
        + "  \"links\": [\n"
        + "    {\n"
        + "      \"value\": \"https://rdap.example.com/com/v1/domain/EXAMPLE.COM\",\n"
        + "      \"rel\": \"self\",\n"
        + "      \"href\": \"https://rdap.example.com/com/v1/domain/EXAMPLE.COM\",\n"
        + "      \"type\": \"application/rdap+json\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"value\": \"https://rdap.markmonitor.com/rdap/domain/EXAMPLE.COM\",\n"
        + "      \"rel\": \"related\",\n"
        + "      \"href\": \"https://rdap.markmonitor.com/rdap/domain/EXAMPLE.COM\",\n"
        + "      \"type\": \"application/rdap+json\"\n"
        + "    }\n"
        + "  ]\n"
        + "}");
  }

  @Override
  @Test
  public void testValidate() {
    Validation3Dot2 validation = new Validation3Dot2(rdapResponseJson.toString(), results, config,
        QUERY_TYPE);

    validateOk(validation);
  }

  @Test
  public void testValidate_NoLinksInTopmostObject_AddResults23200() {
    rdapResponseJson.remove("links");

    Validation3Dot2 validation = new Validation3Dot2(rdapResponseJson.toString(), results, config,
        QUERY_TYPE);

    validateNotOk(validation, -23200, "",
        "A links data structure in the topmost object exists, and the links object shall "
            + "contain the elements rel:related and href, but they were not found. "
            + "See section 3.2 of the RDAP_Technical_Implementation_Guide_2_1.");
  }

  @Test
  public void testValidate_NoRelatedLink_AddResults23200() {
    rdapResponseJson.getJSONArray("links").forEach(l -> {
      JSONObject link = (JSONObject) l;
      if (link.getString("rel").equals("related")) {
        link.put("rel", "self");
      }
    });

    Validation3Dot2 validation = new Validation3Dot2(rdapResponseJson.toString(), results, config,
        QUERY_TYPE);

    validateNotOk(validation, -23200, rdapResponseJson.getJSONArray("links").toString(),
        "A links data structure in the topmost object exists, and the links object shall "
            + "contain the elements rel:related and href, but they were not found. "
            + "See section 3.2 of the RDAP_Technical_Implementation_Guide_2_1.");
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
        config, QUERY_TYPE);

    validateNotOk(validation, -23200, rdapResponseJson.getJSONArray("links").toString(),
        "A links data structure in the topmost object exists, and the links object shall "
            + "contain the elements rel:related and href, but they were not found. "
            + "See section 3.2 of the RDAP_Technical_Implementation_Guide_2_1.");
  }

  @Test
  public void testDoLaunch_NotARegistry_IsFalse() {
    doReturn(false).when(config).isGtldRegistry();
    assertThat(new Validation3Dot2(rdapResponseJson.toString(), results, config, QUERY_TYPE).doLaunch()).isFalse();
  }

  @Test
  public void testDoLaunch_NotADomainQuery_IsFalse() {
    doReturn(true).when(config).isGtldRegistry();
    assertThat(new Validation3Dot2(rdapResponseJson.toString(), results, config, RDAPQueryType.NAMESERVER).doLaunch())
        .isFalse();
  }
}