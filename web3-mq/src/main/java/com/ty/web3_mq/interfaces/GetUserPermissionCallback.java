package com.ty.web3_mq.interfaces;


import com.ty.web3_mq.http.beans.UserPermissionsBean;

public interface GetUserPermissionCallback {
    void onSuccess(UserPermissionsBean userPermissions);
    void onFail(String error);
}
