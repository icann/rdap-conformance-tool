package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.util.Set;
import org.icann.rdapconformance.validator.jcard.JcardCategoriesSchemas;
import org.icann.rdapconformance.validator.workflow.profile.RDAPProfileVcardArrayValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public final class TigValidation7Dot1And7Dot2 extends RDAPProfileVcardArrayValidation {

  private static final Set<String> AUTHORIZED_PHONE_TYPE = Set.of("voice", "fax");

  public TigValidation7Dot1And7Dot2(String rdapResponse,
      RDAPValidatorResults results) {
    super(rdapResponse, results);
  }

  @Override
  public String getGroupName() {
    return "tigSection_7_1_and_7_2_Validation";
  }

  @Override
  public boolean validateVcardArray(String category, JSONArray categoryJsonArray,
      String jsonExceptionPointer, JcardCategoriesSchemas jcardCategoriesSchemas) {
    if (category.equals("tel")) {
      Object phoneType = categoryJsonArray.get(1);
      if (!(phoneType instanceof JSONObject)) {
        logError(jsonExceptionPointer, phoneType);
        return false;
      }

      Object type = ((JSONObject) phoneType).get("type");
      if (type instanceof JSONArray) {
        type = ((JSONArray) type).getString(0);
      }

      if (!AUTHORIZED_PHONE_TYPE.contains(type.toString())) {
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
        .message("An entity with a tel property without a voice or fax type was found. "
            + "See section 7.1 and 7.2 of the TIG.")
        .build());
  }
}
