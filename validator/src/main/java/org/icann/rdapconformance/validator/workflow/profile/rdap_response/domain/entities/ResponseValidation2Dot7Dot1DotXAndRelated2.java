package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.util.Set;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.jcard.JcardCategoriesSchemas;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

/**
 * 8.8.1.2
 */
public class ResponseValidation2Dot7Dot1DotXAndRelated2 extends
    ResponseValidation2Dot7Dot1DotXAndRelated {

  public ResponseValidation2Dot7Dot1DotXAndRelated2(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType,
      RDAPValidatorConfiguration config) {
    super(rdapResponse, results, queryType, config);
  }

  @Override
  protected boolean doValidateEntity(String jsonPointer, JSONObject entity) {
    if (isChildOfRegistrar(jsonPointer)) {
      return true;
    }

    Set<String> withRemarkTitleRedactedForPrivacy =
        getPointerFromJPath(entity, "$.remarks[?(@.title == 'REDACTED FOR PRIVACY')]");

    boolean isValid = true;
    if (withRemarkTitleRedactedForPrivacy.isEmpty()) {
      // Validate handle field (top-level entity property)
      if (!entity.has("handle")) {
        isValid &= log52101(jsonPointer);
      }
      
      // Validate vcard properties
      Set<String> properties = Set.of("fn", "adr", "tel", "email");
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

        if (property.equals("adr")) {
          // check if adr contains an address with street (index == 2), City (index == 3) & Country (index == 6)
          Set<String> adrPointers = getPointerFromJPath(entity,
              "vcardArray[1][?(@[0]=='adr')][3][2,3]");
          if (adrPointers.size() < 2) {
            // less than 3 means an address component is missing, we log an error:
            isValid &= log52101(jsonPointer);
          }
        }
      }
    }
    return isValid;
  }

  private Set<String> getVcardPropertyPointers(JSONObject entity, String property) {
    return getPointerFromJPath(entity, "vcardArray[1][?(@[0] == '" + property + "')]");
  }

  private boolean log52101(String jsonPointer) {
    results.add(RDAPValidationResult.builder()
        .code(-52101)
        .value(getResultValue(jsonPointer))
        .message("An entity without a remark titled \"REDACTED FOR PRIVACY\" " +
                "does not have all the necessary information of handle, fn, adr, tel, street and city.")
        .build());
    return false;
  }
}
