package org.icann.rdapconformance.validator.workflow.profile;

import java.util.Set;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.jcard.JcardCategoriesSchemas;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;

public abstract class RDAPProfileVcardArrayValidation extends ProfileJsonValidation {

  public RDAPProfileVcardArrayValidation(String rdapResponse, RDAPValidatorResults results, RDAPValidatorConfiguration config) {
    super(rdapResponse, results, config);
  }

  @Override
  protected boolean doValidate() {
    Set<String> pointersFromJPath = getPointerFromJPath("$..entities..vcardArray");
    JcardCategoriesSchemas jcardCategoriesSchemas = new JcardCategoriesSchemas();
    boolean isValid = true;
    for (String jsonPointer : pointersFromJPath) {
      JSONArray vcardArray = (JSONArray) jsonObject.query(jsonPointer);
      int vcardElementIndex = 0;
      for (Object vcardElement : vcardArray) {
        if (vcardElement instanceof JSONArray) {
          try {
            JSONArray vcardElementArray = (JSONArray) vcardElement;
            int categoryArrayIndex = 0;
            for (Object categoryArray : vcardElementArray) {
              JSONArray categoryJsonArray = ((JSONArray) categoryArray);
              String category = categoryJsonArray.getString(0);
              String jsonExceptionPointer =
                  jsonPointer + "/" + vcardElementIndex + "/" + categoryArrayIndex;
              isValid &= validateVcardArray(category, categoryJsonArray, jsonExceptionPointer,
                  jcardCategoriesSchemas);
              categoryArrayIndex++;
            }
          } catch (Exception e) {
            results.add(RDAPValidationResult.builder()
                .code(-12305)
                .value(getResultValue(jsonPointer))
                .message(
                    "The value for the JSON name value is not a syntactically valid vcardArray.")
                .build());
          }
        }
        vcardElementIndex++;
      }
    }
    return isValid;
  }

  protected abstract boolean validateVcardArray(
      String category,
      JSONArray categorieJsonArray,
      String jsonExceptionPointer,
      JcardCategoriesSchemas jcardCategoriesSchemas);
}
