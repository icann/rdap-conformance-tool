package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.customvalidator.RdapExtensionsFormatValidator;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class RdapExtensionsExceptionParser extends
    StringFormatExceptionParser<RdapExtensionsFormatValidator> {


  protected RdapExtensionsExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results,
      org.icann.rdapconformance.validator.QueryContext queryContext) {
    super(e, schema, jsonObject, results, RdapExtensionsFormatValidator.class, queryContext);
  }

  @Override
  protected void doParse() {
    RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
        .code(parseErrorCode(e::getErrorCodeFromViolatedSchema))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message("The JSON string is not included as an Extension Identifier in RDAPExtensions.");

    results.add(builder.build(queryContext));
  }
}
