package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static com.jayway.jsonpath.JsonPath.using;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Option;
import java.util.List;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.jcard.JcardCategoriesSchemas;
import org.icann.rdapconformance.validator.schema.JsonPointers;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.TigValidation;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.TigValidationVcardArray;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public class Validation4Dot1 extends TigValidationVcardArray {

  public Validation4Dot1(String rdapResponse, RDAPValidatorResults results) {
    super(rdapResponse, results);
  }

  @Override
  protected String getGroupName() {
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
