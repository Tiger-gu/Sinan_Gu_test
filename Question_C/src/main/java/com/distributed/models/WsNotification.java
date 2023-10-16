package com.distributed.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WsNotification {
    private String action;
    private String key;
    private String value;
    private String masterId;

    public WsNotification(@JsonProperty("action") String action, 
                          @JsonProperty("key") String key,
                          @JsonProperty("value") String value, 
                          @JsonProperty("masterId") String masterId) {
        this.action = action;
        this.key = key;
        this.value = value;
        this.masterId = masterId;
    }

    public String getAction() {
        return this.action;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public String getMasterId() {
        return this.masterId;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setKey(String key) {
        this.key = key;
    }   

    public void setValue(String value) {
        this.value = value;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }  
}
