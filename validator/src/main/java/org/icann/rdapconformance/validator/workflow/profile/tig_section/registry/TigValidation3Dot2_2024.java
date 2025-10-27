package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.regex.Pattern;

public final class TigValidation3Dot2_2024 extends ProfileJsonValidation {

    private final RDAPValidatorConfiguration config;
    private final RDAPQueryType queryType;
    private final org.icann.rdapconformance.validator.QueryContext queryContext;
    private static final Pattern DOMAIN_QUERY_PATTERN = Pattern.compile("^https?://[^/]+/.*/domain/.+$");
    
    // Error messages
    private static final String ERROR_23201_MESSAGE = "a value property must be specified and it must match the URI of the query.";
    private static final String ERROR_23202_MESSAGE = "the href property must be domain query as defined by Section 3.1.3 of RFC 9082.";
    
    // Field names
    private static final String LINKS_FIELD = "links";
    private static final String VALUE_FIELD = "value";
    private static final String REL_FIELD = "rel";
    private static final String HREF_FIELD = "href";
    private static final String ENTITIES_FIELD = "entities";
    private static final String PUBLIC_IDS_FIELD = "publicIds";
    private static final String IDENTIFIER_FIELD = "identifier";
    private static final String REL_RELATED = "related";
    
    // Excluded registrar IDs
    private static final String[] EXCLUDED_REGISTRAR_IDS = {"9994", "9995", "9996", "9997", "9998", "9999"};

    public TigValidation3Dot2_2024(String rdapResponse, RDAPValidatorResults results,
        RDAPValidatorConfiguration config,
        RDAPQueryType queryType) {
        super(rdapResponse, results);
        this.config = config;
        this.queryType = queryType;
        this.queryContext = null; // For legacy compatibility
    }

    public TigValidation3Dot2_2024(String rdapResponse, RDAPValidatorResults results,
        RDAPValidatorConfiguration config,
        RDAPQueryType queryType, org.icann.rdapconformance.validator.QueryContext queryContext) {
        super(rdapResponse, results);
        this.config = config;
        this.queryType = queryType;
        this.queryContext = queryContext;
    }

    @Override
    public String getGroupName() {
        return "tigSection_3_2_Validation";
    }

    @Override
    public boolean doValidate() {
        if (isExcludedRegistrarId()) {
            return true;
        }

        boolean isValid23201 = validate23201();
        boolean isValid23202 = validate23202();
        
        return isValid23201 && isValid23202;
    }
    
    private boolean validate23201() {
        boolean isValid = false;
        JSONArray links = jsonObject.optJSONArray(LINKS_FIELD);
        if (links != null) {
            for (Object link : links) {
                JSONObject l = (JSONObject) link;
                if (l.optString(VALUE_FIELD).equals(this.config.getUri().toString())) {
                    isValid = true;
                }
            }
        } else {
            // if there is no link data structure, it will be reported by the original TigValidation3Dot2 using error code 23200
            isValid = true;
        }

        if(!isValid) {
            String linksStr = links == null ? "" : links.toString();
            RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
                .code(-23201)
                .value(linksStr)
                .message(ERROR_23201_MESSAGE);

            if (queryContext != null) {
                results.add(builder.build(queryContext));
            } else {
                results.add(builder.build());
            }
        }
        
        return isValid;
    }
    
    private boolean validate23202() {
        boolean isValid = true;
        JSONArray links = jsonObject.optJSONArray(LINKS_FIELD);
        
        if (links != null) {
            for (Object link : links) {
                JSONObject l = (JSONObject) link;
                // Check links with rel="related" that have href (same as -23200 test)
                if (l.optString(REL_FIELD).equals(REL_RELATED) && l.has(HREF_FIELD)) {
                    String href = l.optString(HREF_FIELD);
                    if (href.isEmpty() || !DOMAIN_QUERY_PATTERN.matcher(href).matches()) {
                        RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
                            .code(-23202)
                            .value(l.toString())
                            .message(ERROR_23202_MESSAGE);

                        if (queryContext != null) {
                            results.add(builder.build(queryContext));
                        } else {
                            results.add(builder.build());
                        }
                        isValid = false;
                    }
                }
            }
        }
        
        return isValid;
    }

    public boolean isExcludedRegistrarId() {
        boolean isExcluded = false;
        JSONArray entities = jsonObject.optJSONArray(ENTITIES_FIELD);
        if (entities != null) {
            for (Object entitiesEntryObj : entities) {
                JSONObject entitiesEntry = (JSONObject) entitiesEntryObj;
                JSONArray publicIds = entitiesEntry.optJSONArray(PUBLIC_IDS_FIELD);
                if (publicIds != null) {
                    for (Object publicIdsEntryObj : publicIds) {
                        JSONObject publicIdsEntry = (JSONObject) publicIdsEntryObj;
                        String identifier = publicIdsEntry.optString(IDENTIFIER_FIELD, "");
                        for (String excludedId : EXCLUDED_REGISTRAR_IDS) {
                            if (identifier.equals(excludedId)) {
                                isExcluded = true;
                                break;
                            }
                        }
                        if (isExcluded) break;
                    }
                }
                if (isExcluded) break;
            }
        }
        return isExcluded;
    }
    
    // Backward compatibility method
    public boolean isRegistrarId9999() {
        return isExcludedRegistrarId();
    }

    @Override
    public boolean doLaunch() {
        return config.isGtldRegistry() && queryType.equals(RDAPQueryType.DOMAIN);
    }
}