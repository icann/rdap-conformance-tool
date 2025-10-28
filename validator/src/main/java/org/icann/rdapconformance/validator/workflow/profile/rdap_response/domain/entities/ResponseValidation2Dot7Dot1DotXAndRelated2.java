package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.util.Set;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.jcard.JcardCategoriesSchemas;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 8.8.1.2
 */
public class ResponseValidation2Dot7Dot1DotXAndRelated2 extends
    ResponseValidation2Dot7Dot1DotXAndRelated {

  // vCard adr property array indices per RFC 6350 Section 6.3.1
  private static final int ADR_STREET_INDEX = 2;
  private static final int ADR_CITY_INDEX = 3;
  
  // vCard property structure - value is at index 3 per RFC 6350
  private static final int VCARD_VALUE_INDEX = 3;
  private static final int VCARD_ADR_VALUE_ARRAY_INDEX = 3; // adr property value is at index 3
  
  // Entity field names
  private static final String HANDLE_FIELD = "handle";
  
  // vCard property names per RFC 6350
  private static final String VCARD_PROPERTY_FN = "fn";
  private static final String VCARD_PROPERTY_ADR = "adr";  
  private static final String VCARD_PROPERTY_TEL = "tel";
  
  // JSONPath expressions
  private static final String REDACTED_FOR_PRIVACY_REMARKS_PATH = "$.remarks[?(@.title == 'REDACTED FOR PRIVACY')]";
  private static final String VCARD_ARRAY_PATH_TEMPLATE = "vcardArray[1][?(@[0] == '%s')]";
  private static final String VCARD_ADR_PROPERTIES_PATH = "vcardArray[1][?(@[0]=='adr')]";
  
  // Error code and message
  private static final int ERROR_CODE_52101 = -52101;
  private static final String ERROR_MESSAGE_52101 = "An entity without a remark titled \"REDACTED FOR PRIVACY\" " +
          "does not have all the necessary information of handle, fn, adr, tel, street and city.";

  public ResponseValidation2Dot7Dot1DotXAndRelated2(QueryContext queryContext) {
    super(queryContext.getRdapResponseData(), queryContext.getResults(), queryContext.getQueryType(), queryContext.getConfig());
  }

  /**
   * @deprecated Use ResponseValidation2Dot7Dot1DotXAndRelated2(QueryContext) instead
   * TODO: Migrate tests to QueryContext-only constructor
   */
  @Deprecated
  public ResponseValidation2Dot7Dot1DotXAndRelated2(String rdapResponse, RDAPValidatorResults results, RDAPQueryType queryType, RDAPValidatorConfiguration config) {
    super(rdapResponse, results, queryType, config);
  }

  @Override
  protected boolean doValidateEntity(String jsonPointer, JSONObject entity) {
    if (isChildOfRegistrar(jsonPointer)) {
      return true;
    }

    Set<String> withRemarkTitleRedactedForPrivacy =
        getPointerFromJPath(entity, REDACTED_FOR_PRIVACY_REMARKS_PATH);

    boolean isValid = true;
    if (withRemarkTitleRedactedForPrivacy.isEmpty()) {
      // Validate handle field (top-level entity property)
      if (!entity.has(HANDLE_FIELD)) {
        isValid &= log52101(jsonPointer);
      } else {
        // Check handle content is not empty
        try {
          String handleValue = entity.getString(HANDLE_FIELD);
          if (handleValue.trim().isEmpty()) {
            isValid &= log52101(jsonPointer);
          }
        } catch (Exception e) {
          // Handle is not a string or malformed
          isValid &= log52101(jsonPointer);
        }
      }
      
      // Validate vcard properties per story requirements
      Set<String> properties = Set.of(VCARD_PROPERTY_FN, VCARD_PROPERTY_ADR, VCARD_PROPERTY_TEL);
      for (String property : properties) {
        isValid &= validateVcardProperty(jsonPointer, entity, property);
      }
    }

    return isValid;
  }

  private boolean validateVcardProperty(String jsonPointer, JSONObject entity, String property) {
    Set<String> propertyPointers = getVcardPropertyPointers(entity, property);
    JcardCategoriesSchemas jcardCategoriesSchemas = new JcardCategoriesSchemas();
    boolean isValid = true;
    if (propertyPointers.isEmpty()) {
      isValid &= log52101(jsonPointer);
    } else {
      for (String propertyPointer : propertyPointers) {
        try {
          jcardCategoriesSchemas.getCategory(property).validate(entity.query(propertyPointer));
        } catch (ValidationException e) {
          isValid &= log52101(jsonPointer);
        }

        // Check for empty content in key vCard properties per story requirements
        if (property.equals(VCARD_PROPERTY_FN) || property.equals(VCARD_PROPERTY_TEL)) {
          try {
            JSONArray vcardProperty = (JSONArray) entity.query(propertyPointer);
            Object valueObj = vcardProperty.get(VCARD_VALUE_INDEX);
            if (valueObj instanceof String value) {
              if (value.trim().isEmpty()) {
                isValid &= log52101(jsonPointer);
                continue; // Skip to next property pointer
              }
            } else {
              // Value is not a string (invalid format)
              isValid &= log52101(jsonPointer);
              continue;
            }
          } catch (Exception e) {
            // Malformed vCard property
            isValid &= log52101(jsonPointer);
            continue;
          }
        }

        if (property.equals(VCARD_PROPERTY_ADR)) {
          // Check if adr contains meaningful street (index 2) and city (index 3) values
          Set<String> adrPropertyPointers = getPointerFromJPath(entity, VCARD_ADR_PROPERTIES_PATH);
          for (String adrPropertyPointer : adrPropertyPointers) {
            try {
              JSONArray adrProperty = (JSONArray) entity.query(adrPropertyPointer);
              JSONArray adrValues = adrProperty.getJSONArray(VCARD_ADR_VALUE_ARRAY_INDEX);
              
              // Check street (index 2) - must be non-empty string
              if (adrValues.length() > ADR_STREET_INDEX) {
                Object streetObj = adrValues.get(ADR_STREET_INDEX);
                if (streetObj instanceof String street) {
                  if (street.trim().isEmpty()) {
                    isValid &= log52101(jsonPointer);
                    break;
                  }
                } else {
                  // Street is not a string (invalid format)
                  isValid &= log52101(jsonPointer);
                  break;
                }
              } else {
                // Street index missing
                isValid &= log52101(jsonPointer);
                break;
              }
              
              // Check city (index 3) - must be non-empty string
              if (adrValues.length() > ADR_CITY_INDEX) {
                Object cityObj = adrValues.get(ADR_CITY_INDEX);
                if (cityObj instanceof String city) {
                  if (city.trim().isEmpty()) {
                    isValid &= log52101(jsonPointer);
                    break;
                  }
                } else {
                  // City is not a string (invalid format)
                  isValid &= log52101(jsonPointer);
                  break;
                }
              } else {
                // City index missing
                isValid &= log52101(jsonPointer);
                break;
              }
            } catch (Exception e) {
              // Malformed adr property
              isValid &= log52101(jsonPointer);
              break;
            }
          }
        }
      }
    }
    return isValid;
  }

  private Set<String> getVcardPropertyPointers(JSONObject entity, String property) {
    return getPointerFromJPath(entity, String.format(VCARD_ARRAY_PATH_TEMPLATE, property));
  }

  private boolean log52101(String jsonPointer) {
    results.add(RDAPValidationResult.builder()
        .code(ERROR_CODE_52101)
        .value(getResultValue(jsonPointer))
        .message(ERROR_MESSAGE_52101)
        .build());
    return false;
  }
}
