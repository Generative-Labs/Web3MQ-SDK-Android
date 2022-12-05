package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.ContactsBean;

public interface GetSentFriendRequestListCallback {
    void onSuccess(ContactsBean contactsBean);
    void onFail(String error);
}
