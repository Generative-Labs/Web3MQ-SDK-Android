package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.ChatsBean;

public interface GetChatsCallback {
    void onSuccess(ChatsBean chatsBean);
    void onFail(String error);
}
