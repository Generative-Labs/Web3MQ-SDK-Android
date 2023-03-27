package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.websocket.bean.BridgeMessageMetadata;

public interface WalletConnectRequestCallback {
    void onConnectWalletSuccess(BridgeMessageMetadata walletInfo);
}
