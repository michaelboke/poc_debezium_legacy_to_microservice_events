package com.acme.demo.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.acme.demo.models.EventNameResolver;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class JsonPojoSerializer<T> implements Serializer<T> {

    protected final static ObjectMapper ojectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> props, boolean isKey) { }

    @Override
    public byte[] serialize(String topic, Headers headers, T data) {

        headers.add("source", "debezium".getBytes());
        if (data instanceof EventNameResolver) {
            headers.add("event.name", ((EventNameResolver)data).EventName().getBytes());
        }

        return serialize(topic, data);
    }

    @Override
    public byte[] serialize(String topic, T data) {

        if (data == null)
            return null;

        try {
            return ojectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new SerializationException("Error serializing JSON message", e);
        }

    }

    @Override
    public void close() { }

}