package com.ty.web3_mq;

import com.ty.web3_mq.http.ApiConfig;
import com.ty.web3_mq.http.HttpManager;
import com.ty.web3_mq.http.request.GetNotificationHistoryRequest;
import com.ty.web3_mq.http.response.GetNotificationHistoryResponse;
import com.ty.web3_mq.interfaces.GetNotificationHistoryCallback;
import com.ty.web3_mq.interfaces.NotificationMessageCallback;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.utils.Ed25519;
import com.ty.web3_mq.websocket.MessageManager;

public class Web3MQNotification {
    private static final String TAG = "Web3MQNotification";
    private volatile static Web3MQNotification notification;
    public static final String NOTIFICATION_TYPE_FRIEND_REQUEST = "system.friend_request";
    public static final String NOTIFICATION_TYPE_AGREE_FRIEND_REQUEST = "system.agree_friend_request";
    public static final String NOTIFICATION_TYPE_GROUP_INVITATION = "system.group_invitation";
    public static final String NOTIFICATION_TYPE_SUBSCRIPTION = "subscription";
    private Web3MQNotification() {
    }

    public static Web3MQNotification getInstance() {
        if (null == notification) {
            synchronized (Web3MQNotification.class) {
                if (null == notification) {
                    notification = new Web3MQNotification();
                }
            }
        }
        return notification;
    }

    public void setOnNotificationMessageEvent(NotificationMessageCallback notificationMessageCallback){
        MessageManager.getInstance().setOnNotificationMessageEvent(notificationMessageCallback);
    }

    public void removeNotificationMessageEvent(){
        MessageManager.getInstance().removeNotificationMessageEvent();
    }

//    public void changeStatus(String userid, String[] message_ids, String status){
//        String prv_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
//        if(prv_seed==null){
//            Log.e(TAG,"no prv seed in local storage, please register first");
//            return;
//        }
//        ChangeNotificationStatusRequest request = new ChangeNotificationStatusRequest();
//        HttpManager.getInstance().post(ApiConfig.CHANGE_NOTIFICATION_STATUS, request, CommonResponse.class, new HttpManager.Callback<CommonResponse>() {
//            @Override
//            public void onResponse(CommonResponse response) {
//                //TODO
//            }
//
//            @Override
//            public void onError(String error) {
//
//            }
//        });
//    }


    public void getNotificationHistory(int page, int size, GetNotificationHistoryCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            GetNotificationHistoryRequest request = new GetNotificationHistoryRequest();
            request.userid = DefaultSPHelper.getInstance().getUserID();
//            request.notice_type = notice_type;
            request.page = page;
            request.size = size;
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.notice_type+request.timestamp).getBytes());
            HttpManager.getInstance().get(ApiConfig.GET_NOTIFICATION_HISTORY, request,pub_key,did_key, GetNotificationHistoryResponse.class, new HttpManager.Callback<GetNotificationHistoryResponse>() {
                @Override
                public void onResponse(GetNotificationHistoryResponse response) {
                    if(response.getCode()==0){
                        callback.onSuccess(response.getData());
                    }else{
                        callback.onFail("error code: "+response.getCode()+" msg:"+ response.getMsg());
                    }
                }

                @Override
                public void onError(String error) {
                    callback.onFail("error: "+error);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFail("ed25519 sign error");
        }
    }


}
