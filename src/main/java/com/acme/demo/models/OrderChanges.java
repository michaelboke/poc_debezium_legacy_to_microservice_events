package com.acme.demo.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({"source", "ts_ms"})
public class OrderChanges {
    private String operation;
    private Before before;
    private After after;

    @JsonCreator
    public OrderChanges(
            @JsonProperty("op") String operation,
            @JsonProperty("before") Before before,
            @JsonProperty("after") After after) {
        this.operation = operation;
        this.before = before;
        this.after = after;
    }

    public String getOperation() {
        return operation;
    }

    public Before getBefore() {
        return before;
    }

    public After getAfter() {
        return after;
    }

    public boolean isCreated() {
        return before == null && after != null &&
                after.getStatus().equals("open");
    }

    public boolean isCompleted() {
        return before != null && after != null &&
                before.getStatus().equals("open") &&
                after.getStatus().equals("closed");
    }

    public boolean isDomainChange() {
        return
                //new record always start at version 1, if raw db insert it would be 0
                (before == null && after != null && after.getVersion() == 1)
                ||
                //version will increment on domain changes
                (before != null && after != null && after.getVersion() > before.getVersion() );
    }
}
