package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.UsersBean;
import com.ty.web3_mq.http.response.SearchUsersResponse;

public interface SearchUsersCallback {
    void onSuccess(UsersBean usersBean);
    void onFail(String error);
}
