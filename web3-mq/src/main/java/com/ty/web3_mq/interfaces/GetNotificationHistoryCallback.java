package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.NotificationsBean;

public interface GetNotificationHistoryCallback {
    void onSuccess(NotificationsBean notificationsBean);
    void onFail(String error);
}
