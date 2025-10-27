package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import java.util.Set;
import org.icann.rdapconformance.validator.JpathUtil;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseValidationLinkElements_2024 extends ProfileValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationLinkElements_2024.class);
    private final JpathUtil jpathUtil;
    private final JSONObject jsonObject;

    public ResponseValidationLinkElements_2024(QueryContext qctx) {
        super(qctx.getResults());
        this.jpathUtil = new JpathUtil();
        this.jsonObject = new JSONObject(qctx.getRdapResponseData());
    }

    @Override
    public String getGroupName() {
        return "stdRdapLinksValidation";
    }

    @Override
    public boolean doValidate() {
        boolean isOK  = true;
        Set<String> linksJsonPointers = jpathUtil.getPointerFromJPath(jsonObject, "$..links");
        for (String jsonPointer : linksJsonPointers) {
            try {
                JSONArray links = (JSONArray) jsonObject.query(jsonPointer);
                for (int i = 0; i < links.length(); i++) {
                    JSONObject link = links.getJSONObject(i);
                    if (!link.has("value")) {
                        results.add(RDAPValidationResult.builder()
                                                        .code(-10612)
                                                        .value(jsonPointer + "/" + i + "/value:" + link)
                                                        .message("A 'value' property does not exist in the link object.")
                                                        .build());
                        isOK = false;
                    }
                    if (!link.has("rel")) {
                        results.add(RDAPValidationResult.builder()
                                                        .code(-10613)
                                                        .value(jsonPointer + "/" + i + "/rel:" + link)
                                                        .message("A 'rel' property does not exist in the link object.")
                                                        .build());
                        isOK = false;
                    }
                }
            } catch (Exception e) {
                logger.debug("Exception during evaluation of link properties: {} \n\n details: {}", jsonObject.query(jsonPointer), e);
            }
        }
        return isOK;
    }
}