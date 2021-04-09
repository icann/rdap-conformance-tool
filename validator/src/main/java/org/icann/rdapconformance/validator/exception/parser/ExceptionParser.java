package org.icann.rdapconformance.validator.exception.parser;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.schema.SchemaNode;
import org.icann.rdapconformance.validator.schema.ValidationNode;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ExceptionParser {

  private static final Logger logger = LoggerFactory.getLogger(ExceptionParser.class);
  protected final ValidationException e;
  protected final Schema schema;
  protected final JSONObject schemaObject;
  protected final JSONObject jsonObject;
  protected final RDAPValidatorResults results;

  protected ExceptionParser(ValidationException e, Schema schema,
      JSONObject jsonObject, RDAPValidatorResults results) {
    this.e = e;
    this.schema = schema;
    this.schemaObject = new JSONObject(schema.toString());
    this.jsonObject = jsonObject;
    this.results = results;
  }

  public static List<ExceptionParser> createParsers(
      ValidationException e,
      Schema schema,
      JSONObject object, RDAPValidatorResults results) {
    List<ExceptionParser> parsers = new ArrayList<>();

    List<ValidationException> basicExceptions = getAllExceptions(List.of(e));
    for (ValidationException basicException : basicExceptions) {
      parsers.add(new UnknowKeyExceptionParser(basicException, schema, object, results));
      parsers.add(new BasicTypeExceptionParser(basicException, schema, object, results));
      parsers.add(new EnumExceptionParser(basicException, schema, object, results));
      parsers.add(new MissingKeyExceptionParser(basicException, schema, object, results));
      parsers.add(new ConstExceptionParser(basicException, schema, object, results));
      parsers.add(new ContainsConstExceptionParser(basicException, schema, object, results));
      parsers.add(new RegexExceptionParser(basicException, schema, object, results));
      parsers.add(new DatetimeExceptionParser(basicException, schema, object, results));
    }

    return parsers;
  }

  protected int parseErrorCode(Supplier<Integer> getErrorCodeFn) {
    try {
      return getErrorCodeFn.get();
    } catch (Exception parseException) {
      logger.info("Can't find the corresponding error in schema, replacing by -999");
      return -999;
    }
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
    return e.getViolatedSchema().getUnprocessedProperties().get(key);
  }

  protected static int getErrorCodeFromViolatedSchema(ValidationException e) {
    return (int) getPropertyFromViolatedSchema(e, "errorCode");
  }

  public abstract boolean matches(ValidationException e);

  public void parse() {
    if (matches(e)) {
      doParse();
      if (e.getPointerToViolation() != null) {
        SchemaNode tree = SchemaNode.create(null, schema);
        ValidationNode validationNode = tree.findValidationNode(e.getPointerToViolation(),
            "validationName");
        if (validationNode.hasParentValidationCode()) {
          results.add(RDAPValidationResult.builder()
              .code(
                  parseErrorCode(validationNode::getParentValidationCode))
              .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
              .message(MessageFormat.format("The value for the JSON name value does not pass {0} "
                  + "validation [{1}].", e.getPointerToViolation(), validationNode.getValidationKey()))
              .build());
        }
      }
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
