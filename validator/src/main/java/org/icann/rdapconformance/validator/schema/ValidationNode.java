package org.icann.rdapconformance.validator.schema;

import java.util.Objects;
import java.util.Optional;

public class ValidationNode {

  private SchemaNode schemaNode;
  private String validationName;
  private Optional<SchemaNode> parentValidationNode = Optional.empty();

  public ValidationNode(SchemaNode schemaNode, String validationName) {
    this.schemaNode = schemaNode;
    this.validationName = validationName;
    this.parentValidationNode =
        schemaNode.findAssociatedParentValidationNode(getValidationKey());
  }

  public String getValidationKey() {
    return (String) schemaNode.getErrorKey(validationName);
  }

  public SchemaNode getSchemaNode() {
    return schemaNode;
  }

  public boolean hasParentValidationCode() {
    return parentValidationNode.isPresent();
  }

  public int getParentValidationCode() {
    return (int) parentValidationNode.get().getErrorKey(getValidationKey());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ValidationNode that = (ValidationNode) o;
    return schemaNode.equals(that.schemaNode) &&
        validationName.equals(that.validationName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(schemaNode, validationName);
  }
}
