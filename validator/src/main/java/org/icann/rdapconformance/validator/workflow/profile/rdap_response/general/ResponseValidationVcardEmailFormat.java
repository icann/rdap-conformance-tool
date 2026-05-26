package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.regex.Pattern;

/** Validates that every "email" property value in every vCardArray of every entity
 * conforms to the dot-atom form of RFC 5322 addr-spec (Section 3.4.1).
 * <p>Note: The full RFC 5322 addr-spec grammar also permits quoted-string local-parts
 * (e.g., "user name"@domain.com) and domain-literals (e.g., user@[127.0.0.1]).
 * These forms are not used in RDAP/EPP registration data and are intentionally
 * out of scope for this validator. If such values are encountered they will be
 * flagged as invalid.
 * Error code -12320.
 */
public class ResponseValidationVcardEmailFormat extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationVcardEmailFormat.class);
    private static final int CODE = -12320;
    private static final String MESSAGE = "Email addresses must adhere to the 'addr-spec' format of RFC 5322 Section 3.4.1";
    private static final String ALL_ENTITIES_PATH = "$..entities[*]";
    // RFC 5322 Section 3.4.1 dot-atom allowed chars in local-part
    private static final Pattern LOCAL_PART_PATTERN =
            Pattern.compile("^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*$");

    // RFC 5322 dot-atom-text for domain labels (letters, digits, hyphens; no leading/trailing hyphen)
    private static final Pattern DOMAIN_LABEL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?$");

    private final QueryContext queryContext;

    public ResponseValidationVcardEmailFormat(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
        this.queryContext = qctx;
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_vcard_email_format_Validation";
    }

    @Override
    protected boolean doValidate() {
        boolean isValid = true;

        // Case 1: domain/nameserver response — entities array (recursive, covers nested)
        Set<String> entityPointers = getPointerFromJPath(ALL_ENTITIES_PATH);
        if (entityPointers != null && !entityPointers.isEmpty()) {
            for (String entityPointer : entityPointers) {
                isValid &= validateVcardEmails(entityPointer);
            }
        }

        // Case 2: entity lookup response — vcardArray at topmost level
        if (jsonObject.has("vcardArray") && "entity".equals(jsonObject.optString("objectClassName"))) {
            isValid &= validateVcardEmailsDirect(jsonObject.getJSONArray("vcardArray"));
        }

        return isValid;
    }

    private boolean validateVcardEmails(String entityPointer) {
        try {
            JSONObject entity = (JSONObject) jsonObject.query(entityPointer);
            JSONArray vcardArray = entity.optJSONArray("vcardArray");
            if (vcardArray == null || vcardArray.length() < 2) {
                return true;
            }
            return validateVcardEmailsDirect(vcardArray);
        } catch (Exception e) {
            logger.debug("Skipping malformed entity at {}: {}", entityPointer, e.getMessage());
            return true;
        }
    }

    private boolean validateVcardEmailsDirect(JSONArray vcardArray) {
        // Defensive checks — malformed vCard structure is handled by -12305
        if (vcardArray == null || vcardArray.length() < 2) {
            logger.debug("vcardArray is null or has fewer than 2 elements, skipping email format validation");
            return true;
        }

        Object propertiesObj = vcardArray.get(1);
        if (!(propertiesObj instanceof JSONArray vcardProperties)) {
            logger.debug("vcardArray[1] is not a JSONArray, skipping email format validation");
            return true;
        }

        boolean isValid = true;
        for (int i = 0; i < vcardProperties.length(); i++) {
            try {
                JSONArray property = vcardProperties.getJSONArray(i);
                if (property.length() >= 4 && "email".equals(property.getString(0))) {
                    String emailValue = property.get(3).toString();
                    if (!isValidAddrSpec(emailValue)) {
                        logger.debug("Invalid email addr-spec: {}", emailValue);
                        results.add(RDAPValidationResult.builder()
                                .code(CODE)
                                .value(emailValue)
                                .message(MESSAGE)
                                .build(queryContext));
                        isValid = false;
                    }
                }
            } catch (Exception e) {
                logger.debug("Skipping malformed vcard property at index {}: {}", i, e.getMessage());
            }
        }
        return isValid;
    }

    private boolean isValidAddrSpec(String email) {
        if (email == null || email.contains(" ")) {
            return false;
        }

        // Must contain exactly one @
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex != email.lastIndexOf('@')) {
            return false;
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);

        // Validate local-part: dot-atom rules (no leading/trailing/consecutive dots)
        if (!LOCAL_PART_PATTERN.matcher(localPart).matches()) {
            return false;
        }

        // Validate domain directly (RFC 5322 dot-atom-text):
        // - must contain at least one dot — RFC 5322 allows single-atom domains (e.g., user@localhost)
        //   but dotless domains are not valid in RDAP/EPP registration data where an FQDN is required
        // - no leading or trailing dot (no empty labels)
        // - each label must match DOMAIN_LABEL_PATTERN
        if (!domain.contains(".")) {
            return false;
        }
        if (domain.startsWith(".") || domain.endsWith(".")) {
            return false;
        }
        String[] labels = domain.split("\\.", -1);
        for (String label : labels) {
            if (label.isEmpty() || !DOMAIN_LABEL_PATTERN.matcher(label).matches()) {
                return false;
            }
        }

        return true;
    }
}