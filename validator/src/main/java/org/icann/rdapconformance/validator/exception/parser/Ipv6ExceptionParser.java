package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.internal.IPV6Validator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class Ipv6ExceptionParser extends StringFormatExceptionParser<IPV6Validator> {

  protected Ipv6ExceptionParser(ValidationException e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results, IPV6Validator.class);
  }

  @Override
  protected void doParse() {

  }
}
