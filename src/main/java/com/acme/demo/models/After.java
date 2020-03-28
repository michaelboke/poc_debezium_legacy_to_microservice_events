package com.acme.demo.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class After extends Before {

    @JsonCreator
    public After(
            @JsonProperty("status") String status,
            @JsonProperty("_version") int version) {
        super(status, version);
    }
}
