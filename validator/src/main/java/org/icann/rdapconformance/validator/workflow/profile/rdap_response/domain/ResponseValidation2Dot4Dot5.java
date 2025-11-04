package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import java.util.Set;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;

public final class ResponseValidation2Dot4Dot5 extends ProfileJsonValidation {

  private final RDAPQueryType queryType;
  private final QueryContext queryContext;

  public ResponseValidation2Dot4Dot5(QueryContext qctx) {
    super(qctx.getRdapResponseData(), qctx.getResults());
    this.queryType = qctx.getQueryType();
    this.queryContext = qctx;
  }


  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_4_5_Validation";
  }

  public boolean doValidate() {
    boolean isValid = !getPointerFromJPath(
        "$.entities[?(@.roles contains 'registrar')]..entities[?(@.roles contains 'abuse')]")
        .isEmpty();

    isValid &= !getPointerFromJPath(
            "$.entities[?(@.roles contains 'registrar')]..entities[?(@.roles contains 'abuse')].vcardArray")
            .isEmpty();

    Set<String> vcardJsonPointers = getPointerFromJPath(
        "$.entities[?(@.roles contains 'registrar')]..entities[?(@.roles contains 'abuse')].vcardArray");
    for (String jsonPointer : vcardJsonPointers) {
      isValid &= checkVcard(jsonPointer);
    }

    if (!isValid) {
      results.add(RDAPValidationResult.builder()
          .code(-47500)
          .value(getResultValue(getPointerFromJPath("$.entities[?(@.roles contains 'registrar')]")))
          .message(
              "Tel and email members were not found for the entity within the entity with the abuse role in the topmost domain object.")
          .build(queryContext));
    }

    return isValid;
  }

  private boolean checkVcard(String vcardJsonPointer) {
    JSONArray vcardArray = (JSONArray) jsonObject.query(vcardJsonPointer);
    boolean hasTel = false;
    boolean hasEmail = false;
    for (Object vcardElement : vcardArray) {
      if (vcardElement instanceof JSONArray) {
        JSONArray vcardElementArray = (JSONArray) vcardElement;
        for (Object categoryArray : vcardElementArray) {
          JSONArray categoryJsonArray = ((JSONArray) categoryArray);
          String category = categoryJsonArray.getString(0);
          if (category.equals("tel")) {
            hasTel = true;
          }
          if (category.equals("email")) {
            hasEmail = true;
          }
        }
      }
    }
    return hasTel & hasEmail;
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
