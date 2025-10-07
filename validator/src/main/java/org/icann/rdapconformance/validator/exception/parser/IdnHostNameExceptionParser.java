package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.customvalidator.IdnHostNameFormatValidator;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class IdnHostNameExceptionParser extends StringFormatExceptionParser<IdnHostNameFormatValidator> {

  protected IdnHostNameExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results, IdnHostNameFormatValidator.class);
  }

  @Override
  protected void doParse() {
    if (e.getMessage().contains("LABEL_TOO_LONG")) {
      results.add(RDAPValidationResult.builder()
          .code(parseErrorCode(() -> (int) e.getPropertyFromViolatedSchema("labelTooLong")))
          .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
          .message("A DNS label with length not between 1 and 63 was found.")
          .build());
    }

    if (e.getMessage().contains("DOMAIN_NAME_TOO_LONG")) {
      results.add(RDAPValidationResult.builder()
          .code(parseErrorCode(() -> (int) e.getPropertyFromViolatedSchema("domainTooLong")))
          .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
          .message("A domain name of more than 253 characters was found.")
          .build());
    }

    if (e.getMessage().contains("LESS_THAN_TWO_LABELS")) {
      results.add(RDAPValidationResult.builder()
          .code(parseErrorCode(() -> (int) e.getPropertyFromViolatedSchema("lessThanTwoLabels")))
          .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
          .message("A domain name with less than two labels was found.")
          .build());
    }

    results.add(RDAPValidationResult.builder()
        .code(parseErrorCode(e::getErrorCodeFromViolatedSchema))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message(e.getMessage("A DNS label not being a valid 'A-label', 'U-label', or 'NR-LDH label' was found."))
        .build());
  }
}
