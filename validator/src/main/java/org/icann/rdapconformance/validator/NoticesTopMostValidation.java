package org.icann.rdapconformance.validator;

import static org.icann.rdapconformance.validator.exception.parser.ExceptionParser.parseErrorCode;

import java.util.Set;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.schema.SchemaNode;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class NoticesTopMostValidation extends ProfileJsonValidation {

  private final SchemaNode schemaNode;
  private final QueryContext queryContext;

  public NoticesTopMostValidation(String rdapResponse,
                                  RDAPValidatorResults results, SchemaNode schemaNode, QueryContext queryContext) {
    super(rdapResponse, results);
    this.schemaNode = schemaNode;
    this.queryContext = queryContext;
  }

  @Override
  public String getGroupName() {
    return (String) schemaNode.getErrorKey("validationName");
  }

  @Override
  protected boolean doValidate() {
    Set<String> noticesNotInTopMost = getPointerFromJPath("$.*..notices");
    for (String jsonPointer : noticesNotInTopMost) {
      results.add(RDAPValidationResult.builder()
          .code(parseErrorCode(() -> schemaNode.getErrorCode("noticesTopMost")))
          .value(getResultValue(jsonPointer))
          .message(
              "The value for the JSON name notices exists but " + schemaNode.getSchema().getTitle()
                  + " object is not the topmost JSON object.")
          .build(queryContext));
      return false;
    }
    return true;
  }
}
