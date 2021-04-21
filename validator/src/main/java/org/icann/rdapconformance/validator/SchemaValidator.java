package org.icann.rdapconformance.validator;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.icann.rdapconformance.validator.customvalidator.DatasetValidator;
import org.icann.rdapconformance.validator.customvalidator.HostNameInUriFormatValidator;
import org.icann.rdapconformance.validator.customvalidator.IdnHostNameFormatValidator;
import org.icann.rdapconformance.validator.customvalidator.Ipv4FormatValidator;
import org.icann.rdapconformance.validator.customvalidator.Ipv6FormatValidator;
import org.icann.rdapconformance.validator.customvalidator.RdapExtensionsFormatValidator;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.exception.parser.ExceptionParser;
import org.icann.rdapconformance.validator.schema.SchemaNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EventActionJsonValues;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv4AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv6AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.LinkRelations;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.MediaTypes;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.NoticeAndRemarkJsonValues;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPExtensions;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv4Addresses;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv6Addresses;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.StatusJsonValues;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaValidator {

  private static final Logger logger = LoggerFactory.getLogger(SchemaValidator.class);
  static Pattern duplicateKeys = Pattern.compile("Duplicate key \"(.+)\" at");
  private final RDAPDatasetService datasetService;
  private JSONObject schemaObject;
  private Schema schema;
  private RDAPValidatorResults results;
  private SchemaNode schemaRootNode;

  public SchemaValidator(String schemaName, RDAPValidatorResults results,
      RDAPDatasetService datasetService) {
    this.datasetService = datasetService;
    this.init(getSchema(schemaName, "json-schema/", getClass().getClassLoader(), datasetService), results);
  }

  private void init(Schema schema, RDAPValidatorResults results) {
    this.schema = schema;
    this.schemaRootNode = SchemaNode.create(null, this.schema);
    this.schemaObject = new JSONObject(schema.toString());
    this.results = results;
  }

  public static Schema getSchema(
      String name,
      String scope,
      ClassLoader classLoader,
      RDAPDatasetService ds) {
    Ipv4FormatValidator ipv4FormatValidator = new Ipv4FormatValidator(ds.get(Ipv4AddressSpace.class),
        ds.get(SpecialIPv4Addresses.class));
    Ipv6FormatValidator ipv6FormatValidator = new Ipv6FormatValidator(ds.get(Ipv6AddressSpace.class),
        ds.get(SpecialIPv6Addresses.class));

    RdapExtensionsFormatValidator rdapExtensionsFormatValidator =
        new RdapExtensionsFormatValidator(ds.get(RDAPExtensions.class));
    JSONObject jsonSchema = new JSONObject(
        new JSONTokener(
            Objects.requireNonNull(
                classLoader.getResourceAsStream(scope + name))));
    SchemaLoader schemaLoader = SchemaLoader.builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .schemaJson(jsonSchema)
        .resolutionScope("classpath://" + scope)
        .addFormatValidator(new IdnHostNameFormatValidator())
        .addFormatValidator(
            new HostNameInUriFormatValidator(ipv4FormatValidator, ipv6FormatValidator))
        .addFormatValidator(ipv4FormatValidator)
        .addFormatValidator(ipv6FormatValidator)
        .addFormatValidator(rdapExtensionsFormatValidator)
        .addFormatValidator(
            new DatasetValidator(ds.get(LinkRelations.class), "linkRelations"))
        .addFormatValidator(
            new DatasetValidator(ds.get(MediaTypes.class), "mediaTypes"))
        .addFormatValidator(
            new DatasetValidator(ds.get(NoticeAndRemarkJsonValues.class), "notice-and-remark"))
        .addFormatValidator(
            new DatasetValidator(ds.get(EventActionJsonValues.class), "event-action"))
        .addFormatValidator(
            new DatasetValidator(ds.get(StatusJsonValues.class), "status"))
        .draftV7Support()
        .build();
    return schemaLoader.load().build();
  }

  public boolean validate(String content) {
    JSONObject jsonObject;
    try {
      jsonObject = new JSONObject(content);
    } catch (JSONException e) {
      RDAPValidationResult result = parseJsonException(e, content);
      results.add(result);
      return false;
    }

    try {
      schema.validate(jsonObject);
    } catch (ValidationException e) {
      parseException(e, jsonObject);
      return false;
    }
    return true;
  }

  private RDAPValidationResult parseJsonException(JSONException e, String content) {
    Matcher duplicateKeysMatcher = duplicateKeys.matcher(e.getMessage());
    if (duplicateKeysMatcher.find()) {
      String key = duplicateKeysMatcher.group(1);
      Matcher valueMatcher = Pattern.compile(key + "\":\\s*\"(.*?)\",").matcher(content);
      String value = "...";
      if (valueMatcher.find()) {
        value = valueMatcher.group(1).trim();
      }

      // TODO: reinsert jackson for duplicate handling: without the json pointer where the element
      // is duplicated, we can fall on the wrong duplicateKeys error code.
      return RDAPValidationResult.builder()
          .code(schemaRootNode.searchBottomMostErrorCode(key, "duplicateKeys"))
          .value(key + ":" + value)
          .message("The name in the name/value pair of a link structure was found more than once.")
          .build();
    }

    return RDAPValidationResult.builder()
        .code(getErrorCode("structureInvalid"))
        .value(content)
        .message("The " + schema.getTitle() + " structure is not syntactically valid.")
        .build();
  }

  private void parseException(ValidationException e, JSONObject jsonObject) {
    List<ExceptionParser> exceptionParsers = ExceptionParser.createParsers(e, schema, jsonObject,
        results);
    for (ExceptionParser exceptionParser : exceptionParsers) {
      exceptionParser.parse();
    }

    List<ValidationExceptionNode> validationExceptions =
        new ValidationExceptionNode(null, e).getAllExceptions();
    for (ValidationExceptionNode validationException : validationExceptions) {
      if (exceptionParsers.stream()
          .noneMatch(exceptionParser -> exceptionParser.matches(validationException))) {
        logger.error(
            "We found this error with no exception parser {}", validationException.getMessage());
      }
    }
  }

  private int getErrorCode(String validationName) {
    return (int) schemaObject.get(validationName);
  }

  public Schema getSchema() {
    return schema;
  }
}
