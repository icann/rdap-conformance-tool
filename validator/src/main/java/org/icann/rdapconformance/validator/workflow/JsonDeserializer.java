package org.icann.rdapconformance.validator.workflow;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JsonDeserializer<T> implements Deserializer<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Class<T> type;

    public JsonDeserializer(Class<T> type) {
        this.type = type;
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public T deserialize(File file) throws IOException {
        return objectMapper.readValue(file, type);
    }
}
