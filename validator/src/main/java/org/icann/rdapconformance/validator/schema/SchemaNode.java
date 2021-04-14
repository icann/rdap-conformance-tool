package org.icann.rdapconformance.validator.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
    } else if (schema instanceof ReferenceSchema) {
      return new ReferenceSchemaNode(parentNode, schema);
    } else if (schema instanceof ArraySchema) {
      return new ArraySchemaNode(parentNode, schema);
    } else if (schema instanceof CombinedSchema) {
      return new CombinedSchemaNode(parentNode, schema);
    } else {
      return new SimpleSchemaNode(parentNode, schema);
    }
  }

  public abstract List<SchemaNode> getChildren();

  public List<SchemaNode> getAllCombinedChildren() {
    List<SchemaNode> children = new ArrayList<>();
    return getAllCombinedChildrenRecursively(children);
  }

  List<SchemaNode> getAllCombinedChildrenRecursively(List<SchemaNode> children) {
    if (getChildren().isEmpty()) {
      children.add(this);
    } else {
      for (SchemaNode child : getChildren()) {
        if (child instanceof CombinedSchemaNode || child instanceof ReferenceSchemaNode) {
          child.getAllCombinedChildrenRecursively(children);
        } else {
          children.add(child);
        }
      }
    }
    return children;
  }

  public boolean containsErrorKey(String errorKey) {
    return schema.getUnprocessedProperties().containsKey(errorKey);
  }

  public int getErrorCode(String errorKey) {
    return (int) schema.getUnprocessedProperties().get(errorKey);
  }

  public Object getErrorKey(String errorKey) {
    return schema.getUnprocessedProperties().get(errorKey);
  }

  public Optional<ObjectSchemaNode> findParentOfNodeWith(String key) {
    List<SchemaNode> schemaNodes = getChildren();
    for (SchemaNode schemaNode : schemaNodes) {
      Optional<ObjectSchemaNode> foundNode = schemaNode.findParentOfNodeWith(key);
      if (foundNode.isPresent()) {
        return foundNode;
      }
    }

    return Optional.empty();
  }

  public Optional<SchemaNode> findChild(String key) {
    return findParentOfNodeWith(key)
        .map(p -> p.getChild(key))
        .map(c -> {
          if (c instanceof ReferenceSchemaNode) {
            return ((ReferenceSchemaNode)c).getChild();
          }
          return c;
        });
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
    Optional<SchemaNode> optNode = findChild(searchKey);
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

  public Schema getSchema() {
    return schema;
  }

  Optional<SchemaNode> findAssociatedSchema(String jsonPointer) {
    String[] elements = jsonPointer.split("/");
    if (elements.length < 2) {
      return Optional.empty();
    }

    SchemaNode schemaNode = this;
    int i = 1;
    do {
      try {
        Integer.parseInt(elements[i]);
      } catch (NumberFormatException e) {
        // we have a string
        String schemaName = elements[i];
        Optional<ObjectSchemaNode> node = schemaNode.findParentOfNodeWith(schemaName);
        if (node.isPresent()) {
          schemaNode = node.get().getChild(schemaName);
        } else {
          return Optional.empty();
        }
      }
      i++;
    } while (i < elements.length);

    return Optional.of(schemaNode);
  }

  public Set<ValidationNode> findValidationNodes(String jsonPointer, String validationName) {
    List<SchemaNode> schemaNodes = findAssociatedSchema(jsonPointer)
        .map(s -> s instanceof ReferenceSchemaNode ? ((ReferenceSchemaNode) s).getChild() : s)
        .map(s -> s instanceof CombinedSchemaNode ? s.getAllCombinedChildren() : List.of(s))
        .orElse(Collections.emptyList());

    Set<ValidationNode> validationNodes = new HashSet<>();
    for (SchemaNode parent : schemaNodes) {
      while (parent != null) {
        if (parent.containsErrorKey(validationName)) {
          validationNodes.add(new ValidationNode(parent, validationName));
        }
        parent = parent.parentNode;
      }
    }

    return validationNodes;
  }

  public Optional<SchemaNode> findAssociatedParentValidationNode(String validationKey) {
    SchemaNode parent = this;
    while (parent != null && !parent.containsErrorKey(validationKey)) {
      parent = parent.parentNode;
    }
    if (parent != null) {
      return Optional.of(parent);
    }
    return Optional.empty();
  }
}
