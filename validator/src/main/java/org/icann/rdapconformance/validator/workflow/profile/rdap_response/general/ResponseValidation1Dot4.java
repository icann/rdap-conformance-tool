package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.jcard.JcardCategoriesSchemas;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.RDAPProfileVcardArrayValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;

public class ResponseValidation1Dot4 extends RDAPProfileVcardArrayValidation {

  public ResponseValidation1Dot4(QueryContext qctx) {
    super(qctx.getRdapResponseData(), qctx.getResults());
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_1_4_Validation";
  }


  @Override
  protected boolean validateVcardArray(String category, JSONArray categoryJsonArray,
      String jsonExceptionPointer, JcardCategoriesSchemas jcardCategoriesSchemas) {
    if (category.equals("adr")) {
      Object address = categoryJsonArray.get(3);
      if (address instanceof JSONArray) {
        String country = ((JSONArray) address).getString(6);
        if (!country.isEmpty()) {
          results.add(RDAPValidationResult.builder()
              .code(-40400)
              .value(jsonExceptionPointer + ":" + categoryJsonArray)
              .message("A vcard object with a country name parameter with data was found.")
              .build());
          return false;
        }
      }
    }
    return true;
  }
}
