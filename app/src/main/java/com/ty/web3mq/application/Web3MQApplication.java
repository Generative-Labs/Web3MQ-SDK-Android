package com.ty.web3mq.application;

import android.app.Application;
import android.util.Log;

import com.ty.common.Web3MQUI;
import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.Web3MQSign;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.Ed25519;


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
