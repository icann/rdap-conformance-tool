package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.customvalidator.LinkRelationsValidator;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class LinkRelationsExceptionParser extends
    StringFormatExceptionParser<LinkRelationsValidator> {

  protected LinkRelationsExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results, LinkRelationsValidator.class);
  }

  @Override
  protected void doParse() {
    results.add(RDAPValidationResult.builder()
        .code(parseErrorCode(e::getErrorCodeFromViolatedSchema))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message(e.getMessage("The JSON value is not included as a Relation Name in linkRelations"
            + "."))
        .build());
  }
}
