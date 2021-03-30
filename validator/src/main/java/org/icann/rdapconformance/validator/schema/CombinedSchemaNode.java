package org.icann.rdapconformance.validator.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.Schema;

public class CombinedSchemaNode extends SchemaNode {

  private final CombinedSchema combinedSchema;

  protected CombinedSchemaNode(SchemaNode parentNode, Schema schema) {
    super(parentNode, schema);
    this.combinedSchema = (CombinedSchema)schema;
  }

  @Override
  public List<SchemaNode> getChildren() {
    List<SchemaNode> schemaNodes = new ArrayList<>();
    for (Schema subschema : combinedSchema.getSubschemas()) {
      schemaNodes.add(create(this, subschema));
    }
    return schemaNodes;
  }
}
