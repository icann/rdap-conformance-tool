package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.util.Set;
import org.icann.rdapconformance.validator.jcard.JcardCategoriesSchemas;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.TigValidationVcardArray;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public final class Validation7Dot1And7Dot2 extends TigValidationVcardArray {

  private static final Set<String> AUTHORIZED_PHONE_TYPE = Set.of("voice", "fax");

  public Validation7Dot1And7Dot2(String rdapResponse,
      RDAPValidatorResults results) {
    super(rdapResponse, results);
  }

  @Override
  public String getGroupName() {
    return "tigSection_7_1_and_7_2_Validation";
  }

  @Override
  public boolean validateVcardArray(String category, JSONArray categorieJsonArray,
      String jsonExceptionPointer, JcardCategoriesSchemas jcardCategoriesSchemas) {
    if (category.equals("tel")) {
      Object phoneType = categorieJsonArray.get(1);
      if (!(phoneType instanceof JSONObject)) {
        logError(jsonExceptionPointer, phoneType);
        return false;
      }

      if (!AUTHORIZED_PHONE_TYPE.contains(((JSONObject) phoneType).get("type"))) {
        logError(jsonExceptionPointer, phoneType);
        return false;
      }
    }
    return true;
  }

  private void logError(String jsonExceptionPointer, Object value) {
    results.add(RDAPValidationResult.builder()
        .code(-20900)
        .value(jsonExceptionPointer + ":" + value.toString())
        .message(
            "An entity with a tel property without a voice or fax type was found. See section 7.1 and 7.2 of the TIG.")
        .build());
  }
}
