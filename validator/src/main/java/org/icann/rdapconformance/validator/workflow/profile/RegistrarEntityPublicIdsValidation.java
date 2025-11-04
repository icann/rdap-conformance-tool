package org.icann.rdapconformance.validator.workflow.profile;

import java.util.Set;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public abstract class RegistrarEntityPublicIdsValidation extends ProfileJsonValidation {

  protected final RDAPQueryType queryType;
  protected final QueryContext queryContext;
  final int code;


  // QueryContext constructor for production use
  public RegistrarEntityPublicIdsValidation(QueryContext queryContext, int code) {
    super(queryContext.getRdapResponseData(), queryContext.getResults());
    this.queryType = queryContext.getQueryType();
    this.queryContext = queryContext;
    this.code = code;
  }

  protected Set<String> getRegistrarEntitiesJsonPointers() {
    return getPointerFromJPath("$.entities[?(@.roles contains 'registrar')]");
  }

  @Override
  protected boolean doValidate() {
    boolean isValid = true;

    for (String jsonPointer : getRegistrarEntitiesJsonPointers()) {
      isValid &= checkEntity(jsonPointer);
    }

    return isValid;
  }

  protected boolean checkEntity(String entityJsonPointer) {
    boolean isValid = true;

    JSONObject entity = (JSONObject) jsonObject.query(entityJsonPointer);
    Set<String> publicIdsJsonPointers = getPointerFromJPath(entity, "$.publicIds[*]");
    if (publicIdsJsonPointers.isEmpty()) {
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
          .code(code)
          .value(getResultValue(entityJsonPointer))
          .message("A publicIds member is not included in the entity with the registrar role.");

      results.add(builder.build(queryContext));
      return false;
    }
    for (String jsonPointer : publicIdsJsonPointers) {
      isValid &= checkPublicId(entity, entityJsonPointer.concat(jsonPointer.substring(1)));
    }
    return isValid;
  }

  protected boolean checkPublicId(JSONObject entity, String publicIdJsonPointer) {
    JSONObject publicId = (JSONObject) jsonObject.query(publicIdJsonPointer);
    String identifier = publicId.optString("identifier", "");
    if (!isPositiveInteger(identifier)) {
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
          .code(code - 1)  // CalculatedCode: -23301 (TigValidation6Dot1)
          .value(getResultValue(publicIdJsonPointer))
          .message("The identifier of the publicIds member of the entity with the registrar role "
              + "is not a positive integer.");

      results.add(builder.build(queryContext));
      return false;
    }
    return true;
  }

  protected boolean isPositiveInteger(String nbr) {
    try {
      int id = Integer.parseInt(nbr);
      if (id < 0) {
        return false;
      }
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }
}
