package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.GroupsBean;
import com.ty.web3_mq.http.response.GroupsResponse;

public interface GetGroupListCallback {
    void onSuccess(GroupsBean groups);
    void onFail(String error);
}
