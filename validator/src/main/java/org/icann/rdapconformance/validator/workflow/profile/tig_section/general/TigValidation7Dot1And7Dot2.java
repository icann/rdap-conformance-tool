package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.util.Set;
import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.jcard.JcardCategoriesSchemas;
import org.icann.rdapconformance.validator.workflow.profile.RDAPProfileVcardArrayValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public final class TigValidation7Dot1And7Dot2 extends RDAPProfileVcardArrayValidation {

  private static final String TEL_CATEGORY = "tel";
  private static final String VOICE_TYPE = "voice";
  private static final String FAX_TYPE = "fax";
  private static final Set<String> AUTHORIZED_PHONE_TYPE = Set.of(VOICE_TYPE, FAX_TYPE);

  public TigValidation7Dot1And7Dot2(String rdapResponse,
      RDAPValidatorResults results, RDAPValidatorConfiguration config) {
    super(rdapResponse, results, config);
  }

  @Override
  public String getGroupName() {
    return "tigSection_7_1_and_7_2_Validation";
  }

  @Override
  public boolean validateVcardArray(String category, JSONArray categoryJsonArray,
      String jsonExceptionPointer, JcardCategoriesSchemas jcardCategoriesSchemas) {
    if (category.equals(TEL_CATEGORY)) {
      Object phoneType = categoryJsonArray.get(CommonUtils.ONE);
      if (!(phoneType instanceof JSONObject)) {
        logError(jsonExceptionPointer, phoneType);
        return false;
      }

      Object type = ((JSONObject) phoneType).get("type");
      boolean hasValidType = false;
      
      if (type instanceof JSONArray) {
        JSONArray typeArray = (JSONArray) type;
        for (int i = CommonUtils.ZERO; i < typeArray.length(); i++) {
          try {
            String typeValue = typeArray.getString(i);
            if (AUTHORIZED_PHONE_TYPE.contains(typeValue)) {
              hasValidType = true;
              break;
            }
          } catch (Exception e) {
            // Skip non-string elements (null, numbers, objects, etc.)
            // Continue checking other elements in the array
            continue;
          }
        }
      } else if (type != null && AUTHORIZED_PHONE_TYPE.contains(type.toString())) {
        hasValidType = true;
      }

      if (!hasValidType) {
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
