package org.icann.rdapconformance.validator.schema;

import java.util.List;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;

public class ReferenceSchemaNode extends SchemaNode {

  private final ReferenceSchema referenceSchema;

  protected ReferenceSchemaNode(SchemaNode parentNode, Schema schema) {
    super(parentNode, schema);
    this.referenceSchema = (ReferenceSchema)schema;
  }

  @Override
  public List<SchemaNode> getChildren() {
    return List.of(getChild());
  }

  public SchemaNode getChild() {
    return create(this, referenceSchema.getReferredSchema());
  }
}
