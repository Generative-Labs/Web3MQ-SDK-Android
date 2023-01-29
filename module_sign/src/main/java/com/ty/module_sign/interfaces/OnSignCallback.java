package com.ty.module_sign.interfaces;

public interface OnSignCallback {
    String sign(String sign_raw);
    void signApprove(String redirect);
    void signReject(String redirect);
}
