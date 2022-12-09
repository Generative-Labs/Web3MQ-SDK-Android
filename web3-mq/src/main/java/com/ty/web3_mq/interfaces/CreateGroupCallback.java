package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.GroupBean;

public interface CreateGroupCallback {
    void onSuccess(GroupBean groupBean);
    void onFail(String error);
}
