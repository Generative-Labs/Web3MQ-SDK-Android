package com.ty.web3_mq.interfaces;

public interface SubscribeCallback {
    void onSuccess();
    void onFail(String error);
}
