package org.icann.rdapconformance.validator.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Optional;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.Test;

public class SchemaNodeTest {

  SchemaNode schemaNode = SchemaNode.create(null, SchemaValidator.getSchema("simple.json", "schema/",
          getClass().getClassLoader(), new RDAPDatasetServiceMock(), new RDAPValidatorConfiguration() {
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
  public void testSchemaNodeGetChildren() {
    assertThat(schemaNode.getChildren())
        .hasSize(3);
  }

  @Test
  public void testSchemaNodeContainsErrorKey() {
    assertThat(schemaNode.containsErrorKey("someCustomErrorKey")).isTrue();
  }

  @Test
  public void testSchemaNodeGetErrorCode() {
    assertThat(schemaNode.getErrorCode("someCustomErrorKey")).isEqualTo(-999);
  }

  @Test
  public void testGetParentNode() {
    var children = schemaNode.getChildren();
    assertThat(children).first().hasFieldOrPropertyWithValue("parentNode",
        schemaNode);
  }

  @Test
  public void testFindBottomNode() {
    Optional<ObjectSchemaNode> node = schemaNode.findParentOfNodeWith("test");
    assertThat(node)
        .isPresent()
        .map(n -> assertThat(n.schema.getTitle()).isEqualTo("ref"));
  }

  @Test
  public void testFindAssociatedSchema() {
    String jsonPointer = "#/anArray/0/aSubField";
    Optional<SchemaNode> childSchemaNode = schemaNode.findAssociatedSchema(jsonPointer);
    assertThat(childSchemaNode.get().schema.toString()).isEqualTo("{\"type\":\"string\",\"id\":\"aSubFieldSchemaId\"}");
  }

  @Test
  public void testFindValidationName() {
    String jsonPointer = "#/anArray/0/aSubField";
    String validationKey = schemaNode.findValidationNodes(jsonPointer,
        "validationName").stream().findFirst().get().getValidationKey();
    assertThat(validationKey).isEqualTo(
        "aCustomValidation");
  }

  @Test
  public void testSearchBottomMostErrorCode() {
    int errorCode = schemaNode.searchBottomMostErrorCode("test", "aboveRefErrorKey");
    assertThat(errorCode).isEqualTo(-555);
  }
}