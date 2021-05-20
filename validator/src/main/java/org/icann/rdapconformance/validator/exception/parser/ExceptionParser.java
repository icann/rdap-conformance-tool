package org.icann.rdapconformance.validator.exception.parser;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.schema.SchemaNode;
import org.icann.rdapconformance.validator.schema.ValidationNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ExceptionParser {

  private static final Logger logger = LoggerFactory.getLogger(ExceptionParser.class);
  protected final ValidationExceptionNode e;
  protected final Schema schema;
  protected final JSONObject schemaObject;
  protected final JSONObject jsonObject;
  protected final RDAPValidatorResults results;
  public final static int UNKNOWN_ERROR_CODE = -999;

  protected ExceptionParser(ValidationExceptionNode e, Schema schema,
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

    ValidationExceptionNode rootException = new ValidationExceptionNode(null, e);
    List<ValidationExceptionNode> basicExceptions = rootException.getAllExceptions();
    for (ValidationExceptionNode basicException : basicExceptions) {
      parsers.add(new UnknowKeyExceptionParser(basicException, schema, object, results));
      parsers.add(new BasicTypeExceptionParser(basicException, schema, object, results));
      parsers.add(new EnumExceptionParser(basicException, schema, object, results));
      parsers.add(new MissingKeyExceptionParser(basicException, schema, object, results));
      parsers.add(new ConstExceptionParser(basicException, schema, object, results));
      parsers.add(new ContainsConstExceptionParser(basicException, schema, object, results));
      parsers.add(new RegexExceptionParser(basicException, schema, object, results));
      parsers.add(new DatetimeExceptionParser(basicException, schema, object, results));
      parsers.add(new DependenciesExceptionParser(basicException, schema, object, results));
      parsers.add(new HostNameInUriExceptionParser(basicException, schema, object, results));
      parsers.add(new Ipv4ValidationExceptionParser(basicException, schema, object, results));
      parsers.add(new Ipv6ValidationExceptionParser(basicException, schema, object, results));
      parsers.add(new IdnHostNameExceptionParser(basicException, schema, object, results));
      parsers.add(new UniqueItemsExceptionParser(basicException, schema, object, results));
      parsers.add(new NumberExceptionParser(basicException, schema, object, results));
      parsers.add(new ComplexTypeExceptionParser(basicException, schema, object, results));
      parsers.add(new RdapExtensionsExceptionParser(basicException, schema, object, results));
      parsers.add(new DatasetExceptionParser(basicException, schema, object, results));
      parsers.add(new VcardExceptionParser(basicException, schema, object, results));
    }

    return parsers;
  }

  public static int parseErrorCode(Supplier<Integer> getErrorCodeFn) {
    try {
      return getErrorCodeFn.get();
    } catch (Exception parseException) {
      logger.info("Can't find the corresponding error in schema, replacing by {}", UNKNOWN_ERROR_CODE);
      return UNKNOWN_ERROR_CODE;
    }
  }

  public abstract boolean matches(ValidationExceptionNode e);

  public void parse() {
    if (matches(e)) {
      doParse();

      if (e.getPointerToViolation() != null) {
        validateGroupTest(e.getPointerToViolation(), jsonObject, results, schema);
      }
    }
  }

  public static void validateGroupTest(String jsonPointer, JSONObject jsonObject,
      RDAPValidatorResults results, Schema schema) {
    SchemaNode tree = SchemaNode.create(null, schema);
    Set<ValidationNode> validationNodes = tree.findValidationNodes(jsonPointer,
        "validationName");
    for (ValidationNode validationNode : validationNodes) {
      results.addGroupErrorWarning(validationNode.getValidationKey());
      if (validationNode.hasParentValidationCode()) {
        results.add(RDAPValidationResult.builder()
            .code(
                parseErrorCode(validationNode::getParentValidationCode))
            .value(jsonPointer + ":" + jsonObject.query(jsonPointer))
            .message(MessageFormat.format("The value for the JSON name value does not pass {0} "
                + "validation [{1}].", jsonPointer, validationNode.getValidationKey()))
            .build());
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
