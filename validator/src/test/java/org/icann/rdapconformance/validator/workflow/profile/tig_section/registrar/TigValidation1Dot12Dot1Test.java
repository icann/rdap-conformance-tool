package org.icann.rdapconformance.validator.workflow.profile.tig_section.registrar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarId;
import org.testng.annotations.Test;

public class TigValidation1Dot12Dot1Test extends ProfileJsonValidationTestBase {

  public TigValidation1Dot12Dot1Test() {
    super(
        "/validators/profile/tig_section/entities/valid.json",
        "tigSection_1_12_1_Validation");
  }

  public ProfileJsonValidation getProfileValidation() {
    return new TigValidation1Dot12Dot1(queryContext);
  }

  /**
   * 8.3.1.1
   */
  @Test
  public void publicIdWithoutIdentifier() {
    removeKey("$['entities'][1]['publicIds'][1]['identifier']");
    validate(-26100, "#/entities/1/publicIds/1:{\"type\":\"IANA Registrar ID\"}",
        "An identifier in the publicIds within the entity data "
            + "structure with the registrar role was not found. See section 1.12.1 of the "
            + "RDAP_Technical_Implementation_Guide_2_1.");
  }

  /**
   * 8.3.1.2
   */
  @Test
  public void identifierNotInRegistrarIdDataset() {
    int registrarIdUnknown = -999;
    doReturn(false).when(datasets.get(RegistrarId.class)).containsId(registrarIdUnknown);
    replaceValue("$['entities'][1]['publicIds'][1]['identifier']", registrarIdUnknown);
    validate(-26101, "#/entities/1/publicIds/1/identifier:-999",
        "The registrar identifier is not included in the registrarId. "
            + "See section 1.12.1 of the RDAP_Technical_Implementation_Guide_2_1.");
  }

  /**
   * 8.3.1.3
   */
  @Test
  public void registrarIdUrlIsNotHttps() {
    String wrongXml = "<record date=\"2020-11-25\" updated=\"2021-01-28\">\n"
        + "      <value>292</value>\n"
        + "      <name>Test</name>\n"
        + "      <status>Accredited</status>\n"
        + "      <rdapurl>\n"
        + "        <server>ftp://example.com/</server>\n"
        + "      </rdapurl>\n"
        + "    </record>";
    RegistrarId.Record record = new RegistrarId.Record(
        292,
        "Test",
        "ftp://example.com/",
        wrongXml);
    doReturn(record)
        .when(datasets.get(RegistrarId.class))
        .getById(292);
    validate(-26102, "#/entities/1/publicIds/0/identifier:" + wrongXml,
        "One or more of the base URLs for the registrar contain a schema different from https. "
            + "See section 1.2 of the RDAP_Technical_Implementation_Guide_2_1.");
  }

  /**
   * 8.3.1.3
   */
  @Test
  public void registrarIdWithNoUrl() {
    String wrongXml = "<record date=\"2020-11-25\" updated=\"2021-01-28\">\n"
        + "      <value>292</value>\n"
        + "      <name>Test</name>\n"
        + "      <status>Accredited</status>\n"
        + "    </record>";
    RegistrarId.Record record = new RegistrarId.Record(
        292,
        "Test",
        "",
        wrongXml);
    doReturn(record)
        .when(datasets.get(RegistrarId.class))
        .getById(292);
    validate();
  }

  @Test
  public void testDoLaunch_NotARegistryNorRegistrar_IsFalse() {
    queryContext.setQueryType(RDAPQueryType.HELP);
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryContext.setQueryType(RDAPQueryType.NAMESERVERS);
    assertThat(getProfileValidation().doLaunch()).isFalse();
  }

  @Test
  public void relatedLinkMissing_reportsMinus26103() {
    when(config.isGtldRegistry()).thenReturn(true);
    queryContext.setQueryType(RDAPQueryType.DOMAIN);

    RegistrarId.Record accreditedRecord = new RegistrarId.Record(
            292, "Test", "https://example.com/", "Accredited", "<record>...</record>");
    RegistrarId.Record accreditedRecord293 = new RegistrarId.Record(
            293, "Test2", "https://example.com/", "Accredited", "<record>...</record>");

    doReturn(accreditedRecord).when(datasets.get(RegistrarId.class)).getById(292);
    doReturn(accreditedRecord293).when(datasets.get(RegistrarId.class)).getById(293);

    validate(-26103, "",
            "Referral to registrar is either unregistered with IANA or invalid.");
  }

  /**
   * 8.3.1.4 — passing path for -26103:
   * When the response has a "related" link whose href starts with the registrar's
   * IANA rdapUrl prefix and ends with a valid domain name, validation must pass.
   * This also guards against the double-/domain/ prefix regression.
   */
  @Test
  public void relatedLinkPresent_withValidDomain_passes() {
    when(config.isGtldRegistry()).thenReturn(true);
    queryContext.setQueryType(RDAPQueryType.DOMAIN);

    // Dataset URL already ends with /domain/ — the href must NOT add another /domain/
    RegistrarId.Record accreditedRecord = new RegistrarId.Record(
            292, "Test", "https://rdap.example-registrar.com/rdap/domain/",
            "Accredited", "<record>...</record>");
    RegistrarId.Record accreditedRecord293 = new RegistrarId.Record(
            293, "Test2", "https://rdap.example-registrar.com/rdap/domain/",
            "Accredited", "<record>...</record>");

    doReturn(accreditedRecord).when(datasets.get(RegistrarId.class)).getById(292);
    doReturn(accreditedRecord293).when(datasets.get(RegistrarId.class)).getById(293);

    // Add a top-level "related" link whose href = rdapUrl + valid domain name
    putValue("$['links'][0]", "rel", "related");
    putValue("$['links'][0]", "href",
            "https://rdap.example-registrar.com/rdap/domain/example.com");

    validate(); // expects no errors
  }
}