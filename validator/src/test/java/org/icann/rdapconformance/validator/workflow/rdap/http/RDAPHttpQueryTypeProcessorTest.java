package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.net.URI;
import org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryTypeProcessor;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationStatus;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class RDAPHttpQueryTypeProcessorTest {

  private final RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
  private final RDAPQueryTypeProcessor processor = new RDAPHttpQueryTypeProcessor(config);
  private final RDAPDatasetService datasetService = new RDAPDatasetServiceMock();

  @Test
  public void testDomainQuery() {
    URI uri = URI.create("http://rdap.server.example/domain/test.example");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(new RDAPDatasetServiceMock())).isTrue();
    assertThat(processor.getQueryType()).isEqualTo(RDAPQueryType.DOMAIN);
  }

  @Test
  public void testNameserverQuery() {
    URI uri = URI.create("http://rdap.server.example/nameserver/test.example");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(new RDAPDatasetServiceMock())).isTrue();
    assertThat(processor.getQueryType()).isEqualTo(RDAPQueryType.NAMESERVER);
  }

  @Test
  public void testEntityQuery() {
    URI uri = URI.create("http://rdap.server.example/entity/VG-1234");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(datasetService)).isTrue();
    assertThat(processor.getQueryType()).isEqualTo(RDAPQueryType.ENTITY);
  }

  @Test
  public void testHelpQuery() {
    URI uri = URI.create("http://rdap.server.example/help");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(datasetService)).isTrue();
    assertThat(processor.getQueryType()).isEqualTo(RDAPQueryType.HELP);
  }

  @Test
  public void testNameserversQuery() {
    URI uri = URI.create("http://rdap.server.example/nameservers?ip=.*");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(datasetService)).isTrue();
    assertThat(processor.getQueryType()).isEqualTo(RDAPQueryType.NAMESERVERS);
  }

  @Test
  public void testUnsupportedQuery_ErrorStatusIs3() {
    URI uri = URI.create("http://rdap.server.example/");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(datasetService)).isFalse();
    assertThat(processor.getErrorStatus()).isEqualTo(RDAPValidationStatus.UNSUPPORTED_QUERY);
  }

  @Test
  @Ignore("Validation not yet implemented")
  public void testCheckInvalidDomainName_ErrorStatusIs4() {
    URI uri = URI.create("http://rdap.server.example/domain/xn--abcdé");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(datasetService)).isFalse();
    assertThat(processor.getErrorStatus()).isEqualTo(RDAPValidationStatus.MIXED_LABEL_FORMAT);
  }

  @Test
  @Ignore("Validation not yet implemented")
  public void testCheckInvalidNameserverName_ErrorStatusIs4() {
    URI uri = URI.create("http://rdap.server.example/nameserver/xn--abcdé");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(datasetService)).isFalse();
    assertThat(processor.getErrorStatus()).isEqualTo(RDAPValidationStatus.MIXED_LABEL_FORMAT);
  }

}