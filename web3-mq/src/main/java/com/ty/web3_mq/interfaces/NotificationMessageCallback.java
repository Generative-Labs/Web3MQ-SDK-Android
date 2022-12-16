package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.NotificationBean;

import java.util.ArrayList;

import web3mq.Message;

public interface NotificationMessageCallback {
    void onNotificationMessage(ArrayList<NotificationBean> response);
}
