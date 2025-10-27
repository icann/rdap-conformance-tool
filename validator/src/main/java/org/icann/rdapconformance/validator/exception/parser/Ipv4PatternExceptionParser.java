package org.icann.rdapconformance.validator.exception.parser;

import java.util.regex.Pattern;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.util.IPv4ValidationUtil;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

/**
 * Exception parser specifically for IPv4 pattern validation failures.
 *
 * This parser handles the case where IPv4 addresses fail the regex pattern validation
 * (error code -11406) but need to be converted to the proper -10100 syntax error.
 */
public class Ipv4PatternExceptionParser extends ExceptionParser {

  static Pattern regexPattern = Pattern.compile("string (.+) does not match pattern (.+)");

  protected Ipv4PatternExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
  }

  protected Ipv4PatternExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results,
      org.icann.rdapconformance.validator.QueryContext queryContext) {
    super(e, schema, jsonObject, results, queryContext);
  }

  @Override
  public boolean matches(ValidationExceptionNode e) {
    if (e.getViolatedSchema() instanceof StringSchema) {
      StringSchema stringSchema = (StringSchema) e.getViolatedSchema();

      // Check if pattern exists
      if (stringSchema.getPattern() == null) {
        return false;
      }

      String pattern = stringSchema.getPattern().pattern();
      int errorCode = parseErrorCode(e::getErrorCodeFromViolatedSchema);
      boolean messageMatches = regexPattern.matcher(e.getMessage()).find();

      // Check if this is IPv4-related by using the pattern comparison AND checking if the actual value looks like an IPv4 attempt
      boolean isIPv4Pattern = CommonUtils.IPV4_DOT_DECIMAL_PATTERN.equals(pattern);
      boolean isIPv4Attempt = false;

      if (isIPv4Pattern && errorCode == -11406 && messageMatches) {
        // Extract the actual value to check if it's an IPv4 attempt
        String ipValue = jsonObject.query(e.getPointerToViolation()).toString();
        // Use Sean Foley's library to determine if this is an IPv4-related validation
        // This includes both valid and invalid IPv4 attempts
        isIPv4Attempt = looksLikeIPv4Attempt(ipValue);
      }

      return isIPv4Pattern && errorCode == -11406 && messageMatches && isIPv4Attempt;
    }
    return false;
  }

  /**
   * Determines if a string looks like an IPv4 address attempt using Sean Foley's library.
   * This relies entirely on the IPAddressString library to determine IPv4 relevance.
   */
  private boolean looksLikeIPv4Attempt(String value) {
    if (value == null || value.trim().isEmpty()) {
      return false;
    }

    try {
      // Use Sean Foley's library to parse the string
      // If it can be parsed as any kind of IP address attempt, we consider it IPv4-related
      inet.ipaddr.IPAddressString ipAddressString = new inet.ipaddr.IPAddressString(value);

      // Try to get the address - if this doesn't throw an exception, it's some kind of IP attempt
      // This includes both valid IPs and invalid ones that the library can still interpret
      ipAddressString.getAddress();
      return true;

    } catch (Exception e) {
      // If Sean Foley's library can't parse it at all, it's not an IPv4 attempt
      return false;
    }
  }

  @Override
  protected void doParse() {
    String ipValue = jsonObject.query(e.getPointerToViolation()).toString();

    // Use IPAddressString library to determine if this is actually a syntax error
    if (!IPv4ValidationUtil.isValidIPv4Syntax(ipValue)) {
      // TRUE syntax error - convert -11406 pattern failure to -10100
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
          .code(-10100)
          .value(e.getPointerToViolation() + ":" + ipValue)
          .message("The IPv4 address is not syntactically valid in dot-decimal notation.");

      if (queryContext != null) {
        results.add(builder.build(queryContext));
      } else {
        results.add(builder.build());
      }
    } else {
      // Edge case: IP passes IPAddressString validation but fails regex pattern
      // This shouldn't happen with current regex, but handle gracefully
      // Keep the original pattern error code
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
          .code(-11406)
          .value(e.getPointerToViolation() + ":" + ipValue)
          .message("The IPv4 address is not syntactically valid in dot-decimal notation.");

      if (queryContext != null) {
        results.add(builder.build(queryContext));
      } else {
        results.add(builder.build());
      }
    }
  }

}