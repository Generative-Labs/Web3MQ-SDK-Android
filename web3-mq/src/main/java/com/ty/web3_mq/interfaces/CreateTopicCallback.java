package com.ty.web3_mq.interfaces;

public interface CreateTopicCallback {
    void onSuccess();
    void onFail(String error);
}
