package org.icann.rdapconformance.validator.schema;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.ConstSchema;
import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;

public abstract class SchemaNode {

  private static final Set<?> SIMPLE_SCHEMAS = Set.of(
      NumberSchema.class,
      StringSchema.class,
      ConstSchema.class,
      EnumSchema.class,
      BooleanSchema.class);
  protected final SchemaNode parentNode;
  protected final Schema schema;

  protected SchemaNode(SchemaNode parentNode, Schema schema) {
    this.parentNode = parentNode;
    this.schema = schema;
  }

  public static SchemaNode create(SchemaNode parentNode, Schema schema) {
    if (schema instanceof ObjectSchema) {
      return new ObjectSchemaNode(parentNode, schema);
    } else if (SIMPLE_SCHEMAS.contains(schema.getClass())) {
      return new SimpleSchemaNode(parentNode, schema);
    } else if (schema instanceof ReferenceSchema) {
      return create(parentNode, ((ReferenceSchema) schema).getReferredSchema());
    } else if (schema instanceof ArraySchema) {
      return create(parentNode, ((ArraySchema) schema).getAllItemSchema());
    } else if (schema instanceof CombinedSchema) {
      return new CombinedSchemaNode(parentNode, schema);
    }

    return null;
  }

  public abstract List<SchemaNode> getChildren();

  public boolean containsErrorKey(String errorKey) {
    return schema.getUnprocessedProperties().containsKey(errorKey);
  }

  public int getErrorCode(String errorKey) {
    return (int) schema.getUnprocessedProperties().get(errorKey);
  }

  public Optional<SchemaNode> findBottomNode(String searchKey) {
    List<SchemaNode> schemaNodes = getChildren();
    for (SchemaNode schemaNode : schemaNodes) {
      Optional<SchemaNode> foundNode = schemaNode.findBottomNode(searchKey);
      if (foundNode.isPresent()) {
        return foundNode;
      }
    }

    return Optional.empty();
  }

  /**
   * Find the corresponding the closest error key from the searchKey in the JSON hierarchy. e.g.: {
   * "firstLevel": { "secondLevel": { "searchKey": "test" } } "errorKey": -1 } In this example,
   * since the errorKey doesn't exist at the second level, this function will take the "errorKey" =
   * -1 at the first level.
   */
  public int searchBottomMostErrorCode(String searchKey, String errorKey) {
    String unfoundError =
        "No such error key (" + errorKey + ") in the hierarchy around " + searchKey;
    Optional<SchemaNode> optNode = findBottomNode(searchKey);
    if (optNode.isEmpty()) {
      throw new IllegalArgumentException(unfoundError);
    }
    SchemaNode node = optNode.get();
    SchemaNode parent = node;
    while (parent != null && !parent.containsErrorKey(errorKey)) {
      parent = parent.parentNode;
    }

    if (parent.containsErrorKey(errorKey)) {
      return parent.getErrorCode(errorKey);
    }

    throw new IllegalArgumentException(unfoundError);
  }
}
