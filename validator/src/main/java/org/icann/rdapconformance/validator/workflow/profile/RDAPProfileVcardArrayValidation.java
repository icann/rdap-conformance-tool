package org.icann.rdapconformance.validator.workflow.profile;

import java.util.Set;
import org.icann.rdapconformance.validator.jcard.JcardCategoriesSchemas;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;

public abstract class RDAPProfileVcardArrayValidation extends ProfileJsonValidation {

  public RDAPProfileVcardArrayValidation(String rdapResponse, RDAPValidatorResults results) {
    super(rdapResponse, results);
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
