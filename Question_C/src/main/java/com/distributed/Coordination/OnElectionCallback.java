package com.distributed.Coordination;

public interface OnElectionCallback {
    void onElectedToBeLeader();

    void onWorker();
}
