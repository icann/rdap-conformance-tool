package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static com.jayway.jsonpath.JsonPath.using;

import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.jcard.JcardCategoriesSchemas;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.TigValidationVcardArray;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;

public class TigValidation4Dot1 extends TigValidationVcardArray {

  public TigValidation4Dot1(String rdapResponse, RDAPValidatorResults results) {
    super(rdapResponse, results);
  }

  @Override
  public String getGroupName() {
    return "tigSection_4_1_Validation";
  }

  @Override
  protected boolean validateVcardArray(String category, JSONArray categorieJsonArray,
      String jsonExceptionPointer, JcardCategoriesSchemas jcardCategoriesSchemas) {
    if (jcardCategoriesSchemas.hasCategory(category)) {
      try {
        jcardCategoriesSchemas.getCategory(category).validate(categorieJsonArray);
      } catch (ValidationException e) {
        if (category.equals("adr")) {
          results.add(RDAPValidationResult.builder()
              .code(-20800)
              .value(jsonExceptionPointer + ":" + categorieJsonArray)
              .message(
                  "An entity with a non-structured address was found. See section 4.1 of the TIG.")
              .build());
          return false;
        }
      }
    }
    return true;
  }
}
