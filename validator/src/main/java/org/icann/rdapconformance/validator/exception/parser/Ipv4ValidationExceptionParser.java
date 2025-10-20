package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.customvalidator.Ipv4FormatValidator;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.util.IPv4ValidationUtil;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

/**
 * Exception parser for IPv4 format validation failures.
 *
 * Handles semantic validation errors from Ipv4FormatValidator:
 * - -10101: Not allocated/legacy (valid syntax, invalid allocation)
 * - -10102: Special address space (valid syntax, special purpose)
 * - -10100: Syntax errors (invalid dot-decimal notation)
 *
 * Works in conjunction with Ipv4PatternExceptionParser which handles
 * pattern-based validation failures.
 */

public class Ipv4ValidationExceptionParser extends StringFormatExceptionParser<Ipv4FormatValidator> {

  protected Ipv4ValidationExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results, Ipv4FormatValidator.class);
  }

  @Override
  protected void doParse() {
    String ipValue = jsonObject.query(e.getPointerToViolation()).toString();


    // First, determine if this is truly a syntax error using IPAddressString library
    if (!IPv4ValidationUtil.isValidIPv4Syntax(ipValue)) {
      // TRUE syntax error - generate syntax error only
      results.add(RDAPValidationResult.builder()
          .code(-10100)
          .value(e.getPointerToViolation() + ":" + ipValue)
          .message("The IPv4 address is not syntactically valid in dot-decimal notation.")
          .build());
      return;
    }

    // IP has valid syntax, check for semantic validation failures
    String errorMessage = e.getMessage();

    if (errorMessage.contains(Ipv4FormatValidator.NOT_ALLOCATED_NOR_LEGACY)) {
      // Valid syntax but not allocated/legacy
      results.add(RDAPValidationResult.builder()
          .code(-10101)
          .value(e.getPointerToViolation() + ":" + ipValue)
          .message(Ipv4FormatValidator.NOT_ALLOCATED_NOR_LEGACY)
          .build());
    } else if (errorMessage.contains(Ipv4FormatValidator.PART_OF_SPECIAL_ADDRESSES)) {
      // Valid syntax but in special address space
      results.add(RDAPValidationResult.builder()
          .code(-10102)
          .value(e.getPointerToViolation() + ":" + ipValue)
          .message(Ipv4FormatValidator.PART_OF_SPECIAL_ADDRESSES)
          .build());
    } else {
      // Unknown format validation failure - default behavior
      // This handles cases where format validation fails for reasons other than allocation/special
      results.add(RDAPValidationResult.builder()
          .code(parseErrorCode(e::getErrorCodeFromViolatedSchema))
          .value(e.getPointerToViolation() + ":" + ipValue)
          .message(e.getMessage("The v4 structure is not syntactically valid."))
          .build());
    }
  }


}
