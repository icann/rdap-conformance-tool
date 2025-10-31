package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.jcard.JcardCategoriesSchemas;
import org.icann.rdapconformance.validator.workflow.profile.RDAPProfileVcardArrayValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;

public class TigValidation4Dot1 extends RDAPProfileVcardArrayValidation {

  private final QueryContext queryContext;


  public TigValidation4Dot1(QueryContext queryContext) {
    super(queryContext.getRdapResponseData(), queryContext.getResults(), queryContext);
    this.queryContext = queryContext;
  }

  /**
   * @deprecated Use TigValidation4Dot1(QueryContext) instead
   * TODO: Migrate tests to QueryContext-only constructor
   */
  @Deprecated
  public TigValidation4Dot1(String rdapResponse, RDAPValidatorResults results) {
    super(rdapResponse, results);
    this.queryContext = null; // For testing purposes only
  }

  /**
   * @deprecated Use TigValidation4Dot1(QueryContext) instead
   * TODO: Migrate tests to QueryContext-only constructor
   */
  @Deprecated
  public TigValidation4Dot1(String rdapResponse, RDAPValidatorResults results, QueryContext queryContext) {
    super(rdapResponse, results, queryContext);
    this.queryContext = queryContext;
  }

  @Override
  public String getGroupName() {
    return "tigSection_4_1_Validation";
  }

  @Override
  protected boolean validateVcardArray(String category, JSONArray categoryJsonArray,
      String jsonExceptionPointer, JcardCategoriesSchemas jcardCategoriesSchemas) {
    if (category.equals("adr")) {
      try {
        jcardCategoriesSchemas.getCategory(category).validate(categoryJsonArray);
      } catch (ValidationException e) {
        RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
            .code(-20800)
            .value(jsonExceptionPointer + ":" + categoryJsonArray)
            .message(
                "An entity with a non-structured address was found. See section 4.1 of the TIG.");
        results.add(builder.build(queryContext));
        return false;
      }
    }
    return true;
  }
}
