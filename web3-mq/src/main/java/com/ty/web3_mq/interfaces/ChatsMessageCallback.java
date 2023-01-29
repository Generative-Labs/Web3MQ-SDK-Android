package com.ty.web3_mq.interfaces;


import com.ty.web3_mq.websocket.bean.MessageBean;

import web3mq.Message;


public interface ChatsMessageCallback {
    void onMessage(Message.Web3MQMessage message);
}
