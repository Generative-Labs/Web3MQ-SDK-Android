package com.ty.web3_mq.interfaces;

public interface LoginCallback {
    void onSuccess();
    void onFail(String error);
}
