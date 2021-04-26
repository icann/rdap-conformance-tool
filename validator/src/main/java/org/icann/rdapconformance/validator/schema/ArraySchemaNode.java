package org.icann.rdapconformance.validator.schema;

import java.util.List;
import java.util.stream.Collectors;
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
    if (arraySchema.getItemSchemas() != null && !arraySchema.getItemSchemas().isEmpty()) {
      return arraySchema.getItemSchemas().stream().map(s -> create(this, s))
          .collect(Collectors.toList());
    } else if (arraySchema.getAllItemSchema() != null) {
      return List.of(create(this, arraySchema.getAllItemSchema()));
    } else if (arraySchema.getContainedItemSchema() != null) {
      return List.of(create(this, arraySchema.getContainedItemSchema()));
    }

    throw new IllegalArgumentException("Array sub schema unknown: " + arraySchema);
  }
}
