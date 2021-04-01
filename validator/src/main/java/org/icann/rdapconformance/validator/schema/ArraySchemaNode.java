package org.icann.rdapconformance.validator.schema;

import java.util.List;
import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.Schema;

public class ArraySchemaNode extends SchemaNode {

  private final ArraySchema arraySchema;

  protected ArraySchemaNode(SchemaNode parentNode, Schema schema) {
    super(parentNode, schema);
    arraySchema = (ArraySchema) schema;
  }

  @Override
  public List<SchemaNode> getChildren() {
    return List.of(create(this, arraySchema.getAllItemSchema()));
  }
}
