package org.icann.rdapconformance.validator.workflow.profile.tig_section.registrar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarId;
import org.testng.annotations.Test;

public class TigValidation1Dot12Dot1Test extends ProfileJsonValidationTestBase {

  RDAPQueryType queryType = RDAPQueryType.DOMAIN;

  public TigValidation1Dot12Dot1Test() {
    super(
        "/validators/profile/tig_section/entities/valid.json",
        "tigSection_1_12_1_Validation");
  }

  @Override
  public ProfileJsonValidation getProfileValidation() {
    return new TigValidation1Dot12Dot1(jsonObject.toString(), results, datasets, queryType);
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
    queryType = RDAPQueryType.HELP;
    assertThat(getProfileValidation().doLaunch()).isFalse();
    queryType = RDAPQueryType.NAMESERVERS;
    assertThat(getProfileValidation().doLaunch()).isFalse();
  }
}