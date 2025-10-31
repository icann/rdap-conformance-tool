package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.customvalidator.HostNameInUriFormatValidator;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class HostNameInUriExceptionParser extends StringFormatExceptionParser<HostNameInUriFormatValidator> {

  private final IdnHostNameExceptionParser idnHostNameExceptionParser;


  protected HostNameInUriExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results,
      org.icann.rdapconformance.validator.QueryContext queryContext) {
    super(e, schema, jsonObject, results, HostNameInUriFormatValidator.class, queryContext);
    idnHostNameExceptionParser = new IdnHostNameExceptionParser(e,
        schema, jsonObject, results, queryContext);
  }

  @Override
  protected void doParse() {
    idnHostNameExceptionParser.doParse();
  }
}
