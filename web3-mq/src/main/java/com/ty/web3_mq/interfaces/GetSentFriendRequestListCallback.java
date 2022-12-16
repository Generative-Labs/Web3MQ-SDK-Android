package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.FriendRequestsBean;


public interface GetSentFriendRequestListCallback {
    void onSuccess(FriendRequestsBean requestsBean);
    void onFail(String error);
}
