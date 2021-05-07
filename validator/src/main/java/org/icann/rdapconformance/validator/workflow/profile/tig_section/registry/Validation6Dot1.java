package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import com.jayway.jsonpath.JsonPath;
import java.util.Map;
import java.util.Set;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class Validation6Dot1 extends ProfileJsonValidation {

  private final RDAPQueryType queryType;

  public Validation6Dot1(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType) {
    super(rdapResponse, results);
    this.queryType = queryType;
  }


  @Override
  public String getGroupName() {
    return "tigSection_6_1_Validation";
  }

  public boolean doValidate() {
    boolean isValid = true;
    JSONArray registrarEntities = JsonPath.parse(jsonObject.toString())
        .read("$.entities[?(@.roles contains 'registrar')]");

    for (Object registrarEntity : registrarEntities) {
      isValid &= checkEntity(new JSONObject((Map<String, ?>) registrarEntity));
    }

    return isValid;
  }

  private boolean checkEntity(JSONObject entity) {
    boolean isValid = true;

    if (!entity.containsKey("publicIds")) {
      results.add(RDAPValidationResult.builder()
          .code(-23300)
          .value(entity.toJSONString())
          .message("A publicIds member is not included in the entity with the registrar role.")
          .build());
      return false;
    }
    JSONArray publicIds = JsonPath.parse(entity).read("$.publicIds");
    for (Object registrarPublicId : publicIds) {
      isValid &= checkPublicId(new JSONObject((Map<String, ?>) registrarPublicId));
    }
    return isValid;
  }

  private boolean checkPublicId(JSONObject publicId) {
    String identifier = JsonPath.parse(publicId).read("$.identifier");
    try {
      int id = Integer.parseInt(identifier);
      if (id < 0) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException e) {
      results.add(RDAPValidationResult.builder()
          .code(-23301)
          .value(publicId.toJSONString())
          .message("The identifier of the publicIds member of the entity with the registrar role "
              + "is not a positive integer.")
          .build());
      return false;
    }
    return true;
  }

  @Override
  protected boolean doLaunch() {
    return Set.of(RDAPQueryType.DOMAIN, RDAPQueryType.NAMESERVER, RDAPQueryType.ENTITY)
        .contains(queryType);
  }
}
