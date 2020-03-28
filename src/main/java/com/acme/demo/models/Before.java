package com.acme.demo.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Before {
    private String status;
    private int version;

    @JsonCreator
    public Before(
            @JsonProperty("status") String status,
            @JsonProperty("_version") int version) {
        this.status = status;
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public int getVersion() { return version; }
}
