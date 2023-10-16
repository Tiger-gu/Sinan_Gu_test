package com.distributed.Broadcast;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.distributed.Storage.StorageService;
import com.distributed.WsClient.StompClient;
import com.distributed.models.WsNotification;

@Service
public class BroadcastService {
    // The SpringBoot is multi-threaded.
    // That is why I choose the data type ConcurrentHashmap
    // instead of the standard Hashmap.
    @Autowired
    private ConcurrentHashMap<String, StompClient> sessionsStore;

    // The master node can use this function to broadcast notifictions
    // and related info to all the slave nodes. 
    public void broadcast(WsNotification notification, List<String> slaveAddresses, 
                          StorageService cache) throws KeeperException, InterruptedException{
        // Loop through the slave nodes addresses and broadcast related info
        // through websocket.
        for (String addr : slaveAddresses) {
            addr = addr + "/ws-broadcast";
            // store this mapping: [slave node's address <---> the websocket stomp client]
            // into the concurrent hashmap. So, next time, when communicaion is needed,
            // a new stomp client needn't be created.
            StompClient result = this.sessionsStore.putIfAbsent(addr, new StompClient());
            if (result == null) result = this.sessionsStore.get(addr);
            result.connect(addr, notification, cache);
        }
    }
}
