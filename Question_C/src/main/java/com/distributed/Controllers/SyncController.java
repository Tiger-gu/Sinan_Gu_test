package com.distributed.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.distributed.Storage.StorageService;
import com.distributed.models.CacheEntry;
import com.distributed.models.WsMessage;
import com.distributed.models.WsNotification;

@Controller
public class SyncController {

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    // In the Spring Boot, the scope of a bean is by its default Singleton.
    // So this instance of Cache will be the same instance found in the 
    // previous RestController
    @Autowired
    private StorageService cache;
    
    @MessageMapping("/sync/put")
    public void sync_put(@Payload WsNotification notification) {
        WsMessage msg;
        try {
            this.cache.put(notification.getKey(), notification.getValue());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            msg = new WsMessage("put-confirmation", "Fail");
            this.simpMessagingTemplate.convertAndSend("/topic/"+ notification.getMasterId() + "/put", msg);
        }
        // Once a slave node finishes the sync operation the master
        // node asked it to do, it will report back to the master on
        // the status of the operation through the STOMP message broker.
        // "sync-put" is just a topic that the master node actively listens to.
        msg = new WsMessage("put-confirmation","Succeed");
        this.simpMessagingTemplate.convertAndSend("/topic/"+ notification.getMasterId() + "/put", msg);
    }

    @MessageMapping("/sync/delete")
    public void sync_delete(@Payload WsNotification notification) {
        WsMessage msg;
        try {
            this.cache.delete(notification.getKey());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            msg = new WsMessage("delete-confirmation", "Fail");
            this.simpMessagingTemplate.convertAndSend("/topic/"+ notification.getMasterId() + "/delete", msg);
        }
        // Similarly, a slave node reports back to the master node on
        // how the sync operation goes.
        msg = new WsMessage("delete-confirmation", "Succeed");
        this.simpMessagingTemplate.convertAndSend("/topic/"+ notification.getMasterId() + "/delete", msg);
    }

    @MessageMapping("/sync/help")
    public void sync_delete(@Payload CacheEntry entry) {
        this.cache.put(entry.getKey(), entry.getValue());
    }

    @MessageMapping("/sync/ping")
    public void pong(@Payload WsNotification notification) {
        WsMessage msg = new WsMessage("pong", "ok");
        this.simpMessagingTemplate.convertAndSend("/topic/"+ notification.getMasterId() + "/pong", msg);
    }
}
