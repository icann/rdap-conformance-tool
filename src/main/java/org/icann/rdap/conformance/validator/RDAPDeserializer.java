package org.icann.rdap.conformance.validator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPDeserializer {

  private final Logger logger = LoggerFactory.getLogger(RDAPDeserializer.class);
  private final ObjectMapper mapper;

  public RDAPDeserializer() {
    this.mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .addHandler(new RDAPDeserializationProblemHandler())
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  public <T> T deserialize(String json, Class<T> objectClass) throws JsonProcessingException {
    return this.mapper.readValue(json, objectClass);
  }

  private static class RDAPDeserializationProblemHandler extends DeserializationProblemHandler {

    @Override
    public Object handleUnexpectedToken(DeserializationContext ctxt, JavaType targetType,
        JsonToken t, JsonParser p, String failureMsg) throws IOException {
      // TODO add relevant error code
      // continue on unexpected object
      return null;
    }
  }
}
