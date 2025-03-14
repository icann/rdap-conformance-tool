package org.icann.rdapconformance.validator.schema;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.Test;

import java.net.URI;

public class SchemaNodeGroupOkTest {

  SchemaNode schemaNode = SchemaNode.create(null, SchemaValidator.getSchema("parent.json",
          "schema/groupOk/",
          getClass().getClassLoader(), new RDAPDatasetServiceMock(),
          new RDAPValidatorConfiguration() {
            @Override
            public URI getConfigurationFile() {
              return null;
            }

            @Override
            public URI getUri() {
              return null;
            }

            @Override
            public void setUri(URI uri) {

            }

            @Override
            public int getTimeout() {
              return 0;
            }

            @Override
            public int getMaxRedirects() {
              return 0;
            }

            @Override
            public boolean useLocalDatasets() {
              return false;
            }

            @Override
            public boolean useRdapProfileFeb2019() {
              return false;
            }

            @Override
            public boolean isGtldRegistrar() {
              return false;
            }

            @Override
            public boolean isGtldRegistry() {
              return false;
            }

            @Override
            public boolean isThin() {
              return false;
            }

            @Override
            public boolean isNoIPV4Queries() {
              return false;
            }

            @Override
            public RDAPQueryType getQueryType() {
              return null;
            }
          }));

  @Test
  public void testFindAllValuesOfKey() {
    assertThat(schemaNode.findAllValuesOf("validationName")).contains(
        "childValidation",
        "childItemValidation",
        "childOfChildValidation"
    );
  }
}