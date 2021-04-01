package org.icann.rdapconformance.validator.schema;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.everit.json.schema.Schema;

public class SimpleSchemaNode extends SchemaNode {

  protected SimpleSchemaNode(SchemaNode parentNode, Schema schema) {
    super(parentNode, schema);
  }

  @Override
  public List<SchemaNode> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public Optional<ObjectSchemaNode> findParentOfNodeWith(String key) {
    return Optional.empty();
  }
}
