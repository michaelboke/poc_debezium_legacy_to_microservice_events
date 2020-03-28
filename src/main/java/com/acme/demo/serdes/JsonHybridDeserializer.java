package com.acme.demo.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonHybridDeserializer<T> implements Deserializer<T> {

    public static final String DBZ_CDC_EVENT_PAYLOAD_FIELD = "payload";

    private final static ObjectMapper objectMapper = new ObjectMapper();

    private Class<T> clazz;

    @SuppressWarnings("unchecked")
    @Override
    public void configure(Map<String, ?> props, boolean isKey) {
        clazz = (Class<T>) props.get("serializedClass");
    }

    @Override
    public T deserialize(String topic, byte[] bytes) {

        if (bytes == null)
            return null;

        T data;

        //step1: try to directly deserialize expected class
        try {
            data = objectMapper.readValue(bytes, clazz);
        } catch (IOException e1) {
            //step2: try to deserialize from DBZ json event
            try {
                Map temp = (Map) objectMapper.readValue(new String(bytes), Map.class)
                        .get(DBZ_CDC_EVENT_PAYLOAD_FIELD);
                if (temp == null) {
                    temp = new HashMap();
                    //NOTE: we record a delete event in this case
                    //temp.put("_eventType", EventType.DELETE);
                }
                data = objectMapper.readValue(objectMapper.writeValueAsBytes(temp), clazz);
            } catch (IOException e2) {
                throw new SerializationException(e2);
            }
        }

        return data;
    }

    @Override
    public void close() {
    }
}