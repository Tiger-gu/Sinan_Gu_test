package com.distributed.Storage;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.distributed.Coordination.CoordinationService;
import com.distributed.models.WsMessage;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Service
public class StorageService {

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;
    
    @Autowired
    CoordinationService coordinator;
    
    Cache<String, String> storage;

    public void put(String key, String value) {
        this.storage.put(key, value);
    }

    public String get(String key, Boolean isMaster) {
        String result = this.storage.getIfPresent(key);
        // if the node is a slave and this node cannot find the 
        // value assosiated with that key, then take advantage of
        // the established websocket channel and send a help message.
        if (result == null && !isMaster) {
            WsMessage msg = new WsMessage("get-help", key);
            try {
                this.simpMessagingTemplate.convertAndSend(
                    "/topic/"+ coordinator.getMasterId() + "/help", msg);
            } catch (Exception e) {}
        }
        return result;
    }

    public void delete(String key) {
        this.storage.invalidate(key);
    }

    public StorageService() {
        // When the maximum size is set, the Google Guava cache library
        // will use LRU as the cache evicion strategy.
        this.storage = CacheBuilder.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(Duration.ofMinutes(1))
        .build();
    }
}
