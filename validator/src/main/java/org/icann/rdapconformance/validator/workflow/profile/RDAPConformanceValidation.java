package org.icann.rdapconformance.validator.workflow.profile;

import java.util.HashSet;
import java.util.Set;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RDAPConformanceValidation extends ProfileJsonValidation {

  private static final Logger logger = LoggerFactory.getLogger(RDAPConformanceValidation.class);

  final int code;
  final String message;
  private final String requiredValue;
  private final QueryContext queryContext;

  // QueryContext constructor for production use
  public RDAPConformanceValidation(QueryContext queryContext, String requiredValue, Integer code, String message) {
    super(queryContext.getRdapResponseData(), queryContext.getResults());
    this.queryContext = queryContext;
    this.requiredValue = requiredValue;
    this.code = code;
    this.message = message;
  }

  // Deprecated constructor for testing
  @Deprecated
  public RDAPConformanceValidation(String rdapResponse, RDAPValidatorResults results,
                                   String requiredValue, Integer code, String message) {
    super(rdapResponse, results);
    this.queryContext = null; // Not available in deprecated constructor
    this.requiredValue = requiredValue;
    this.code = code;
    this.message = message;
  }

  @Override
  protected boolean doValidate() {
    String jsonPointer = "#/rdapConformance";
    JSONArray rdapConformance = (JSONArray) new JSONObject(jsonObject.toString())
        .query(jsonPointer);

    if (rdapConformance == null) {
      logger.info("The rdapConformance array is missing or null.");
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
                                      .code(code)
                                      .value(getResultValue(jsonPointer))
                                      .message(message); // Always use the passed-in message

      results.add(builder.build(queryContext));
      return false;
    }

    Set<String> values = new HashSet<>();
    rdapConformance.forEach(v -> values.add(v.toString()));

    if (!values.contains(requiredValue)) {
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
                                      .code(code)
                                      .value(getResultValue(jsonPointer))
                                      .message(message); // Always use the passed-in message

      results.add(builder.build(queryContext));
      return false;
    }

    return true;
  }

}
