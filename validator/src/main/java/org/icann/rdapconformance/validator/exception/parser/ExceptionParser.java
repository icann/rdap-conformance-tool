package org.icann.rdapconformance.validator.exception.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.json.JSONObject;

public abstract class ExceptionParser {

  protected final ValidationException e;
  protected final Schema schema;
  protected final JSONObject schemaObject;
  protected final JSONObject jsonObject;
  protected final RDAPValidatorContext context;

  protected ExceptionParser(ValidationException e, Schema schema,
      JSONObject jsonObject, RDAPValidatorContext context) {
    this.e = e;
    this.schema = schema;
    this.schemaObject = new JSONObject(schema.toString());
    this.jsonObject = jsonObject;
    this.context = context;
  }

  public static List<ExceptionParser> createParsers(
      ValidationException e,
      Schema schema,
      JSONObject object, RDAPValidatorContext context) {
    List<ExceptionParser> exceptionParsers = new ArrayList<>();

    List<ValidationException> basicExceptions = getAllExceptions(List.of(e));
    for (ValidationException basicException : basicExceptions) {
      exceptionParsers.add(new UnknowKeyExceptionParser(basicException, schema, object, context));
      exceptionParsers.add(new BasicTypeExceptionParser(basicException, schema, object, context));
      exceptionParsers.add(new EnumExceptionParser(basicException, schema, object, context));
      exceptionParsers.add(new MissingKeyExceptionParser(basicException, schema, object, context));
    }

    exceptionParsers.add(new ComplexTypeExceptionParser(e, schema, object, context));

    return exceptionParsers;
  }

  protected int getErrorCode(String validationName) {
    String parentSchemaName = getParentSchemaName(e.getPointerToViolation());
    Schema parentSchema = getParentSchema(parentSchemaName, schema);
    return (int) parentSchema.getUnprocessedProperties().get(validationName);
  }

  private Schema getParentSchema(String parentSchemaName, Schema childSchema) {
    if (parentSchemaName.equals("#")) {
      return childSchema;
    }

    Map<String, Schema> childSchemas = ((ObjectSchema) childSchema).getPropertySchemas();

    if (childSchemas.containsKey(parentSchemaName)) {
      return ((ReferenceSchema) childSchemas.get(parentSchemaName)).getReferredSchema();
    }

    for (var schema : childSchemas.values()) {
      Schema foundSchema = getParentSchema(parentSchemaName, schema);
      if (foundSchema != null) {
        return foundSchema;
      }
    }

    return null;
  }

  public static List<ValidationException> getAllExceptions(
      List<ValidationException> causingExceptions) {
    List<ValidationException> allExceptions = new ArrayList<>();
    if (causingExceptions.isEmpty()) {
      return allExceptions;
    }

    for (ValidationException causingException : causingExceptions) {
      if (causingException.getCausingExceptions().isEmpty()) {
        allExceptions.add(causingException);
      }

      List<ValidationException> candidateExceptions =
          getAllExceptions(causingException.getCausingExceptions());
      allExceptions.addAll(
          candidateExceptions.stream().filter(c -> c.getCausingExceptions().isEmpty()).collect(
              Collectors.toList()));
    }
    return allExceptions;
  }

  protected static Object getPropertyFromViolatedSchema(ValidationException e, String key) {
    return e.getViolatedSchema().getUnprocessedProperties().get(
        key);
  }

  protected static int getErrorCodeFromViolatedSchema(ValidationException e) {
    return (int) getPropertyFromViolatedSchema(e, "errorCode");
  }

  protected abstract boolean matches(ValidationException e);

  public void parse() {
    if (matches(e)) {
      doParse();
    }
  }

  protected abstract void doParse();

  static String getParentSchemaName(String jsonPointer) {
    String[] elements = jsonPointer.split("/");
    for (int i = elements.length - 2; i > 0; i--) {
      try {
        Integer.parseInt(elements[i]);
      } catch (NumberFormatException e) {
        // we have a string
        return elements[i];
      }
    }
    return "#";
  }
}
