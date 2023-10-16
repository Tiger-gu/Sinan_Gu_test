package com.distributed.Coordination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class NodeRegistry implements Watcher{
    private static final String REGISTRY_ZNODE = "/node_registry";
    private ZooKeeper zookeeper;
    private String currentZnode = null;
    private List<String> allSlavesAddresses = null;

    private void  createServiceRegistryZnode() {
        // Don't need to wory about race conditions, since ZooKeeper server only allows
        // one create method call for a specific znode path. Duplicate calls will result in
        // KeeperException
        try {
            if (zookeeper.exists(REGISTRY_ZNODE, false) == null) {
                zookeeper.create(REGISTRY_ZNODE, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }


    }
    
    // Obtain the latest list of slaves addresses from the zookeeper. 
    private synchronized void updateSlavesAddresses() throws KeeperException, InterruptedException {
        List<String> workerZnodes = zookeeper.getChildren(REGISTRY_ZNODE, this);
        List<String> addresses = new ArrayList<>(workerZnodes.size());
        for (String workerZnode : workerZnodes) {
            String workerZnodeFullPath = REGISTRY_ZNODE + "/" + workerZnode;
            Stat stat = zookeeper.exists(workerZnodeFullPath, false);
            // there is nothing we can do about his race condition
            if (stat == null) {
                continue;
            }
            byte[] addressBytes = zookeeper.getData(workerZnodeFullPath, false, stat);
            String address = new String(addressBytes);
            addresses.add(address);
        }
        this.allSlavesAddresses = Collections.unmodifiableList(addresses);
        System.out.println("Watching all The cluster addresses: " + this.allSlavesAddresses.toString());
    }

    protected void updateZookeeper(ZooKeeper zookeeper) {
        this.zookeeper = zookeeper;
    }

    protected synchronized List<String> getAllSlavesAddresses() throws KeeperException, InterruptedException{
        if (this.allSlavesAddresses == null) {
            updateSlavesAddresses();
        }
        return this.allSlavesAddresses;
    }

    protected String getMasterId() throws KeeperException, InterruptedException {
        Stat stat = zookeeper.exists(REGISTRY_ZNODE, false);
        byte[] masterIdBytes = zookeeper.getData(REGISTRY_ZNODE, false, stat);
        return new String(masterIdBytes);
    }

    // In case that a node becomes a slave, it adds itself to the list of slave nodes
    // the master will watch over.
    protected void registerToCluster(String metainfo) throws KeeperException, InterruptedException{
         this.currentZnode = zookeeper.create(REGISTRY_ZNODE + "/n_", metainfo.getBytes(), 
                                              ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
         System.out.println("Registered to service reistry");                             
    }

    // In case that a node becomes a master, it removes itself from the list of slave nodes
    // the master watches over. Also, it stores its own address to the registry znode.
    protected void unregisteredFromCluster(String masterId) throws KeeperException, InterruptedException{
        if (currentZnode != null && zookeeper.exists(currentZnode, false) != null) {
            zookeeper.delete(currentZnode, -1);
        }
        zookeeper.setData(REGISTRY_ZNODE, masterId.getBytes(), -1);
    }
        
    // for the master to register for updates on all slave nodes'addresses
    protected void registerForUpdates() {
        try {
            updateSlavesAddresses();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    protected NodeRegistry(ZooKeeper zooKeeper) {
        this.zookeeper = zooKeeper;
        this.allSlavesAddresses = null;
        createServiceRegistryZnode();
    }

    @Override
    public void process(WatchedEvent e) {
        // if there is any change to the list of slave nodes.
        // re-register to get the latest slaves addresses.
        registerForUpdates();
    }
}
