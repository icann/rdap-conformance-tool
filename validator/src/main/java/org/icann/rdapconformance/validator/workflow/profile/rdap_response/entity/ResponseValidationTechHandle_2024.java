package org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity;

import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EPPRoid;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

import static org.icann.rdapconformance.validator.CommonUtils.DASH;

public final class ResponseValidationTechHandle_2024 extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationTechHandle_2024.class);

    public static final String ENTITY_ROLE_PATH = "$.entities[?(@.roles contains 'technical')]";
    public static final String ENTITY_TECH_HANDLE_PATH = "$.entities[?(@.roles contains 'technical')].handle";
    private static final String REDACTED_PATH = "$.redacted[*]";
    public static final String REGISTRY_TECH_ID = "Registry Tech ID";

    private Set<String> redactedPointersValue = null;
    private final RDAPQueryType queryType;
    private final RDAPDatasetService datasetService;
    private final QueryContext queryContext;

    public ResponseValidationTechHandle_2024(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
        this.queryType = qctx.getQueryType();
        this.datasetService = qctx.getDatasetService();
        this.queryContext = qctx;
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_tech_handle_Validation";
    }

    @Override
    public boolean doLaunch() {
        return queryType.equals(RDAPQueryType.DOMAIN);
    }

    @Override
    public boolean doValidate() {
        if (getPointerFromJPath(ENTITY_ROLE_PATH).isEmpty()) {
            return true;
        }

        if (getPointerFromJPath(ENTITY_TECH_HANDLE_PATH).isEmpty()) {
            return true;
        }

        boolean isValid = true;
        Set<String> entityPointers = getPointerFromJPath(ENTITY_ROLE_PATH);

        // Retrieve EPPRoid dataset once before the loop — it is invariant across iterations
        EPPRoid eppRoid = datasetService.get(EPPRoid.class);

        for (String jsonPointer : entityPointers) {
            JSONObject entity = (JSONObject) jsonObject.query(jsonPointer);

            if (!entity.has("handle")) {
                continue;
            }

            Object handleObj = entity.get("handle");
            if (handleObj instanceof String handle) {
                if (!handle.matches(CommonUtils.HANDLE_PATTERN)) {
                    // -65700: format does not comply with RFC5730
                    results.add(RDAPValidationResult.builder()
                            .code(-65700)
                            .value(handle)
                            .message("The handle of the technical entity does not comply with the format "
                                    + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.")
                            .build(queryContext));
                    isValid = false;
                } else {
                    // -65701: format is valid, now check EPPROID
                    String roid = handle.substring(handle.indexOf(DASH) + 1);
                    if (eppRoid.isInvalid(roid)) {
                        results.add(RDAPValidationResult.builder()
                                .code(-65701)
                                .value(handle)
                                .message("The globally unique identifier in the technical entity handle is not registered in EPPROID.")
                                .build(queryContext));
                        isValid = false;
                    }
                }
            }
        }

        // -65702: handle is present but a "Registry Tech ID" redaction also exists
        JSONObject redactedTechId = extractRedactedTechId();
        if (Objects.nonNull(redactedTechId)) {
            results.add(RDAPValidationResult.builder()
                    .code(-65702)
                    .value(getResultValue(redactedPointersValue))
                    .message("a redaction of type Registry Tech ID was found but the registrant handle was not redacted.")
                    .build(queryContext));
            isValid = false;
        }

        return isValid;
    }

    private JSONObject extractRedactedTechId() {
        JSONObject redactedTechId = null;
        redactedPointersValue = getPointerFromJPath(REDACTED_PATH);
        for (String redactedJsonPointer : redactedPointersValue) {
            JSONObject redacted = (JSONObject) jsonObject.query(redactedJsonPointer);
            JSONObject name = (JSONObject) redacted.get("name");
            try {
                var nameValue = name.get("type");
                if (nameValue instanceof String redactedName) {
                    if (redactedName.trim().equalsIgnoreCase(REGISTRY_TECH_ID)) {
                        redactedTechId = redacted;
                        break;
                    }
                }
            } catch (Exception e) {
                logger.debug("Redacted object at {} does not have extractable type property, skipping: {}",
                        redactedJsonPointer, e.getMessage());
            }
        }
        return redactedTechId;
    }
}