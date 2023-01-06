package com.ty.web3_mq;

import com.ty.web3_mq.http.ApiConfig;
import com.ty.web3_mq.http.HttpManager;
import com.ty.web3_mq.http.request.ChatRequest;
import com.ty.web3_mq.http.response.ChatResponse;
import com.ty.web3_mq.interfaces.GetChatsCallback;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.utils.Ed25519;

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
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes());
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

}
