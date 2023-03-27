package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.websocket.bean.sign.Participant;

public interface OnSignRequestMessageCallback {
    void onSignRequestMessage(String id,Participant participant, String address, String sign_raw);
}
