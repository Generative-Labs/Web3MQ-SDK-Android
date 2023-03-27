package com.ty.web3mq.application;

import android.app.Application;

import com.ty.common.Web3MQUI;


public class Web3MQApplication extends Application {
    private boolean isDebugARouter = true;
    @Override
    public void onCreate() {
        super.onCreate();
//        Web3MQSign.getInstance().generate25519KeyPairTest();
        //ARouter调试开关

        Web3MQUI.getInstance().init(this);
    }
}
