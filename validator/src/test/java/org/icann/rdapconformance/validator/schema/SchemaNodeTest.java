package org.icann.rdapconformance.validator.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.testng.annotations.Test;

public class SchemaNodeTest {

  SchemaNode schemaNode = SchemaNode.create(null, SchemaValidator.getSchema("simple.json", "schema/",
      getClass().getClassLoader()));

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