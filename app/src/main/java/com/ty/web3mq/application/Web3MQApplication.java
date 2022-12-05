package com.ty.web3mq.application;

import android.app.Application;

import com.ty.web3_mq.Web3MQClient;


public class Web3MQApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Web3MQClient.getInstance().init(this,"rkkJARiziBQCscgg");
    }
}
