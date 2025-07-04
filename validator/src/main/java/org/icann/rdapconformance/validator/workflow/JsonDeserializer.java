package org.icann.rdapconformance.validator.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JsonDeserializer<T> implements Deserializer<T> {
    private final ObjectMapper objectMapper;
    private final Class<T> type;

    public JsonDeserializer(Class<T> type) {
        this.type = type;
        // Use the shared ObjectMapper for better performance
        this.objectMapper = JsonMapperUtil.getSharedMapper();
    }

    @Override
    public T deserialize(File file) throws IOException {
        return objectMapper.readValue(file, type);
    }
}
