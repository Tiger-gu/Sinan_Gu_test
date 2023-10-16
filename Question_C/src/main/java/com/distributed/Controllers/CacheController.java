package com.distributed.Controllers;

import org.springframework.web.bind.annotation.RestController;

import com.distributed.Broadcast.BroadcastService;
import com.distributed.Coordination.CoordinationService;
import com.distributed.Storage.StorageService;
import com.distributed.models.WsNotification;
import com.distributed.models.CacheEntry;

import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class CacheController {
    @Autowired 
    String nodeId;

    @Autowired
    private CoordinationService coordinator;

    @Autowired
    private StorageService cache;

    @Autowired
    private BroadcastService broadcastService;

    // It is the master node's responsibility to handle put requests . 
    // Once the master node finishes adding the new cache entry,
    // it will broadcast the put notification containing the new cache entry
    // to all the slave nodes through websocket. That is to take advantage of
    // the fact that websockets are generally faster than http to achieve
    // real-time writes.
    @PutMapping(value="/put")
    public ResponseEntity<String> put(@RequestBody(required=true) CacheEntry entry) {
        cache.put(entry.getKey(), entry.getValue());

        try {
            this.broadcastService.broadcast(new WsNotification("put", entry.getKey(), entry.getValue(), nodeId),
                                            coordinator.getAllSlavesAddresses(), cache);
        } catch (KeeperException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatusCode.valueOf(500));
        } catch (InterruptedException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatusCode.valueOf(500));
        }
        return new ResponseEntity<String>("success", HttpStatusCode.valueOf(200));
    }

    // The master node and slave nodes will handle get requests.
    // The difference is that if the master node can not find the
    // value assosiated with the key, the master will just return null,
    // whereas if a slave node cannot find the value assosiated with the key,
    // the slave node will return null to the client and clandestinely ask the master
    // through the existing websocket channel whether it knows anything related to the key.
    // Please see the Storage folder
    @GetMapping(value="/get/{key}")
    public ResponseEntity<String> answer(@PathVariable("key") String key) {
        String value;
        String masterId;

        try {
            masterId = coordinator.getMasterId();
        } catch (Exception e) {
            masterId = nodeId;
        }
        // This isMaster boolean indicator indicates whether a node
        // is a master node. If a node is already a master node,
        // the node should just return null if it cannot find the
        // value assosiated with the key.
        if (masterId.equals(this.nodeId)) {
            value = cache.get(key, true);
        } else {
            value = cache.get(key, false);
        }
        return new ResponseEntity<>(value, HttpStatusCode.valueOf(200));
    }

    // It is the master node's responsibility to handle delete requests.
    // Once the master node finishes deleting, similar to cases of PUT requests,
    // it will broadcast the DELETE notification to all its slave nodes.
    @DeleteMapping(value="/delete/{key}")
    public ResponseEntity<String> delete(@PathVariable("key") String key) {
        cache.delete(key);

        try {
            this.broadcastService.broadcast(new WsNotification("delete", key, null, nodeId),
                                            coordinator.getAllSlavesAddresses(), cache);
        } catch (KeeperException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatusCode.valueOf(500));
        } catch (InterruptedException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatusCode.valueOf(500));
        }
        return new ResponseEntity<String>("success", HttpStatusCode.valueOf(200));
    }
}

