package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.internal.IPV4Validator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class Ipv4ExceptionParser extends StringFormatExceptionParser<IPV4Validator> {

  protected Ipv4ExceptionParser(ValidationException e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results, IPV4Validator.class);
  }

  @Override
  public boolean matches(ValidationException e) {
    return e.getViolatedSchema() instanceof StringSchema &&
        ((StringSchema) e.getViolatedSchema())
            .getFormatValidator() instanceof IPV4Validator;
  }

  @Override
  protected void doParse() {
    StringSchema stringSchema = (StringSchema) e.getViolatedSchema();
  }
}
