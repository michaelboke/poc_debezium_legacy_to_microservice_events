package com.acme.demo.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateOrder<T extends Serializable> implements Deserializer<T>, EventNameResolver {

    @Override
    public T deserialize(String s, byte[] bytes) {
        return null;
    }

    @Override
    public T deserialize(String topic, Headers headers, byte[] data) {
        return null;
    }

    private int orderId;

    @JsonCreator
    public CreateOrder(
            @JsonProperty("orderId") int orderId ) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }

    @Override
    public String EventName() {
        return "Demo\\Events\\CreateOrder";
    }
}
