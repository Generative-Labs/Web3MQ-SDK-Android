package com.ty.web3_mq.interfaces;


import com.ty.web3_mq.websocket.bean.MessageBean;


public interface MessageCallback {
    void onMessage(MessageBean message);
}
