package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static com.jayway.jsonpath.JsonPath.using;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Option;
import java.util.List;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.schema.JsonPointers;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class Validation1Dot14 implements TigValidation {

  private final String rdapResponse;
  private final RDAPDatasetService datasetService;
  private final RDAPValidatorResults results;

  public Validation1Dot14(String rdapResponse,
      RDAPDatasetService datasetService,
      RDAPValidatorResults results) {
    this.rdapResponse = rdapResponse;
    this.datasetService = datasetService;
    this.results = results;
  }

  @Override
  public boolean validate() {
    // maybe pull up jpath parsing to avoid redoing it each time...
    Configuration jsonPathConfig = Configuration.defaultConfiguration()
        .addOptions(Option.AS_PATH_LIST)
        .addOptions(Option.SUPPRESS_EXCEPTIONS);
    DocumentContext jpath = using(jsonPathConfig).parse(rdapResponse);
    List<String> rdapConformances = jpath.read("$..rdapConformance");

    // using a schema to check if something is in an array is maybe an overkill
    // maybe replace it with a simple check sometime...
    Schema schema = SchemaValidator.getSchema(
        "tigSection_1_14_Validation.json",
        "json-schema/profile/tig_section/",
        getClass().getClassLoader(),
        datasetService);

    for (String rdapConformance : rdapConformances) {
      String jsonPointer = JsonPointers.fromJpath(rdapConformance);
      Object value = new JSONObject(rdapResponse).query(jsonPointer);
      try {
        schema.validate(value);
      } catch (ValidationException e) {
        results.add(RDAPValidationResult.builder()
            .code(-20600)
            .value(jsonPointer + ":" + value)
            .message(
                "The RDAP Conformance data structure does not include icann_rdap_technical_implementation_guide_0. See section 1.14 of the RDAP_Technical_Implementation_Guide_2_1.")
            .build());
        return false;
      }
    }

    return true;
  }
}
