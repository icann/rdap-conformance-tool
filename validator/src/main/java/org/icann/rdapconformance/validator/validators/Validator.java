package org.icann.rdapconformance.validator.validators;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.icann.rdapconformance.validator.RDAPDeserializer;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.icann.rdapconformance.validator.models.RDAPValidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Validator<T extends RDAPValidate> {

  private static final Logger logger = LoggerFactory.getLogger(Validator.class);

  protected final RDAPValidatorContext context;
  private final Class<T> clazz;

  public Validator(RDAPValidatorContext context, Class<T> clazz) {
    this.context = context;
    this.clazz = clazz;
  }

  public abstract List<String> getAuthorizedKeys();

  public abstract int getInvalidJsonErrorCode();

  public abstract int getInvalidKeysErrorCode();

  protected abstract int getDuplicateKeyErrorCode();

  /**
   * Perform validation of the RDAP content.
   */
  public boolean validate(String rdapContent) {
    Map<String, Object> rawRdap;
    T rdapObject;
    RDAPDeserializer deserializer = this.context.getDeserializer();
    try {
      logger.debug("Deserializing {} object", clazz.getSimpleName());
      rawRdap = deserializer.deserialize(rdapContent, Map.class);
      rdapObject = deserializer.deserialize(rdapContent, clazz);
    } catch (JsonProcessingException e) {
      if (!checkDuplicateFields(e)) {
        return false;
      }

      return checkJsonValidity(rdapContent, e);
    }

    boolean result = checkInvalidKeys(rawRdap);
    result &= rdapObject.validate();
    return result;
  }

  /**
   * The name of every name/value pairs shall be any of {@link #getAuthorizedKeys()}.
   */
  private boolean checkInvalidKeys(Map<String, Object> rawRdap) {
    boolean result = true;
    for (var entry : rawRdap.entrySet()) {
      if (!getAuthorizedKeys().contains(entry.getKey())) {
        logger.error("Unrecognized key {} in RDAP response", entry.getKey());
        this.context.addResult(RDAPValidationResult.builder()
            .code(getInvalidKeysErrorCode())
            .value(entry.getKey() + "/" + entry.getValue())
            .message("The name in the name/value pair is not of: " + String.join(", ",
                getAuthorizedKeys()) + ".")
            .build());
        result = false;
      }
    }
    return result;
  }

  /**
   * The {@link #clazz} data structure must be a syntactically valid JSON object.
   */
  private boolean checkJsonValidity(String rdapContent, JsonProcessingException e) {
    logger.error("Failed to deserialize RDAP response", e);
    this.context.addResult(RDAPValidationResult.builder()
        .code(getInvalidJsonErrorCode())
        .value(rdapContent)
        .message("The " + clazz.getSimpleName() + " structure is not syntactically valid.")
        .build());
    return false;
  }

  /**
   * The JSON name/values of {@link #getAuthorizedKeys()} shall appear only once.
   */
  private boolean checkDuplicateFields(JsonProcessingException e) {
    if (e.getMessage().startsWith("Duplicate field")) {
      String keyVal = "";
      String key = "ERROR: Failed to retrieve key";
      try {
        JsonParseException exc = ((JsonParseException) e);
        key = exc.getProcessor().getCurrentName();
        String value = exc.getProcessor().getText();
        keyVal = key + "/" + value;
      } catch (IOException ex) {
        logger.error("Cannot retrieve key, value from duplicate field error", ex);
      }
      logger.error("Duplicated key '{}' in RDAP response", key);

      this.context.addResult(RDAPValidationResult.builder()
          .code(getDuplicateKeyErrorCode())
          .value(keyVal)
          .message(
              "The name in the name/value pair of a " + clazz.getSimpleName()
                  + " structure was found more than "
                  + "once.")
          .build());
      return false;
    }
    return true;
  }
}
