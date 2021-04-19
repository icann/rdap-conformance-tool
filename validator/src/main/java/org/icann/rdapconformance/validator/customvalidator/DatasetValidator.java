package org.icann.rdapconformance.validator.customvalidator;

import java.util.Optional;
import org.everit.json.schema.FormatValidator;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.DatasetValidatorModel;

public abstract class DatasetValidator implements FormatValidator {

  protected final DatasetValidatorModel datasetValidatorModel;
  private final String formatName;

  public DatasetValidator(
      DatasetValidatorModel datasetValidatorModel,
      String formatName) {
    this.datasetValidatorModel = datasetValidatorModel;
    this.formatName = formatName;
  }

  public DatasetValidatorModel getDatasetValidatorModel() {
    return datasetValidatorModel;
  }

  @Override
  public Optional<String> validate(String s) {
    if (datasetValidatorModel.isInvalid(s)) {
      return Optional.of(getErrorMsg());
    }
    return Optional.empty();
  }

  protected abstract String getErrorMsg();

  @Override
  public String formatName() {
    return formatName;
  }
}
