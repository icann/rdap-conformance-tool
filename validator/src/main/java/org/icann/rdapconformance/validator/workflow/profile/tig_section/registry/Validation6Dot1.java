package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import com.jayway.jsonpath.JsonPath;
import java.util.Map;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class Validation6Dot1 {

  private final String rdapResponse;
  private final RDAPValidatorResults results;

  public Validation6Dot1(String rdapResponse,
      RDAPValidatorResults results) {
    this.rdapResponse = rdapResponse;
    this.results = results;
  }


  public boolean validate() {
    boolean hasError = false;
    JSONArray registrarEntities = JsonPath.parse(rdapResponse)
        .read("$.entities[?(@.roles contains 'registrar')]");

    for (Object registrarEntity : registrarEntities) {
      hasError |= checkEntity(new JSONObject((Map<String, ?>) registrarEntity));
    }

    results.addGroup("tigSection_6_1_Validation", hasError);
    return !hasError;
  }

  private boolean checkEntity(JSONObject entity) {
    boolean hasError = false;

    if (!entity.containsKey("publicIds")) {
      results.add(RDAPValidationResult.builder()
          .code(-23300)
          .value(entity.toJSONString())
          .message("A publicIds member is not included in the entity with the registrar role.")
          .build());
      return true;
    }
    JSONArray publicIds = JsonPath.parse(entity).read("$.publicIds");
    for (Object registrarPublicId : publicIds) {
      hasError |= checkPublicId(new JSONObject((Map<String, ?>) registrarPublicId));
    }
    return hasError;
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
      return true;
    }
    return false;
  }
}
