package org.icann.rdapconformance.validator.workflow.profile.tig_section.registrar;

import java.util.Set;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.customvalidator.IdnHostNameFormatValidator;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
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
  private static final IdnHostNameFormatValidator DOMAIN_VALIDATOR = new IdnHostNameFormatValidator();

  public TigValidation1Dot12Dot1(QueryContext queryContext) {
    super(queryContext.getRdapResponseData(), queryContext.getResults());
    this.datasetService = queryContext.getDatasetService();
    this.queryType = queryContext.getQueryType();
    this.queryContext = queryContext;
  }

  @Override
  public String getGroupName() {
    return "tigSection_1_12_1_Validation";
  }

  @Override
  protected boolean doValidate() {
    Set<String> publicIdsPaths = getPointerFromJPath(
            "$.entities[?(@.roles contains 'registrar')]..publicIds.*");
    for (String jsonPointer : publicIdsPaths) {
      if (!checkPublicId(jsonPointer, (JSONObject) jsonObject.query(jsonPointer))) {
        return false;
      }
    }
    return true;
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

        // -26103 check — only for gTLD registry domain queries
        if (queryType.equals(RDAPQueryType.DOMAIN)
                && queryContext.getConfig().isGtldRegistry()
                && record.isAccredited()
                && !record.getRdapUrl().isBlank()) {
          return checkRegistrarReferralLink(record.getRdapUrl());
        }
      }
    return true;
  }

  /**
   * Validates that the RDAP response contains a valid registrar referral link for gTLD registry
   * domain queries. Specifically, checks that at least one link with {@code rel} containing
   * {@code "related"} has an {@code href} that starts with the registrar's RDAP base URL
   * (as registered in the IANA registrar-ids dataset) followed by a valid domain name.
   * <p>If no valid referral link is found, a validation result with code {@code -26103} is added.
   * @param registrarBaseUrl the registrar's RDAP base URL from the IANA dataset
   *       (e.g. {@code https://www.example-registrar.com/rdap/domain/}).
   *       This value is used directly as the expected href prefix.
   * @return {@code true} if a valid referral link is found; {@code false} otherwise
   * */
  private boolean checkRegistrarReferralLink(String registrarBaseUrl) {
    String expectedPrefix = registrarBaseUrl.endsWith("/")
            ? registrarBaseUrl
            : registrarBaseUrl + "/";

    Set<String> relatedLinkPointers = getPointerFromJPath(
            "$.links[?(@.rel contains 'related')]");

    for (String linkPointer : relatedLinkPointers) {
      Object hrefObj = jsonObject.query(linkPointer + "/href");
      if (hrefObj != null) {
        String href = hrefObj.toString();
        if (href.startsWith(expectedPrefix)) {
          String afterPrefix = href.substring(expectedPrefix.length());
          // Strip trailing slash if present before validating
          String domainPart = afterPrefix.endsWith("/")
                  ? afterPrefix.substring(0, afterPrefix.length() - 1)
                  : afterPrefix;
          if (!domainPart.isBlank() && DOMAIN_VALIDATOR.validate(domainPart).isEmpty()) {
            return true;
          }
        }
      }
    }

    // No valid referral link found
    String foundHref = relatedLinkPointers.stream()
            .map(p -> {
              Object o = jsonObject.query(p + "/href");
              return o != null ? o.toString() : "";
            })
            .findFirst()
            .orElse("");

    results.add(RDAPValidationResult.builder()
            .code(-26103)
            .value(foundHref)
            .message("Referral to registrar is either unregistered with IANA or invalid.")
            .build(queryContext));

    return false;
  }

  @Override
  public boolean doLaunch() {
    return AUTHORIZED_QUERY_TYPES.contains(queryType);
  }
}
