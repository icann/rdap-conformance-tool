package org.icann.rdapconformance.validator.workflow.profile.tig_section.registrar;

import java.util.Set;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarId;
import org.json.JSONObject;

public final class TigValidation1Dot12Dot1 extends ProfileJsonValidation {

  private final RDAPDatasetService datasetService;
  private final RDAPQueryType queryType;
  private final QueryContext queryContext;
  private static final Set<RDAPQueryType> AUTHORIZED_QUERY_TYPES = Set.of(
      RDAPQueryType.DOMAIN,
      RDAPQueryType.NAMESERVER,
      RDAPQueryType.ENTITY
  );

  public TigValidation1Dot12Dot1(QueryContext queryContext) {
    super(queryContext.getRdapResponseData(), queryContext.getResults());
    this.datasetService = queryContext.getDatasetService();
    this.queryType = queryContext.getQueryType();
    this.queryContext = queryContext;
  }

  /**
   * @deprecated Use TigValidation1Dot12Dot1(QueryContext) instead
   * TODO: Migrate tests to QueryContext-only constructor
   */
  @Deprecated
  public TigValidation1Dot12Dot1(String rdapResponse, RDAPValidatorResults results, RDAPDatasetService datasetService, RDAPQueryType queryType) {
    super(rdapResponse, results);
    this.datasetService = datasetService;
    this.queryType = queryType;
    this.queryContext = null; // Not available in deprecated constructor
  }

  @Override
  public String getGroupName() {
    return "tigSection_1_12_1_Validation";
  }

  @Override
  protected boolean doValidate() {
    Set<String> publicIdsPaths = getPointerFromJPath(
        "$.entities[?(@.roles contains 'registrar')]..publicIds.*");
    boolean isValid = true;
    for (String jsonPointer : publicIdsPaths) {
      isValid &= checkPublicId(jsonPointer, (JSONObject) jsonObject.query(jsonPointer));
    }
    
    return isValid;
  }

  private boolean checkPublicId(String jsonPointer, JSONObject publicId) {
      if (!publicId.has("identifier")) {
        RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
            .code(-26100)
            .value(getResultValue(jsonPointer))
            .message("An identifier in the publicIds within the entity data "
                + "structure with the registrar role was not found. See section 1.12.1 of the "
                + "RDAP_Technical_Implementation_Guide_2_1.");

        results.add(builder.build(queryContext));
        return false;
      } else {
        int identifier = publicId.getInt("identifier");
        RegistrarId registrarId = datasetService.get(RegistrarId.class);
        if (!registrarId.containsId(identifier)) {
          RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
              .code(-26101)
              .value(getResultValue(jsonPointer + "/identifier"))
              .message("The registrar identifier is not included in the registrarId. "
                  + "See section 1.12.1 of the RDAP_Technical_Implementation_Guide_2_1.");

          results.add(builder.build(queryContext));
          return false;
        }

        RegistrarId.Record record = registrarId.getById(identifier);
        if (!record.getRdapUrl().isBlank() && !record.getRdapUrl().startsWith("https")) {
          RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
              .code(-26102)
              .value(jsonPointer + "/identifier" + ":" + record)
              .message("One or more of the base URLs for the registrar contain a "
                      + "schema different from https. See section 1.2 of the RDAP_Technical_Implementation_Guide_2_1.");

          results.add(builder.build(queryContext));
          return false;
        }
      }
    return true;
  }

  @Override
  public boolean doLaunch() {
    return AUTHORIZED_QUERY_TYPES.contains(queryType);
  }
}
