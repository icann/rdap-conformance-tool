package org.icann.rdapconformance.validator.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;

public class ObjectSchemaNode extends SchemaNode {

  private ObjectSchema objectSchema;

  public ObjectSchemaNode(SchemaNode parentNode, Schema schema) {
    super(parentNode, schema);
    this.objectSchema = (ObjectSchema)schema;
  }

  @Override
  public List<SchemaNode> getChildren() {
    List<SchemaNode> schemaNodes = new ArrayList<>();
    Map<String, Schema> schemaMap = objectSchema.getPropertySchemas();
    for (Schema childSchema : schemaMap.values()) {
      schemaNodes.add(create(this, childSchema));
    }
    return schemaNodes;
  }

  public SchemaNode getChild(String schemaName) {
    return create(this, objectSchema.getPropertySchemas().get(schemaName));
  }

  @Override
  public Optional<ObjectSchemaNode> findParentOfNodeWith(String key) {
    if (objectSchema.getPropertySchemas().containsKey(key)) {
      return Optional.of(this);
    }

    return super.findParentOfNodeWith(key);
  }
}
