package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.FollowersBean;


public interface GetMyFollowingCallback {
    void onSuccess(FollowersBean followersBeans);
    void onFail(String error);
}
