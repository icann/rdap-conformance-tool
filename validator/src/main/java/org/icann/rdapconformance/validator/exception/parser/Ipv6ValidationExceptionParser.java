package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.customvalidator.Ipv6FormatValidator;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.util.IPv6ValidationUtil;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

/**
 * Exception parser for IPv6 format validation failures.
 *
 * Handles semantic validation errors from Ipv6FormatValidator:
 * - -10201: Not allocated/legacy (valid syntax, invalid allocation)
 * - -10202: Special address space (valid syntax, special purpose)
 * - -10200: Syntax errors (invalid IPv6 notation)
 *
 * Works in conjunction with schema validation which handles
 * pattern-based validation failures.
 */
public class Ipv6ValidationExceptionParser extends StringFormatExceptionParser<Ipv6FormatValidator> {

  protected Ipv6ValidationExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results, Ipv6FormatValidator.class);
  }

  @Override
  protected void doParse() {
    String ipValue = jsonObject.query(e.getPointerToViolation()).toString();

    // First, determine if this is truly a syntax error using IPAddressString library
    if (!IPv6ValidationUtil.isValidIPv6Syntax(ipValue)) {
      // TRUE syntax error - generate syntax error only
      results.add(RDAPValidationResult.builder()
          .code(-10200)
          .value(e.getPointerToViolation() + ":" + ipValue)
          .message("The IPv6 address is not syntactically valid.")
          .build());
      return;
    }

    // IP has valid syntax, check for semantic validation failures
    String errorMessage = e.getMessage();

    if (errorMessage.contains(Ipv6FormatValidator.NOT_ALLOCATED_NOR_LEGACY)) {
      // Valid syntax but not allocated/legacy
      results.add(RDAPValidationResult.builder()
          .code(-10201)
          .value(e.getPointerToViolation() + ":" + ipValue)
          .message(Ipv6FormatValidator.NOT_ALLOCATED_NOR_LEGACY)
          .build());
    } else if (errorMessage.contains(Ipv6FormatValidator.PART_OF_SPECIAL_ADDRESSES)) {
      // Valid syntax but in special address space
      results.add(RDAPValidationResult.builder()
          .code(-10202)
          .value(e.getPointerToViolation() + ":" + ipValue)
          .message(Ipv6FormatValidator.PART_OF_SPECIAL_ADDRESSES)
          .build());
    } else {
      // Unknown format validation failure - default behavior
      // This handles cases where format validation fails for reasons other than allocation/special
      results.add(RDAPValidationResult.builder()
          .code(parseErrorCode(e::getErrorCodeFromViolatedSchema))
          .value(e.getPointerToViolation() + ":" + ipValue)
          .message(e.getMessage("The v6 structure is not syntactically valid."))
          .build());
    }
  }
}
