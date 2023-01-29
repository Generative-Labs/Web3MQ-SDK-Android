package com.ty.common;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ty.common.config.AppConfig;
import com.ty.web3_mq.Web3MQClient;

public class Web3MQUI {
    private static volatile Web3MQUI instance;
    private boolean isDebugARouter = true;//ARouter调试开关
    private static final String TAG = "Web3MQUI";
    public static synchronized Web3MQUI getInstance() {
        if (instance == null) {
            instance = new Web3MQUI();
        }
        return instance;
    }

    public void init(Context context){
        if (isDebugARouter) {
            //下面两行必须写在init之前，否则这些配置在init中将无效
            ARouter.openLog();
            //开启调试模式（如果在InstantRun模式下运行，必须开启调试模式！
            // 线上版本需要关闭，否则有安全风险）
            ARouter.openDebug();
        }
        Log.i(TAG,"Web3MQUI init");
        ARouter.init((Application) context);
        Web3MQClient.getInstance().init(context, AppConfig.APIKey);
    }
}