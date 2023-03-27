package com.ty.module_sign.interfaces;

import com.ty.web3_mq.websocket.bean.SignRequest;

public interface WalletInitCallback {
    void initSuccess();
    void onFail(String error);
}
