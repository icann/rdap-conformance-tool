package org.icann.rdapconformance.validator.models.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.icann.rdapconformance.validator.validators.JsonFieldValidator;
import org.icann.rdapconformance.validator.validators.field.FieldValidation;
import org.icann.rdapconformance.validator.validators.field.FieldValidationBasicType;
import org.icann.rdapconformance.validator.validators.field.FieldValidationEnum;
import org.icann.rdapconformance.validator.validators.field.FieldValidationExistence;
import org.icann.rdapconformance.validator.validators.field.FieldValidationJsonArray;

public class NoticeAndRemark extends Lang {

  // title -- string representing the title of the object
  @JsonProperty
  Object title;

  // type -- string denoting a registered type of remark or notice
  // TODO enum?
  @JsonProperty
  Object type;

  // description -- an array of strings for the purposes of conveying any descriptive text
  @JsonProperty
  Object description;

  // links --
  @JsonProperty
  List<Link> links;

  public NoticeAndRemark() {
  }

  public NoticeAndRemark(RDAPValidatorContext context) {
    this.context = context;
  }

  @Override
  public boolean validate() {
    // 1, 2.1, 2.2 validation are done in StdRdapNoticesRemarksValidation
    Schema schema = getSchema("rdap_notice.json");
    JsonFieldValidator validator = new JsonFieldValidator(schema, this, context);
    // 2.3. If the JSON name title exists, the value shall be a JSON string data type.
    FieldValidation validation2_3 = new FieldValidationBasicType("title", -10703);
    boolean result = validator.validateField(validation2_3);
    // 2.5. If the JSON name type exists, the value shall be a JSON string data type.
    FieldValidation validation2_5 = new FieldValidationBasicType("type", -10705);
    result &= validator.validateField(validation2_5);
    // 2.6. If the JSON name type exists, the value shall be included in the RDAPJSONValues with
    // Type="notice and remark type".
    FieldValidation validation2_6 = new FieldValidationEnum("type", -10706);
    result &= validator.validateField(validation2_6);
    // 2.7. The JSON name description shall exist
    FieldValidation validation2_7 = new FieldValidationExistence("description", -10707);
    result &= validator.validateField(validation2_7);
    // 2.8. The description data structure must be a syntactically valid JSON array.
    FieldValidation validation2_8 = new FieldValidationJsonArray("description", -10708);
    result &= validator.validateField(validation2_8);
    // 2.9. Every value of the JSON array of the description data structure shall be a JSON  string data type.
    FieldValidation validation2_9 = new FieldValidationBasicType("description", -10709);
    result &= validator.validateField(validation2_9);

    return result;
  }
}
