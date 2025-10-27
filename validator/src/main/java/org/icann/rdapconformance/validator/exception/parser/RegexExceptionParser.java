package org.icann.rdapconformance.validator.exception.parser;

import java.util.regex.Pattern;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.util.IPv4ValidationUtil;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class RegexExceptionParser extends ExceptionParser {

  static Pattern regexPattern = Pattern.compile("string (.+) does not match pattern (.+)");

  protected RegexExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
  }

  protected RegexExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results,
      org.icann.rdapconformance.validator.QueryContext queryContext) {
    super(e, schema, jsonObject, results, queryContext);
  }

  @Override
  public boolean matches(ValidationExceptionNode e) {
    if (e.getViolatedSchema() instanceof StringSchema) {
      return ((StringSchema) e.getViolatedSchema()).getPattern() != null
          && regexPattern.matcher(e.getMessage()).find();
    }
    return false;
  }

  @Override
  protected void doParse() {
    int errorCode = parseErrorCode(e::getErrorCodeFromViolatedSchema);
    String ipValue = jsonObject.query(e.getPointerToViolation()).toString();


    // Special handling for IPv4 pattern validation errors
    if (errorCode == -11406 && isIPv4PatternError()) {
      if (!IPv4ValidationUtil.isValidIPv4Syntax(ipValue)) {
        // TRUE syntax error - convert IPv4 pattern failure (-11406) to syntax error (-10100)
        RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
            .code(-10100)
            .value(e.getPointerToViolation() + ":" + ipValue)
            .message("The IPv4 address is not syntactically valid in dot-decimal notation.");

        if (queryContext != null) {
          results.add(builder.build(queryContext));
        } else {
          results.add(builder.build());
        }
        return;
      } else {
        // Edge case: IP passes IPAddressString validation but fails regex pattern
        // This shouldn't happen with current regex, but handle gracefully
        RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
            .code(-11406)
            .value(e.getPointerToViolation() + ":" + ipValue)
            .message("The IPv4 address is not syntactically valid in dot-decimal notation.");

        if (queryContext != null) {
          results.add(builder.build(queryContext));
        } else {
          results.add(builder.build());
        }
        return;
      }
    }

    // Default behavior for all other regex patterns
    RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
        .code(errorCode)
        .value(e.getPointerToViolation() + ":" + ipValue)
        .message(e.getMessage("The value of the JSON string data in the " + e.getPointerToViolation()
            + " does not conform to "
            + e.getSchemaLocation() + " syntax."));

    if (queryContext != null) {
      results.add(builder.build(queryContext));
    } else {
      results.add(builder.build());
    }
  }

  /**
   * Checks if this is an IPv4 pattern validation error by examining both the pattern
   * and using Sean Foley's library to verify the value is IPv4-related.
   */
  private boolean isIPv4PatternError() {
    if (e.getViolatedSchema() instanceof StringSchema) {
      StringSchema stringSchema = (StringSchema) e.getViolatedSchema();
      if (stringSchema.getPattern() != null) {
        String pattern = stringSchema.getPattern().pattern();
        boolean isIPv4Pattern = CommonUtils.IPV4_DOT_DECIMAL_PATTERN.equals(pattern);

        if (isIPv4Pattern) {
          // Also check if the actual value is IPv4-related using Sean Foley's library
          String ipValue = jsonObject.query(e.getPointerToViolation()).toString();
          return looksLikeIPv4Attempt(ipValue);
        }
      }
    }
    return false;
  }

  /**
   * Determines if a string looks like an IPv4 address attempt using Sean Foley's library.
   */
  private boolean looksLikeIPv4Attempt(String value) {
    if (value == null || value.trim().isEmpty()) {
      return false;
    }

    try {
      inet.ipaddr.IPAddressString ipAddressString = new inet.ipaddr.IPAddressString(value);
      ipAddressString.getAddress();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

}
