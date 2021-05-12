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

public class ResponseValidation2Dot6Dot3 extends ProfileJsonValidation {

  final static String TITLE = "Status Codes";
  final static String DESCRIPTION = "For more information on domain status codes, please visit https://icann.org/epp";
  final static String HREF = "https://icann.org/epp";
  private final RDAPQueryType queryType;

  public ResponseValidation2Dot6Dot3(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType) {
    super(rdapResponse, results);
    this.queryType = queryType;
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_6_3_Validation";
  }

  @Override
  protected boolean doValidate() {
    DocumentContext jpath = getJPath();
    String path = String.format(
        "$..notices[?(@.title == '%s' && @.description contains '%s')].links[?(@.href == '%s')]",
        TITLE, DESCRIPTION, HREF);
    JSONArray matchingNode = jpath.read(path);
    if (matchingNode.isEmpty()) {
      List<String> noticesPaths = jpath.read("$..notices");
      results.add(RDAPValidationResult.builder()
          .code(-46600)
          .value(getResultValue(noticesPaths.stream()
              .map(JsonPointers::fromJpath)
              .collect(Collectors.toList())))
          .message("The notice for https://icann.org/epp was not found.")
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
