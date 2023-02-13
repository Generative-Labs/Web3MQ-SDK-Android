package com.ty.web3_mq;

import com.ty.web3_mq.http.ApiConfig;
import com.ty.web3_mq.http.HttpManager;
import com.ty.web3_mq.http.request.ChatRequest;
import com.ty.web3_mq.http.request.UpdateChatRequest;
import com.ty.web3_mq.http.response.BaseResponse;
import com.ty.web3_mq.http.response.ChatResponse;
import com.ty.web3_mq.interfaces.GetChatsCallback;
import com.ty.web3_mq.interfaces.UpdateMyChatCallback;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.utils.Ed25519;

import java.net.URLEncoder;

public class Web3MQChats {
    private static final String TAG = "Chats";
    private volatile static Web3MQChats chats;
    private Web3MQChats() {
    }
    public static Web3MQChats getInstance() {
        if (null == chats) {
            synchronized (Web3MQChats.class) {
                if (null == chats) {
                    chats = new Web3MQChats();
                }
            }
        }
        return chats;
    }

    public void getChats(int page, int size, GetChatsCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            ChatRequest request = new ChatRequest();
            request.timestamp = System.currentTimeMillis();
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.page = page;
            request.size = size;
            request.web3mq_signature = URLEncoder.encode(Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes()));
            HttpManager.getInstance().get(ApiConfig.GET_CHAT_LIST, request,pub_key,did_key, ChatResponse.class, new HttpManager.Callback<ChatResponse>() {
                @Override
                public void onResponse(ChatResponse response) {
                    if(response.getCode()==0){
                        callback.onSuccess(response.getData());
                    }else{
                        callback.onFail("error code: "+response.getCode()+" msg:"+ response.getMsg());
                    }
                }

                @Override
                public void onError(String error) {
                    callback.onFail("request error: "+error);
                }
            });
        } catch (Exception e) {
            callback.onFail("ed25519 sign error");
            e.printStackTrace();
        }
    }

    public void updateMyChat(long timestamp, String chatid, String chat_type, UpdateMyChatCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            UpdateChatRequest request = new UpdateChatRequest();
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.timestamp = timestamp;
            request.chatid = chatid;
            request.chat_type = chat_type;
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes());
            HttpManager.getInstance().post(ApiConfig.UPDATE_MY_CHAT, request, pub_key, did_key, BaseResponse.class, new HttpManager.Callback<BaseResponse>() {
                @Override
                public void onResponse(BaseResponse response) {
                    if(callback!=null){
                        callback.onSuccess();
                    }
                }

                @Override
                public void onError(String error) {
                    if(callback!=null){
                        callback.onFail(error);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
