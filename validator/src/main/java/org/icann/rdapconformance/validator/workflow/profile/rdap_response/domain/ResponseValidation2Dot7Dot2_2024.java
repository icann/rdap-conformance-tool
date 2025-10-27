package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResponseValidation2Dot7Dot2_2024 extends ProfileJsonValidation {
    public static final String ENTITY_REGISTRANT_ROLE_PATH = "$.entities[?(@.roles contains 'registrant')]";
    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot2_2024.class);
    private final RDAPValidatorConfiguration config;
    private final RDAPQueryType queryType;

    public ResponseValidation2Dot7Dot2_2024(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
        this.config = qctx.getConfig();
        this.queryType = qctx.getQueryType();
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_7_2_Validation";
    }

    @Override
    protected boolean doValidate() {
        if(getPointerFromJPath(ENTITY_REGISTRANT_ROLE_PATH).isEmpty()) {
            logger.debug("adding -63000, domain does not have a registrant role in entities");
            results.add(RDAPValidationResult.builder()
                    .code(-63000)
                    .value(getResultValue(getPointerFromJPath("$")))
                    .message("A domain served by a registrar must have one registrant.")
                    .build());
            return false;
        }

        return true;
    }


    @Override
    public boolean doLaunch() {
        return queryType.equals(RDAPQueryType.DOMAIN) && config.isGtldRegistrar();
    }
}
