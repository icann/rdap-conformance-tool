package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import org.apache.commons.lang3.StringUtils;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 8.8.1.1
 */
public abstract class ResponseValidation2Dot7Dot1DotXAndRelated extends
    EntitiesWithinDomainProfileJsonValidation {

  private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot1DotXAndRelated.class);

  public ResponseValidation2Dot7Dot1DotXAndRelated(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType,
      RDAPValidatorConfiguration config) {
    super(rdapResponse, results, queryType, config);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_7_1_X_and_2_7_2_X_and_2_7_3_X_and_2_7_4_X_Validation";
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN)
        && ((config.isGtldRegistry() && !config.isThin())
        || config.isGtldRegistrar());
  }

  protected boolean isChildOfRegistrar(String jsonPointer) {
    int level = StringUtils.countMatches(jsonPointer, "entities");

    if (level == 1) {
      jsonPointer = jsonPointer + "/roles";

      JSONArray roles = (JSONArray) jsonObject.query(jsonPointer);

      for (Object role : roles) {
        if ("registrar".equalsIgnoreCase(role.toString())) {
          return true;
        }
      }
    } else if (level > 1) {
      // it is child
      jsonPointer = jsonPointer.substring(0, jsonPointer.lastIndexOf("entities") - 1);

      return isChildOfRegistrar(jsonPointer);
    } else {
      //level < 1, i.e. "entities" is not found in the jsonPointer, should never happen
      logger.warn("level = {}, jsonPointer = {}", level, jsonPointer);
    }

    return false;
  }
}
