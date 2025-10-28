package org.icann.rdapconformance.validator.jcard;

import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.RDAPProfileVcardArrayValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;

public class VcardArrayGeneralValidation extends RDAPProfileVcardArrayValidation {

  private final QueryContext queryContext;


  public VcardArrayGeneralValidation(String rdapResponse, RDAPValidatorResults results,
      QueryContext queryContext) {
    super(rdapResponse, results, queryContext);
    this.queryContext = queryContext;
  }

  @Override
  public String getGroupName() {
    return "stdRdapEntityLookupValidation";
  }

  @Override
  protected boolean validateVcardArray(String category, JSONArray categoryJsonArray,
      String jsonExceptionPointer, JcardCategoriesSchemas jcardCategoriesSchemas) {
    if (jcardCategoriesSchemas.hasCategory(category)) {
      try {
        jcardCategoriesSchemas.getCategory(category).validate(categoryJsonArray);
      } catch (ValidationException e) {
        RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
            .code(-12305)
            .value(jsonExceptionPointer + ":" + categoryJsonArray)
            .message(
                "The value for the JSON name value is not a syntactically valid vcardArray.");
        if (queryContext != null) {
          results.add(builder.build(queryContext));
        } else {
          results.add(builder.build());
        }
        return false;
      }
    } else {
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
          .code(-12305)
          .value(jsonExceptionPointer + ":" + category)
          .message("unknown vcard category: \"" + category + "\".");
      if (queryContext != null) {
        results.add(builder.build(queryContext));
      } else {
        results.add(builder.build());
      }
      return false;
    }
    return true;
  }
}
