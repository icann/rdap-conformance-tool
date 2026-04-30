package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.jcard.JcardCategoriesSchemas;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.RDAPProfileVcardArrayValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;

public class ResponseValidation2024_1_4 extends RDAPProfileVcardArrayValidation {

    public static final String ADDRESS_CATEGORY = "adr";
    private final QueryContext queryContext;

    public ResponseValidation2024_1_4(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults(), qctx);
        this.queryContext = qctx;
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile2024_1_4_Validation";
    }

    @Override
    protected boolean validateVcardArray(String category, JSONArray categoryJsonArray,
                                         String jsonExceptionPointer, JcardCategoriesSchemas jcardCategoriesSchemas) {
        if (category.equals(ADDRESS_CATEGORY)) {
            Object address = categoryJsonArray.get(3);
            if (address instanceof JSONArray) {
                Object countryObj = ((JSONArray) address).get(6);
                if (!(countryObj instanceof String) || !((String) countryObj).isEmpty()) {
                    results.add(RDAPValidationResult.builder()
                            .code(-62100)
                            .value(jsonExceptionPointer + ":" + categoryJsonArray)
                            .message("All country names MUST be an empty string.")
                            .build(queryContext));
                    return false;
                }
            }
        }
        return true;
    }
}