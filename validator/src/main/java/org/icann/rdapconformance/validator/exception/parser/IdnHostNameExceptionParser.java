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

  protected IdnHostNameExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results,
      org.icann.rdapconformance.validator.QueryContext queryContext) {
    super(e, schema, jsonObject, results, IdnHostNameFormatValidator.class, queryContext);
  }

  @Override
  protected void doParse() {
    if (e.getMessage().contains("LABEL_TOO_LONG")) {
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
          .code(parseErrorCode(() -> (int) e.getPropertyFromViolatedSchema("labelTooLong")))
          .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
          .message("A DNS label with length not between 1 and 63 was found.");

      if (queryContext != null) {
        results.add(builder.build(queryContext));
      } else {
        results.add(builder.build());
      }
    }

    if (e.getMessage().contains("DOMAIN_NAME_TOO_LONG")) {
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
          .code(parseErrorCode(() -> (int) e.getPropertyFromViolatedSchema("domainTooLong")))
          .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
          .message("A domain name of more than 253 characters was found.");

      if (queryContext != null) {
        results.add(builder.build(queryContext));
      } else {
        results.add(builder.build());
      }
    }

    if (e.getMessage().contains("LESS_THAN_TWO_LABELS")) {
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
          .code(parseErrorCode(() -> (int) e.getPropertyFromViolatedSchema("lessThanTwoLabels")))
          .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
          .message("A domain name with less than two labels was found.");

      if (queryContext != null) {
        results.add(builder.build(queryContext));
      } else {
        results.add(builder.build());
      }
    }

    RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
        .code(parseErrorCode(e::getErrorCodeFromViolatedSchema))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message(e.getMessage("A DNS label not being a valid 'A-label', 'U-label', or 'NR-LDH label' was found."));

    if (queryContext != null) {
      results.add(builder.build(queryContext));
    } else {
      results.add(builder.build());
    }
  }
}
