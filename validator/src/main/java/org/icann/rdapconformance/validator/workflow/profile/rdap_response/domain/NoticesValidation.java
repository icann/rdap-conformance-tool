package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import java.util.Set;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.schema.JsonPointers;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public abstract class NoticesValidation extends ProfileJsonValidation {

  final int code;
  final String title;
  final String description;
  final String href;
  private final RDAPQueryType queryType;
  private final QueryContext queryContext;

  public NoticesValidation(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType,
      String title, String description, String href, int code) {
    super(rdapResponse, results);
    this.queryType = queryType;
    this.queryContext = null; // Not available in deprecated constructor
    this.title = title;
    this.description = description;
    this.href = href;
    this.code = code;
  }

  // QueryContext constructor for production use
  public NoticesValidation(QueryContext queryContext,
      String title, String description, String href, int code) {
    super(queryContext.getRdapResponseData(), queryContext.getResults());
    this.queryType = queryContext.getQueryType();
    this.queryContext = queryContext;
    this.title = title;
    this.description = description;
    this.href = href;
    this.code = code;
  }

  @Override
  protected boolean doValidate() {
    String path = String.format(
        "$..notices[?(@.title == '%s' && @.description contains '%s')].links[?(@.href == '%s')]",
        title, description, href);
    if (!exists(path)) {
      Set<String> noticesPaths = getPointerFromJPath("$..notices");
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
          .code(code)
          .value(getResultValue(noticesPaths.stream()
              .map(JsonPointers::fromJpath)
              .collect(Collectors.toSet())))
          .message(String.format("The notice for %s was not found.", href));

      if (queryContext != null) {
        results.add(builder.build(queryContext));
      } else {
        results.add(builder.build()); // Fallback for deprecated constructor
      }
      return false;
    }
    return true;
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
