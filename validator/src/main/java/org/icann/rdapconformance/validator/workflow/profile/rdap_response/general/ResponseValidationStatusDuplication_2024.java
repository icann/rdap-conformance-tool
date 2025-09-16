package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.JpathUtil;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ResponseValidationStatusDuplication_2024 extends ProfileValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationStatusDuplication_2024.class);
    private static final String STATUS_PATH = "$.status"; // top-level structure only
    private static final String STATUS = "status";
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private final JpathUtil jpathUtil;
    private JSONObject jsonObject = null;
    private RDAPValidatorResults results = null;

    public ResponseValidationStatusDuplication_2024(String rdapResponse, RDAPValidatorResults results) {
        super(results);
        this.jpathUtil = new JpathUtil();
        this.jsonObject = new JSONObject(rdapResponse);
        this.results = results;
    }

    @Override
    public String getGroupName() {
        return "stdRdapStatusValidation";
    }

    @Override
    public boolean doValidate() {
        boolean isOK = true;
        try {
            // Check for duplicate status values within the status array
            Set<String> statusPaths = jpathUtil.getPointerFromJPath(jsonObject, STATUS_PATH);
            if (statusPaths.size() == ONE) {
                var statusList = jsonObject.getJSONArray(STATUS);
                var seenStatuses = new HashSet<String>();
                var duplicateStatuses = new HashSet<String>();
                for (int i = ZERO; i < statusList.length(); i++) {
                    String statusValue = statusList.getString(i);
                    if (!seenStatuses.add(statusValue)) {
                        duplicateStatuses.add(statusValue);
                    }
                }
                for (String duplicateStatus : duplicateStatuses) {
                    results.add(RDAPValidationResult.builder()
                                                    .code(-11003)
                                                    .value(STATUS_PATH + ": " + duplicateStatus)
                                                    .message("A status value exists more than once in the status array")
                                                    .build());
                    isOK = false;
                }
            }
        } catch (Exception e) {
            logger.info("Exception during evaluation of status properties: {} \n\n details: {}", jsonObject, e);
            isOK = false;
        }
        return isOK;
    }
}