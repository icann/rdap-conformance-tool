package org.icann.rdapconformance.validator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPDeserializer {

  private static final Logger logger = LoggerFactory.getLogger(RDAPDeserializer.class);
  private final ObjectMapper mapper;

  public RDAPDeserializer(RDAPValidatorContext context) {
    InjectableValues.Std injectableValues = new InjectableValues.Std();
    injectableValues.addValue("context", context);
    this.mapper = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
//        .addHandler(new RDAPDeserializationProblemHandler())
        .setInjectableValues(injectableValues)
        .registerModule(new JavaTimeModule());
  }

  public <T> T deserialize(String json, Class<T> objectClass) throws JsonProcessingException {
    return this.mapper.readValue(json, objectClass);
  }

//  private static class RDAPDeserializationProblemHandler extends DeserializationProblemHandler {
//
//    @Override
//    public Object handleUnexpectedToken(DeserializationContext ctxt, JavaType targetType,
//        JsonToken t, JsonParser p, String failureMsg) throws IOException {
//      // TODO add relevant error code
//      // continue on unexpected object
//      return null;
//    }
//  }
}
