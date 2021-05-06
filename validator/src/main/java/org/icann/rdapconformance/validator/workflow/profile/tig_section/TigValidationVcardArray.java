package org.icann.rdapconformance.validator.workflow.profile.tig_section;

import static com.jayway.jsonpath.JsonPath.using;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Option;
import java.util.List;
import org.icann.rdapconformance.validator.jcard.JcardCategoriesSchemas;
import org.icann.rdapconformance.validator.schema.JsonPointers;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class TigValidationVcardArray extends TigJsonValidation {

  public TigValidationVcardArray(String rdapResponse, RDAPValidatorResults results) {
    super(rdapResponse, results);
  }

  @Override
  protected boolean doValidate() {
    JSONObject jsonObject = new JSONObject(rdapResponse);
    Configuration jsonPathConfig = Configuration.defaultConfiguration()
        .addOptions(Option.AS_PATH_LIST)
        .addOptions(Option.SUPPRESS_EXCEPTIONS);
    DocumentContext jpath = using(jsonPathConfig).parse(jsonObject.toString());
    List<String> vcardArraysPaths = jpath.read("$..entities..vcardArray");
    JcardCategoriesSchemas jcardCategoriesSchemas = new JcardCategoriesSchemas();
    boolean isValid = true;
    for (String vcardArraysPath : vcardArraysPaths) {
      String jsonPointer = JsonPointers.fromJpath(vcardArraysPath);
      JSONArray vcardArray = (JSONArray) jsonObject.query(jsonPointer);
      int vcardElementIndex = 0;
      for (Object vcardElement : vcardArray) {
        if (vcardElement instanceof JSONArray) {
          JSONArray vcardElementArray = (JSONArray) vcardElement;
          int categoryArrayIndex = 0;
          for (Object categoryArray : vcardElementArray) {
            JSONArray categorieJsonArray = ((JSONArray) categoryArray);
            String category = categorieJsonArray.getString(0);
            String jsonExceptionPointer =
                jsonPointer + "/" + vcardElementIndex + "/" + categoryArrayIndex;
            isValid &= validateVcardArray(category, categorieJsonArray, jsonExceptionPointer,
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
