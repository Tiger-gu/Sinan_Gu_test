package com.distributed.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CacheEntry {
    private String key;
    private String value;
    public CacheEntry(@JsonProperty("key")String key, @JsonProperty("value") String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    } 

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
