package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.TigValidationFromSchemaTestBase;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.Test;

public class Validation6Dot1Test extends TigValidationFromSchemaTestBase {

  private final static RDAPQueryType QUERY_TYPE = RDAPQueryType.DOMAIN;

  public Validation6Dot1Test() {
    super("rdap_entities.json", "/validators/profile/tig_section/entities/valid.json", "tigSection_6_1_Validation");
  }

  @Override
  public ProfileJsonValidation getTigValidation() {
    return new Validation6Dot1(jsonObject.toString(), results, QUERY_TYPE);
  }

  @Test
  public void testValidate_RegistrarEntityWithoutPublicIds_AddResults23300() {
    removeKey("$['entities'][1]['publicIds']");
    validate(-23300, "{\"roles\":[\"test\",\"registrar\"]}",
        "A publicIds member is not included in the entity with the registrar role.");
  }

  @Test
  public void testValidate_RegistrarEntityWithPublicIdIdentifierNotAPositiveInteger_AddResults23301() {
    replaceValue("$['entities'][1]['publicIds'][1]['identifier']", "abc");
    validate(
        -23301,
        "{\"type\":\"IANA Registrar ID\",\"identifier\":\"abc\"}",
        "The identifier of the publicIds member of the entity with the registrar role is not a positive integer.");
  }

  @Test
  public void testDoLaunch() {
    assertThat(new Validation6Dot1("", results, RDAPQueryType.DOMAIN).doLaunch())
        .isTrue();
    assertThat(new Validation6Dot1("", results, RDAPQueryType.NAMESERVER).doLaunch())
        .isTrue();
    assertThat(new Validation6Dot1("", results, RDAPQueryType.ENTITY).doLaunch())
        .isTrue();
  }

  @Test
  public void testDoLaunch_NotAValidQuery_IsFalse() {
    assertThat(new Validation6Dot1("", results, RDAPQueryType.NAMESERVERS).doLaunch())
        .isFalse();
    assertThat(new Validation6Dot1("", results, RDAPQueryType.HELP).doLaunch())
        .isFalse();
  }
}