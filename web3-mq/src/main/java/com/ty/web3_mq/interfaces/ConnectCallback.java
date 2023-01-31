package com.ty.web3_mq.interfaces;

public interface ConnectCallback {
    void onSuccess();
    void onFail(String error);
    void alreadyConnected();
}
