package com.ty.web3_mq.interfaces;

public interface SendBridgeMessageCallback {
    void onReceived();
    void onFail();
    void onTimeout();
}
