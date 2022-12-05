package com.ty.web3_mq;

import android.util.Log;

import com.ty.web3_mq.http.ApiConfig;
import com.ty.web3_mq.http.HttpManager;
import com.ty.web3_mq.http.request.ChangeNotificationStatusRequest;
import com.ty.web3_mq.http.response.CommonResponse;
import com.ty.web3_mq.interfaces.OnNotificationMessageEvent;
import com.ty.web3_mq.utils.Constant;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.websocket.MessageManager;

public class Web3MQNotification {
    private static final String TAG = "NotificationManager";
    private volatile static Web3MQNotification notification;
    private Web3MQNotification() {
    }

    public static Web3MQNotification getInstance() {
        if (null == notification) {
            synchronized (Web3MQUser.class) {
                if (null == notification) {
                    notification = new Web3MQNotification();
                }
            }
        }
        return notification;
    }

    public void setOnNotificationMessageEvent(OnNotificationMessageEvent onNotificationMessageEvent){
        MessageManager.getInstance().setOnNotificationMessageEvent(onNotificationMessageEvent);
    }

    public void changeStatus(String userid, String[] messages, String status){
        String prv_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
        if(prv_seed==null){
            Log.e(TAG,"no prv seed in local storage, please register first");
            return;
        }
        ChangeNotificationStatusRequest request = new ChangeNotificationStatusRequest();
        HttpManager.getInstance().post(ApiConfig.CHANGE_NOTIFICATION_STATUS, request, CommonResponse.class, new HttpManager.Callback<CommonResponse>() {
            @Override
            public void onResponse(CommonResponse response) {
                //TODO
            }

            @Override
            public void onError(String error) {

            }
        });
    }
}
