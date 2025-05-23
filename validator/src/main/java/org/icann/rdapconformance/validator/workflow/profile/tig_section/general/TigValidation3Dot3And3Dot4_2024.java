package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.icann.rdapconformance.validator.CommonUtils.HTTP;

import java.util.Set;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public class TigValidation3Dot3And3Dot4_2024 extends ProfileJsonValidation {
    private final RDAPValidatorConfiguration config;

    public TigValidation3Dot3And3Dot4_2024(String rdapResponse,
        RDAPValidatorResults results, RDAPValidatorConfiguration config) {
        super(rdapResponse, results);

        this.config = config;
    }

    @Override
    public String getGroupName() {
        return "tigSection_3_3_and_3_4_Validation";
    }

    @Override
    public boolean doValidate() {
        boolean is61200Valid = false;
        boolean is61201Valid = true;
        boolean is61202Valid = true;

        Set<String> linksInTopMostNotices = getPointerFromJPath("$.notices[*].links");

        for (String jsonPointer : linksInTopMostNotices) {
            JSONArray links = (JSONArray) jsonObject.query(jsonPointer);

            for (Object link : links) {
                JSONObject l = (JSONObject) link;

                if (l.optString("rel").equals("terms-of-service")) {
                    is61200Valid = true;

                    if (!l.optString("href").startsWith(HTTP)) {
                        is61201Valid = false;

                        results.add(RDAPValidationResult.builder()
                            .code(-61201)
                            .value(l.toString())
                            .message("This link must have an href.")
                            .build());
                    }


                    if (!l.optString("value").equals(this.config.getUri().toString())) {
                        is61202Valid = false;

                        results.add(RDAPValidationResult.builder()
                            .code(-61202)
                            .value(l.toString())
                            .message("This link must have a value that is the same as the queried URI.")
                            .build());
                    }
                }
            }
        }

        if (!is61200Valid) {
            results.add(RDAPValidationResult.builder()
                .code(-61200)
                .value(jsonObject.toString())
                .message("The response must have one notice to the terms of service.")
                .build());
        }

        return is61200Valid && is61201Valid && is61202Valid;
    }
}
