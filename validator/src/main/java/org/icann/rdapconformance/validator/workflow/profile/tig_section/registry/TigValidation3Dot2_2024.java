package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public final class TigValidation3Dot2_2024 extends ProfileJsonValidation {

    private final RDAPValidatorConfiguration config;
    private final RDAPQueryType queryType;

    public TigValidation3Dot2_2024(String rdapResponse, RDAPValidatorResults results,
        RDAPValidatorConfiguration config,
        RDAPQueryType queryType) {
        super(rdapResponse, results);
        this.config = config;
        this.queryType = queryType;
    }

    @Override
    public String getGroupName() {
        return "tigSection_3_2_Validation";
    }

    @Override
    public boolean doValidate() {
        if (isRegistrarId9999()) {
            return true;
        }

        boolean isValid = false;
        JSONArray links = jsonObject.optJSONArray("links");
        if (links != null) {
            for (Object link : links) {
                JSONObject l = (JSONObject) link;
                if (l.optString("value").equals(this.config.getUri().toString())) {
                    isValid = true;
                }
            }
        } else {
            // if there is no link data structure, it will be reported by the original TigValidation3Dot2 using error code 23200
            isValid = true;
        }

        if(!isValid) {
            String linksStr = links == null ? "" : links.toString();
            results.add(RDAPValidationResult.builder()
                .code(-23201)
                .value(linksStr)
                .message("a value property must be specified and it must match the URI of the query.")
                .build());
        }


        return isValid;
    }

    public boolean isRegistrarId9999() {
        boolean is9999 = false;
        JSONArray entities = jsonObject.optJSONArray("entities");
        if (entities != null) {
            for (Object entitiesEntryObj : entities) {
                JSONObject entitiesEntry = (JSONObject) entitiesEntryObj;
                JSONArray publicIds = entitiesEntry.optJSONArray("publicIds");
                if (publicIds != null) {
                    for (Object publicIdsEntryObj : publicIds) {
                        JSONObject publicIdsEntry = (JSONObject) publicIdsEntryObj;
                        if (publicIdsEntry.get("identifier").equals("9999")) {
                            is9999 = true;
                        }
                    }
                }
            }
        }
        return is9999;
    }

    @Override
    public boolean doLaunch() {
        return config.isGtldRegistry() && queryType.equals(RDAPQueryType.DOMAIN);
    }
}