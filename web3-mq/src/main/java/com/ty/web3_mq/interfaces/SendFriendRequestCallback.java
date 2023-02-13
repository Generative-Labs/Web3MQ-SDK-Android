package com.ty.web3_mq.interfaces;


public interface SendFriendRequestCallback {
    void onSuccess();
    void onFail(String error);
}
