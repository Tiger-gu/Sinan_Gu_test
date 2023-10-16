package com.distributed.Coordination;

public class OnElectionAction implements OnElectionCallback{
    private final NodeRegistry serviceRegistry;
    private int port;
    private String nodeId;

    private String getCurrentServerAddress() {
        String currentServerAddress =
            String.format("http://localhost:%d", port);
        return currentServerAddress;
    }

    protected OnElectionAction(NodeRegistry serviceRegistry, int currentServerPort, String nodeId) {
        this.serviceRegistry = serviceRegistry;
        this.port = currentServerPort;
        this.nodeId = nodeId;
    }

    @Override
    public void onElectedToBeLeader() {
        try {
            serviceRegistry.unregisteredFromCluster(nodeId);
            serviceRegistry.registerForUpdates();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public void onWorker() {
        try {
            serviceRegistry.registerToCluster(this.getCurrentServerAddress());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
