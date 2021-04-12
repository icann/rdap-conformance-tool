package org.icann.rdapconformance.validator.exception.parser;

import java.util.regex.Pattern;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.internal.IPV4Validator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class Ipv4ExceptionParser extends StringFormatExceptionParser<IPV4Validator> {

  protected Ipv4ExceptionParser(ValidationException e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results, IPV4Validator.class);
  }

  @Override
  protected void doParse() {
    results.add(RDAPValidationResult.builder()
        .code(parseErrorCode(() -> getErrorCodeFromViolatedSchema(e)))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message("The v4 structure is not syntactically valid.")
        .build());

    results.add(RDAPValidationResult.builder()
        .code(-11406)
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message("The IPv4 address is not syntactically valid in dot-decimal notation.")
        .build());
  }
}
