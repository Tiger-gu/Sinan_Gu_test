package com.distributed.WsClient;

import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.distributed.Storage.StorageService;
import com.distributed.models.WsNotification;

public class StompClient {

    private StompSession stompSession;
    private WebSocketStompClient stompClient;

    
    public synchronized void connect(String url, WsNotification notification, StorageService cache) {
        try {
            if (this.stompSession == null || (!stompSession.isConnected())) {
                this.stompSession = stompClient.connectAsync(
                    url, new stompSessionHandlerAdapter(notification, cache)).get();
            } else {
                this.stompSession.send("/app/sync/" + notification.getAction(), notification);
            }
        } catch(Exception e) {
            System.err.println("ws Connection failed: " + e.getMessage());
        }
    }

    public StompClient() {
        this.stompSession = null;
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        transports.add(new RestTemplateXhrTransport());
        this.stompClient = new WebSocketStompClient(new SockJsClient(transports));
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }
}
