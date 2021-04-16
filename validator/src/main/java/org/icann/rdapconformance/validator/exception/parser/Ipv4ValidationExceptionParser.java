package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.customvalidator.Ipv4FormatValidator;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class Ipv4ValidationExceptionParser extends StringFormatExceptionParser<Ipv4FormatValidator> {

  protected Ipv4ValidationExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results, Ipv4FormatValidator.class);
  }

  @Override
  protected void doParse() {
    results.add(RDAPValidationResult.builder()
        .code(parseErrorCode(e::getErrorCodeFromViolatedSchema))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message("The v4 structure is not syntactically valid.")
        .build());

    results.add(RDAPValidationResult.builder()
        .code(-10100)
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message("The IPv4 address is not syntactically valid in dot-decimal notation.")
        .build());

    results.add(RDAPValidationResult.builder()
        .code(-11406)
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message("The IPv4 address is not syntactically valid in dot-decimal notation.")
        .build());

    if (e.getMessage().contains(Ipv4FormatValidator.NOT_ALLOCATED_NOR_LEGACY)) {
      results.add(RDAPValidationResult.builder()
          .code(-10101)
          .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
          .message(Ipv4FormatValidator.NOT_ALLOCATED_NOR_LEGACY)
          .build());
    }

    if (e.getMessage().contains(Ipv4FormatValidator.PART_OF_SPECIAL_ADDRESSES)) {
      results.add(RDAPValidationResult.builder()
          .code(-10102)
          .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
          .message(Ipv4FormatValidator.PART_OF_SPECIAL_ADDRESSES)
          .build());
    }
  }

}
