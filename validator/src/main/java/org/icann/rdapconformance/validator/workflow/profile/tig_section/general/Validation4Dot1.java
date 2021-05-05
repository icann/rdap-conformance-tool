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
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public class Validation4Dot1 extends TigValidation {

  private final JSONObject jsonObject;

  public Validation4Dot1(String rdapResponse, RDAPValidatorResults results) {
    super(results);
    jsonObject = new JSONObject(rdapResponse);
  }

  @Override
  protected String getGroupName() {
    return "tigSection_4_1_Validation";
  }

  @Override
  protected boolean doValidate() {
    Configuration jsonPathConfig = Configuration.defaultConfiguration()
        .addOptions(Option.AS_PATH_LIST)
        .addOptions(Option.SUPPRESS_EXCEPTIONS);
    DocumentContext jpath = using(jsonPathConfig).parse(jsonObject.toString());
    List<String> vcardArraysPaths = jpath.read("$..entities..vcardArray");
    JcardCategoriesSchemas jcardCategoriesSchemas = new JcardCategoriesSchemas();
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
            if (jcardCategoriesSchemas.hasCategory(category)) {
              try {
                jcardCategoriesSchemas.getCategory(category).validate(categoryArray);
              } catch (ValidationException e) {
                if (category.equals("adr")) {
                  results.add(RDAPValidationResult.builder()
                      .code(-20800)
                      .value(jsonExceptionPointer + ":" + categoryArray)
                      .message(
                          "An entity with a non-structured address was found. See section 4.1 of the TIG.")
                      .build());
                  return false;
                }
              }
            }
            categoryArrayIndex++;
          }
        }
        vcardElementIndex++;
      }
    }
    return true;
  }
}
