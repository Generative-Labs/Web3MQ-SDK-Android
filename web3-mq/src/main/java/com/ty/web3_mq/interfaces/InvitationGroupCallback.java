package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.GroupBean;

public interface InvitationGroupCallback {
    void onSuccess(GroupBean invitationGroupBean);
    void onFail(String error);
}
