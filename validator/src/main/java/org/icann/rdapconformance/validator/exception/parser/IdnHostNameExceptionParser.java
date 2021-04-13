package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.customvalidator.IdnHostNameValidator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class IdnHostNameExceptionParser extends StringFormatExceptionParser<IdnHostNameValidator> {

  protected IdnHostNameExceptionParser(ValidationException e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results, IdnHostNameValidator.class);
  }

  @Override
  protected void doParse() {
    if (e.getMessage().contains("LABEL_TOO_LONG")) {
      results.add(RDAPValidationResult.builder()
          .code(parseErrorCode(() -> (int) getPropertyFromViolatedSchema(e, "labelTooLong")))
          .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
          .message("A DNS label with length not between 1 and 63 was found.")
          .build());
    }

    if (e.getMessage().contains("DOMAIN_NAME_TOO_LONG")) {
      results.add(RDAPValidationResult.builder()
          .code(parseErrorCode(() -> (int) getPropertyFromViolatedSchema(e, "domainTooLong")))
          .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
          .message("A domain name of more than 253 characters was found.")
          .build());
    }

    if (e.getMessage().contains("LESS_THAN_TWO_LABELS")) {
      results.add(RDAPValidationResult.builder()
          .code(parseErrorCode(() -> (int) getPropertyFromViolatedSchema(e, "lessThanTwoLabels")))
          .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
          .message("A domain name with less than two labels was found. See "
              + "RDAP_Technical_Implementation_Guide_2_1 section 1.10.")
          .build());
    }

    results.add(RDAPValidationResult.builder()
        .code(parseErrorCode(() -> getErrorCodeFromViolatedSchema(e)))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message(("A label not being a valid \"U-label\"/\"A-label\" or \"NR-LDH label\" was "
            + "found. " + e.getMessage().replace(e.getPointerToViolation(), "Reasons")))
        .build());
  }
}
