package com.distributed.Coordination;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;


public class MasterElection implements Watcher{
    private static final String ELECTION_ZNODE = "/election";
    private ZooKeeper zookeeper;
    private String currentZnodeName;
    private final OnElectionCallback onElectionCallback;

    // Each node will loop through the list of candidates submitted to ZooKeeper.
    // The first node in the list will be recognized as the master.
    // Other nodes will automatically take over slave roles. A slave node will be
    // promoted to the master role if the master node is down.
    protected void reelectMaster() throws KeeperException, InterruptedException{
        List<String> children;
        String predecessorZnodeName;
        String smallestChild;
        int predecessorIndex;
        Stat predecessorStat = null; 

        while(predecessorStat == null) {
            children = zookeeper.getChildren(ELECTION_ZNODE, this);
            Collections.sort(children);
            smallestChild = children.get(0);
            if (smallestChild.equals(currentZnodeName)) {
                System.out.println("I am the master");
                onElectionCallback.onElectedToBeLeader();
                return;
            } else {
                // Every slave node will keep a watchful eye on its left
                // node in the list. Once that node is down/broken,
                // the slave node will immediately call for an reelection
                // Therefore, when any node especially the master node is down,
                // a reelection will be held and one of the slave nodes will be
                // promoted to the master role. 

                // In this way, there will always be a master node.
                System.out.println("I am the slave");
                predecessorIndex = Collections.binarySearch(children, currentZnodeName) - 1;
                predecessorZnodeName = children.get(predecessorIndex);
                predecessorStat = zookeeper.exists(ELECTION_ZNODE+"/"+predecessorZnodeName, this);
            }
        }
        onElectionCallback.onWorker();
    }


    private void  createElectionZnode() {
        // Don't need to wory about race conditions, since ZooKeeper server only allows
        // one create method call for a specific znode path. Duplicate calls will result in
        // KeeperException
        try {
            if (zookeeper.exists(ELECTION_ZNODE, false) == null) {
                zookeeper.create(ELECTION_ZNODE, new byte[]{}, 
                                 ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    protected void updateZookeeper(ZooKeeper zookeeper) {
        this.zookeeper = zookeeper;
    }

    // Submit the candidacy for the role assignment (master or slave)
    protected void volunteerForLeadership() throws KeeperException, 
                                                   InterruptedException{
        String znodePrefix = ELECTION_ZNODE + "/c_";
        String znodeFullPath = zookeeper.create(znodePrefix, new byte[] {}, 
                                                ZooDefs.Ids.OPEN_ACL_UNSAFE, 
                                                CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("znode name" + znodeFullPath);
        this.currentZnodeName = znodeFullPath.replace(ELECTION_ZNODE+"/", "");
    }

    @Override
    public void process(WatchedEvent e) {
        switch (e.getType()) {
            case NodeDeleted:
                // After any node is detected down, hold an election.
                try {
                    reelectMaster();
                } catch (Exception exception) {
                    throw new RuntimeException(exception.getMessage());
                }
                break;
            default:
        }
    }
    
    public MasterElection(ZooKeeper zooKeeper, OnElectionCallback onElectionCallback) {
        this.zookeeper = zooKeeper;
        this.onElectionCallback = onElectionCallback;
        this.createElectionZnode();
    }
}