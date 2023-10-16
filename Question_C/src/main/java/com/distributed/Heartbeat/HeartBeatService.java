package com.distributed.Heartbeat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.distributed.Broadcast.BroadcastService;
import com.distributed.Coordination.CoordinationService;
import com.distributed.Storage.StorageService;
import com.distributed.models.WsNotification;

@Service
public class HeartBeatService {

    @Autowired
    private CoordinationService coordinator;

    @Autowired
    private String nodeId;

    @Autowired
    private BroadcastService broadcastService;

    @Autowired
    private StorageService cache;

    
    @Scheduled(fixedRate = 1000)
    public void followUp() {
        try {
            if (coordinator.getMasterId().equals(nodeId)) {
                WsNotification heartbeat = new WsNotification("ping", null, null, nodeId);
                this.broadcastService.broadcast(heartbeat, coordinator.getAllSlavesAddresses(), cache);

            } 
        } catch (Exception e) {

        }
    }     
}
