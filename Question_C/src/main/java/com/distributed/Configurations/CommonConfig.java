package com.distributed.Configurations;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.distributed.WsClient.StompClient;

@Configuration
public class CommonConfig {
    // Each server/node has its own unique id.
    // When a node becomes the master node, its nodeId
    // will serve as its master Id.
    @Bean
    public String nodeId() {
        return UUID.randomUUID().toString();                      
    }

    @Bean
    public ConcurrentHashMap<String, StompClient> concurrentHashMap() {
        return new ConcurrentHashMap<>();
    }
}
