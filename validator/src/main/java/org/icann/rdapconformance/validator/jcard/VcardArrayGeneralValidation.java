package org.icann.rdapconformance.validator.jcard;

import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.TigValidationVcardArray;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;

public class VcardArrayGeneralValidation extends TigValidationVcardArray {

  public VcardArrayGeneralValidation(String rdapResponse, RDAPValidatorResults results) {
    super(rdapResponse, results);
  }

  @Override
  public String getGroupName() {
    return "stdRdapEntityLookupValidation";
  }

  @Override
  protected boolean validateVcardArray(String category, JSONArray categorieJsonArray,
      String jsonExceptionPointer, JcardCategoriesSchemas jcardCategoriesSchemas) {
    if (jcardCategoriesSchemas.hasCategory(category)) {
      try {
        jcardCategoriesSchemas.getCategory(category).validate(categorieJsonArray);
      } catch (ValidationException e) {
        results.add(RDAPValidationResult.builder()
            .code(-12305)
            .value(jsonExceptionPointer + ":" + categorieJsonArray)
            .message(
                "The value for the JSON name value is not a syntactically valid vcardArray.")
            .build());
        return false;
      }
    } else {
      results.add(RDAPValidationResult.builder()
          .code(-12305)
          .value(jsonExceptionPointer + ":" + category)
          .message("unknown vcard category: \"" + category + "\".")
          .build());
      return false;
    }
    return true;
  }
}
