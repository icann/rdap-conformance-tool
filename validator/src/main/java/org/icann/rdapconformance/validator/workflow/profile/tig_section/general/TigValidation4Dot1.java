package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.jcard.JcardCategoriesSchemas;
import org.icann.rdapconformance.validator.workflow.profile.RDAPProfileVcardArrayValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;

public class TigValidation4Dot1 extends RDAPProfileVcardArrayValidation {

  public TigValidation4Dot1(String rdapResponse, RDAPValidatorResults results) {
    super(rdapResponse, results);
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
        results.add(RDAPValidationResult.builder()
            .code(-20800)
            .value(jsonExceptionPointer + ":" + categoryJsonArray)
            .message(
                "An entity with a non-structured address was found. See section 4.1 of the TIG.")
            .build());
        return false;
      }
    }
    return true;
  }
}
