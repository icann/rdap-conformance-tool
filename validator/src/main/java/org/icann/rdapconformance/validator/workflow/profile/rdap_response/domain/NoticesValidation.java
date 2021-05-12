package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import com.jayway.jsonpath.DocumentContext;
import java.util.List;
import java.util.stream.Collectors;
import net.minidev.json.JSONArray;
import org.icann.rdapconformance.validator.schema.JsonPointers;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public abstract class NoticesValidation extends ProfileJsonValidation {

  final int code;
  private final String title;
  private final String description;
  private final String href;
  private final RDAPQueryType queryType;

  public NoticesValidation(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType,
      String title, String description, String href, int code) {
    super(rdapResponse, results);
    this.queryType = queryType;
    this.title = title;
    this.description = description;
    this.href = href;
    this.code = code;
  }

  @Override
  protected boolean doValidate() {
    DocumentContext jpath = getJPath();
    String path = String.format(
        "$..notices[?(@.title == '%s' && @.description contains '%s')].links[?(@.href == '%s')]",
        title, description, href);
    JSONArray matchingNode = jpath.read(path);
    if (matchingNode.isEmpty()) {
      List<String> noticesPaths = jpath.read("$..notices");
      results.add(RDAPValidationResult.builder()
          .code(code)
          .value(getResultValue(noticesPaths.stream()
              .map(JsonPointers::fromJpath)
              .collect(Collectors.toSet())))
          .message(String.format("The notice for %s was not found.", href))
          .build());
      return false;
    }
    return true;
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
