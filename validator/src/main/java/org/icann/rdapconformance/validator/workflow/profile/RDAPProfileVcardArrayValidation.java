package org.icann.rdapconformance.validator.workflow.profile;

import static com.jayway.jsonpath.JsonPath.using;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Option;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.icann.rdapconformance.validator.jcard.JcardCategoriesSchemas;
import org.icann.rdapconformance.validator.schema.JsonPointers;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;

public abstract class RDAPProfileVcardArrayValidation extends ProfileJsonValidation {

  public RDAPProfileVcardArrayValidation(String rdapResponse, RDAPValidatorResults results) {
    super(rdapResponse, results);
  }

  @Override
  protected boolean doValidate() {
    Configuration jsonPathConfig = Configuration.defaultConfiguration()
        .addOptions(Option.AS_PATH_LIST)
        .addOptions(Option.SUPPRESS_EXCEPTIONS);
    DocumentContext jpath = using(jsonPathConfig).parse(jsonObject.toString());
    Set<String> vcardArraysPaths = new HashSet<>(jpath.read("$..entities..vcardArray"));
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
