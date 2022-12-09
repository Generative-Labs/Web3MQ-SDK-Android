package com.ty.web3_mq.interfaces;

public interface ChangeMessageStatusRequestCallback {
    void onSuccess();
    void onFail(String error);
}
