package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.MessagesBean;

public interface GetMessageHistoryCallback {
    void onSuccess(MessagesBean messagesBean);
    void onFail(String error);
}
