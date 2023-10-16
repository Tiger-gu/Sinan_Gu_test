package com.distributed.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WsMessage {
    private String type;
    private String content;
    public WsMessage(@JsonProperty("type") String type, @JsonProperty("content") String content) {
        this.type = type;
        this.content = content;
    }

    public String getType() {
        return this.type;
    }

    public String getContent() {
        return this.content;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }
}