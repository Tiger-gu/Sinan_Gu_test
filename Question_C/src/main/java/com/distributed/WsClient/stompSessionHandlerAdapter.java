package com.distributed.WsClient;

import java.lang.reflect.Type;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import com.distributed.Storage.StorageService;
import com.distributed.models.CacheEntry;
import com.distributed.models.WsMessage;
import com.distributed.models.WsNotification;

import io.micrometer.common.lang.Nullable;

public class stompSessionHandlerAdapter extends StompSessionHandlerAdapter {
    private StorageService cache;
    private WsNotification notification;
    private StompSession session = null;

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        if (this.session == null) {
            this.session = session;
            session.subscribe("/topic/"+ notification.getMasterId() + "/put", this);
            session.subscribe("/topic/"+ notification.getMasterId() + "/delete", this);
            session.subscribe("/topic/"+ notification.getMasterId() + "/help", this);
            session.subscribe("/topic/"+ notification.getMasterId() + "/pong", this);
        } else {
            this.session.send("/app/sync/"+notification.getAction(), notification);
        }
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        WsMessage msg = (WsMessage) payload;
        if (msg.getType().equals("put-confirmation")) {  
            if (msg.getContent().equals("Fail")) System.err.print("Sync: PUT failed");
        } else if (msg.getType().equals("delete-confirmation")) {
            if (!msg.getContent().equals("Fail")) System.err.print("Sync: DELETE failed");
        } else if (msg.getType().equals("get-help")) {
            CacheEntry entry = new CacheEntry(msg.getContent(), cache.get(msg.getContent(), true));
            session.send("/app/sync/help", entry);
        } else if (msg.getType().equals("pong")) {
            //System.out.println("ping success!");
        }
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        System.err.println(exception.getMessage());
        return;
    }

    @Override
    public void handleException(StompSession session, @Nullable StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        System.err.println(exception.getMessage());
    }

    @Override 
    public Type getPayloadType(StompHeaders headers) {
        return WsMessage.class;
    }

    public stompSessionHandlerAdapter(WsNotification notification, StorageService cache) {
        this.notification = notification;
        this.cache = cache;
    }
}
