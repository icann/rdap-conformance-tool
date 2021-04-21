package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.customvalidator.DatasetValidator;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class DatasetExceptionParser extends StringFormatExceptionParser<DatasetValidator> {

  protected DatasetExceptionParser(
      ValidationExceptionNode e,
      Schema schema, JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results, DatasetValidator.class);
  }
}
