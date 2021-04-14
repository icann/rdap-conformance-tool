package org.icann.rdapconformance.validator.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;

public class ValidationExceptionNode {

  private final ValidationExceptionNode parentException;
  private final ValidationException causingException;
  private final Set<?> containerSchemas = Set.of(
      ArraySchema.class,
      CombinedSchema.class,
      ReferenceSchema.class
  );

  public ValidationExceptionNode(ValidationExceptionNode parentException,
      ValidationException causingException) {
    this.parentException = parentException;
    this.causingException = causingException;
  }

  public ValidationExceptionNode getParentException() {
    return parentException;
  }

  public List<ValidationExceptionNode> getChildren() {
    return causingException
        .getCausingExceptions()
        .stream()
        .map(c -> new ValidationExceptionNode(this, c))
        .collect(Collectors.toList());
  }

  public List<ValidationExceptionNode> getAllExceptions() {
    List<ValidationExceptionNode> children = new ArrayList<>();
    return getAllExceptionsRecursively(children);
  }

  List<ValidationExceptionNode> getAllExceptionsRecursively(List<ValidationExceptionNode> children) {
    if (getChildren().isEmpty()) {
      children.add(this);
    } else {
      for (ValidationExceptionNode child : getChildren()) {
        child.getAllExceptionsRecursively(children);
      }
    }
    return children;
  }

  public String getPointerToViolation() {
    return causingException.getPointerToViolation();
  }

  public Schema getViolatedSchema() {
    return causingException.getViolatedSchema();
  }

  public String getSchemaLocation() {
    String schemaLocation = "the associated";
    if (causingException.getSchemaLocation() != null) {
      schemaLocation = causingException.getSchemaLocation().replace("classpath://json-schema/", "");
    }
    return schemaLocation;
  }

  public String getMessage() {
    return causingException.getMessage();
  }

  public String getKeyword() {
    return causingException.getKeyword();
  }

  public Object getPropertyFromViolatedSchema(String key) {
    ValidationExceptionNode parent = this;
    while (!parent.getViolatedSchema().getUnprocessedProperties().containsKey(key)) {
      parent = parent.getParentException();

      // we follow the parent only if it is a container schema:
      if (!containerSchemas.contains(parent.getViolatedSchema().getClass())) {
        break;
      }
    }

    return parent.getViolatedSchema().getUnprocessedProperties().get(key);
  }

  public int getErrorCodeFromViolatedSchema() {
    return (int) getPropertyFromViolatedSchema("errorCode");
  }

  public String getMessage(String defaultMsg) {
    try {
      String errorMsg = (String) getPropertyFromViolatedSchema("errorMsg");
      if (errorMsg != null) {
        return errorMsg;
      }
    } catch (Exception ex) {
      // ignored
    }
    return defaultMsg;
  }
}
