package com.ty.web3_mq.interfaces;

public interface OnSignResponseMessageCallback {
    void onApprove(String signature);
    void onReject();
}