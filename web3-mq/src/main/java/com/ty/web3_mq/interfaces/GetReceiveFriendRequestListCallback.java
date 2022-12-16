package com.ty.web3_mq.interfaces;


import com.ty.web3_mq.http.beans.FriendRequestsBean;

public interface GetReceiveFriendRequestListCallback {
    void onSuccess(FriendRequestsBean friendRequestBeans);
    void onFail(String error);
}
