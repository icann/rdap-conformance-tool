package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.customvalidator.Ipv6FormatValidator;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class Ipv6ValidationExceptionParser extends StringFormatExceptionParser<Ipv6FormatValidator> {

  protected Ipv6ValidationExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results, Ipv6FormatValidator.class);
  }

  @Override
  protected void doParse() {
    results.add(RDAPValidationResult.builder()
        .code(parseErrorCode(e::getErrorCodeFromViolatedSchema))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message("The v6 structure is not syntactically valid.")
        .build());

    results.add(RDAPValidationResult.builder()
        .code(-11409)
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message("The IPv6 address is not syntactically valid.")
        .build());

    results.add(RDAPValidationResult.builder()
        .code(-10200)
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message("The IPv6 address is not syntactically valid.")
        .build());

    if (e.getMessage().contains(Ipv6FormatValidator.NOT_ALLOCATED_NOR_LEGACY)) {
      results.add(RDAPValidationResult.builder()
          .code(-10201)
          .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
          .message(Ipv6FormatValidator.NOT_ALLOCATED_NOR_LEGACY)
          .build());
    }

    if (e.getMessage().contains(Ipv6FormatValidator.PART_OF_SPECIAL_ADDRESSES)) {
      results.add(RDAPValidationResult.builder()
          .code(-10202)
          .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
          .message(Ipv6FormatValidator.PART_OF_SPECIAL_ADDRESSES)
          .build());
    }
  }
}
