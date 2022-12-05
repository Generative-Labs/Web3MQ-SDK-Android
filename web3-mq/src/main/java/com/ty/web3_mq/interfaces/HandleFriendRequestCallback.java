package com.ty.web3_mq.interfaces;

public interface HandleFriendRequestCallback {
    void onSuccess();
    void onFail(String error);
}
