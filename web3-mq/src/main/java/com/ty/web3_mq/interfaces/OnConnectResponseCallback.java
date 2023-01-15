package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.websocket.bean.BridgeMessageWalletInfo;

public interface OnConnectResponseCallback {
    void onApprove(BridgeMessageWalletInfo walletInfo);
    void onReject();
}
