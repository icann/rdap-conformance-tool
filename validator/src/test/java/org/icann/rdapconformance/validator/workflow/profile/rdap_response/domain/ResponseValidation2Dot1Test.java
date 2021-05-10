package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URI;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot1Test extends ProfileJsonValidationTestBase {

  private RDAPValidatorConfiguration config;
  private RDAPQueryType queryType;


  public ResponseValidation2Dot1Test() {
    super("/validators/domain/valid.json", "rdapResponseProfile_2_1_Validation");
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    queryType = RDAPQueryType.DOMAIN;
    config = mock(RDAPValidatorConfiguration.class);
    doReturn(URI.create("http://domain/test.example")).when(config).getUri();
  }

  @Override
  public ProfileValidation getTigValidation() {
    return new ResponseValidation2Dot1(jsonObject.toString(), results, config, queryType);
  }

  @Test
  public void testValidate_UriContainsOnlyALabelOrNrLdhName_Ok() {
    replaceValue("ldhName", "toto");
    doReturn(URI.create("http://domain/test.xn--viagnie-eya.example")).when(config).getUri();
    validate();
  }

  @Test
  public void testValidate_URIContainsULabel_Ok() {
    doReturn(URI.create("http://domain/test.viagénie.example")).when(config).getUri();
    jsonObject.put("unicodeName", "test.viagénie.example");
    validate();
  }

  @Test
  public void testValidate_UriContainsOnlyALabelButNoLdhName_AddResults46100() {
    doReturn(URI.create("http://domain/test.xn--viagnie-eya.example")).when(config).getUri();
    removeKey("ldhName");
    validate(-46100, jsonObject.toString(),
        "The RDAP Query URI contains only A-label or NR-LDH labels, the topmost domain "
            + "object does not contain a ldhName member. "
            + "See section 2.1 of the RDAP_Response_Profile_2_1.");
  }

  @Test
  public void testValidate_UriContainsULabelButNoUnicodeName_AddResults46101() {
    doReturn(URI.create("http://domain/test.viagénie.example")).when(config).getUri();
    validate(-46101, jsonObject.toString(),
        "The RDAP Query URI contains one or more U-label, the topmost domain object does "
            + "not contain a unicodeName member. "
            + "See section 2.1 of the RDAP_Response_Profile_2_1.");
  }

  @Test
  public void testDoLaunch_NotADomainQuery_IsFalse() {
    queryType = RDAPQueryType.NAMESERVER;
    assertThat(getTigValidation().doLaunch()).isFalse();
  }

}