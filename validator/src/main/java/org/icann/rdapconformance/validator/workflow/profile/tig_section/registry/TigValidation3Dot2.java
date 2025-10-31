package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public final class TigValidation3Dot2 extends ProfileJsonValidation {

  private final RDAPValidatorConfiguration config;
  private final RDAPQueryType queryType;
  private final QueryContext queryContext;
  
  // Field names
  private static final String LINKS_FIELD = "links";
  private static final String REL_FIELD = "rel";
  private static final String HREF_FIELD = "href";
  private static final String ENTITIES_FIELD = "entities";
  private static final String PUBLIC_IDS_FIELD = "publicIds";
  private static final String IDENTIFIER_FIELD = "identifier";
  private static final String REL_RELATED = "related";
  
  // Excluded registrar IDs
  private static final String[] EXCLUDED_REGISTRAR_IDS = {"9994", "9995", "9996", "9997", "9998", "9999"};
  
  // Error message
  private static final String ERROR_23200_MESSAGE = "A links data structure in the topmost object exists, and the links object "
      + "shall contain the elements rel:related and href, but they were not found. "
      + "See section 3.2 of the RDAP_Technical_Implementation_Guide_2_1.";

  public TigValidation3Dot2(QueryContext queryContext) {
    super(queryContext.getRdapResponseData(), queryContext.getResults());
    this.config = queryContext.getConfig();
    this.queryType = queryContext.getQueryType();
    this.queryContext = queryContext;
  }

  /**
   * @deprecated Use TigValidation3Dot2(QueryContext) instead
   * TODO: Migrate tests to QueryContext-only constructor
   */
  @Deprecated
  public TigValidation3Dot2(String rdapResponse, RDAPValidatorResults results, RDAPValidatorConfiguration config, RDAPQueryType queryType) {
    super(rdapResponse, results);
    this.config = config;
    this.queryType = queryType;
    this.queryContext = null; // Not available in deprecated constructor
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

    boolean isValid = false;
    JSONArray links = jsonObject.optJSONArray(LINKS_FIELD);
    if (links != null) {
      for (Object link : links) {
        JSONObject l = (JSONObject) link;
        if (l.optString(REL_FIELD).equals(REL_RELATED) && l.optString(HREF_FIELD, null) != null) {
          isValid = true;
        }
      }
    }

    if (!isValid) {
      String linksStr = links == null ? "" : links.toString();
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
          .code(-23200)
          .value(linksStr)
          .message(ERROR_23200_MESSAGE);

      results.add(builder.build(queryContext));
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

  @Override
  public boolean doLaunch() {
    return config.isGtldRegistry() && queryType.equals(RDAPQueryType.DOMAIN);
  }

}