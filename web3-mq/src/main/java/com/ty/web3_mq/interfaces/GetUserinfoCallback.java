package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.UserInfo;

public interface GetUserinfoCallback {
    void onSuccess(UserInfo userInfo);
    void onFail(String error);
}
