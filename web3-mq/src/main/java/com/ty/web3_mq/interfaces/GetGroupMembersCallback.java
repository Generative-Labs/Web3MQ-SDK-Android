package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.GroupMembersBean;

public interface GetGroupMembersCallback {
    void onSuccess(GroupMembersBean groupMembersBean);
    void onFail(String error);
}
