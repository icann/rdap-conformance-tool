package org.icann.rdapconformance.validator.schema;

import java.util.Optional;

public class ValidationNode {

  private SchemaNode schemaNode;
  private String validationName;
  private Optional<SchemaNode> parentValidationNode = Optional.empty();

  public ValidationNode() {
  }

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
}
