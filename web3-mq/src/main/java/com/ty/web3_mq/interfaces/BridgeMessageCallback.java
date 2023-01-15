package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.websocket.bean.BridgeMessage;

public interface BridgeMessageCallback {
    void onBridgeMessage(String comeFrom, BridgeMessage bridgeMessage);
}