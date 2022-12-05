package com.ty.web3_mq.interfaces;

import web3mq.Message;

public interface OnNotificationMessageEvent {
    void onNotificationMessage(Message.Web3MQMessageListResponse response);
}
