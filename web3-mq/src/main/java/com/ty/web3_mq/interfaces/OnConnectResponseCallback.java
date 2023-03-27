package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.websocket.bean.BridgeMessageMetadata;

public interface OnConnectResponseCallback {
    void onApprove(BridgeMessageMetadata walletInfo,String address);
    void onReject();
}
