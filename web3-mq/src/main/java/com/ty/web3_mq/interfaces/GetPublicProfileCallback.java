package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.ProfileBean;

public interface GetPublicProfileCallback {
    void onSuccess(ProfileBean profileBean);
    void onFail(String error);
}
