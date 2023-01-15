package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.websocket.bean.BridgeMessageProposer;

public interface OnSignRequestMessageCallback {
    void onSignRequestMessage(BridgeMessageProposer proposer,String address, String sign_raw);
}
