package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.ContactsBean;

public interface SearchContactsCallback {
    void onSuccess(ContactsBean contactsBean);
    void onFail(String error);
}
