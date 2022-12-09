package com.ty.web3_mq;

import com.ty.web3_mq.http.ApiConfig;
import com.ty.web3_mq.http.HttpManager;
import com.ty.web3_mq.http.request.CreateTopicRequest;
import com.ty.web3_mq.http.request.GetMyCreateTopicListRequest;
import com.ty.web3_mq.http.request.GetNotificationHistoryRequest;
import com.ty.web3_mq.http.request.PublishTopicMessageRequest;
import com.ty.web3_mq.http.response.CommonResponse;
import com.ty.web3_mq.http.response.GetNotificationHistoryResponse;
import com.ty.web3_mq.http.response.TopicListResponse;
import com.ty.web3_mq.interfaces.CreateTopicCallback;
import com.ty.web3_mq.interfaces.GetMyCreateTopicCallback;
import com.ty.web3_mq.interfaces.GetMySubscribeTopicCallback;
import com.ty.web3_mq.interfaces.PublishTopicMessageCallback;
import com.ty.web3_mq.interfaces.SubscribeCallback;
import com.ty.web3_mq.utils.Constant;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.utils.Ed25519;

public class Web3MQTopic {
    private static final String TAG = "Web3MQNotification";
    private volatile static Web3MQTopic topic;
    private Web3MQTopic() {
    }

    public static Web3MQTopic getInstance() {
        if (null == topic) {
            synchronized (Web3MQTopic.class) {
                if (null == topic) {
                    topic = new Web3MQTopic();
                }
            }
        }
        return topic;
    }

    public void createTopic(String topic_name, CreateTopicCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
            String prv_key_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
            CreateTopicRequest request = new CreateTopicRequest();
            request.userid = "user:"+pub_key;
            request.topic_name = topic_name;
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes());
            HttpManager.getInstance().post(ApiConfig.CREATE_TOPIC, request, CommonResponse.class, new HttpManager.Callback<CommonResponse>() {
                @Override
                public void onResponse(CommonResponse response) {
                    if(response.getCode()==0){
                        callback.onSuccess();
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

    public void getMyCreateTopicList(int page, int size, GetMyCreateTopicCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
            String prv_key_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
            GetMyCreateTopicListRequest request = new GetMyCreateTopicListRequest();
            request.userid = "user:"+pub_key;
            request.page = page;
            request.size = size;
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes());
            HttpManager.getInstance().get(ApiConfig.GET_MY_SUBSCRIBE_TOPIC_LIST, request, TopicListResponse.class, new HttpManager.Callback<TopicListResponse>() {
                @Override
                public void onResponse(TopicListResponse response) {
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

    public void getMySubscribeTopicList(int page, int size, GetMySubscribeTopicCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
            String prv_key_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
            GetMyCreateTopicListRequest request = new GetMyCreateTopicListRequest();
            request.userid = "user:"+pub_key;
            request.page = page;
            request.size = size;
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes());
            HttpManager.getInstance().get(ApiConfig.GET_MY_CREATE_TOPIC_LIST, request, TopicListResponse.class, new HttpManager.Callback<TopicListResponse>() {
                @Override
                public void onResponse(TopicListResponse response) {
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

    public void publishTopicMessage(String topicid, String title, String content, PublishTopicMessageCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
            String prv_key_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
            PublishTopicMessageRequest request = new PublishTopicMessageRequest();
            request.userid = "user:"+pub_key;
            request.topicid = topicid;
            request.title = title;
            request.content = content;
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.topicid+request.timestamp).getBytes());
            HttpManager.getInstance().post(ApiConfig.PUBLISH_TOPIC_MESSAGE, request, CommonResponse.class, new HttpManager.Callback<CommonResponse>() {
                @Override
                public void onResponse(CommonResponse response) {
                    if(response.getCode()==0){
                        callback.onSuccess();
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

    public void subscribeTopic(String topicid, SubscribeCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
            String prv_key_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
            PublishTopicMessageRequest request = new PublishTopicMessageRequest();
            request.userid = "user:"+pub_key;
            request.topicid = topicid;
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.topicid+request.timestamp).getBytes());
            HttpManager.getInstance().post(ApiConfig.SUBSCRIBE_TOPIC_MESSAGE, request, CommonResponse.class, new HttpManager.Callback<CommonResponse>() {
                @Override
                public void onResponse(CommonResponse response) {
                    if(response.getCode()==0){
                        callback.onSuccess();
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
