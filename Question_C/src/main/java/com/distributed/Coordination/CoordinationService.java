package com.distributed.Coordination;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

// This class implements the zookeeper watcher interface
@Service
public class CoordinationService implements Watcher{
    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private ZooKeeper zookeeper = null;
    private NodeRegistry serviceRegistry = null;
    private MasterElection master_elector = null;
    private int currentServerPort;
    private String nodeId;

    private void setUp() throws IOException, KeeperException, InterruptedException{
        // This not only create a connection to the zookeeper server, but it also
        // registers a watcher on the zookeeper. When this node is connected to the
        // zookeeper, another event thread spawned by the zookeeper will call the
        // the overriden process method.
        this.zookeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
        this.serviceRegistry = new NodeRegistry(this.zookeeper);
        OnElectionAction onElectionAcion = new OnElectionAction(this.serviceRegistry, currentServerPort, nodeId);
        this.master_elector = new MasterElection(this.zookeeper, onElectionAcion);
        this.master_elector.volunteerForLeadership();
        this.master_elector.reelectMaster();
    }

    public String getMasterId() throws KeeperException, InterruptedException {
        return this.serviceRegistry.getMasterId();
    }

    public synchronized List<String> getAllSlavesAddresses() throws KeeperException, InterruptedException{
        return this.serviceRegistry.getAllSlavesAddresses();
    }

    @Override
    public void process(WatchedEvent e) {
        switch (e.getType()) {
            case None:
                if (e.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Successfully (re)connected to zookeeper");
                }
            default:
        }
    }
    
    @Autowired
    public CoordinationService(@Value("${server.port}") int port, String nodeId) {
        try {
            this.currentServerPort = port;
            this.nodeId = nodeId;
            this.setUp();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}