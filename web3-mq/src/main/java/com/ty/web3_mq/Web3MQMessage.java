package com.ty.web3_mq;

import android.util.Log;

import com.google.protobuf.ByteString;
import com.ty.web3_mq.http.ApiConfig;
import com.ty.web3_mq.http.HttpManager;
import com.ty.web3_mq.http.request.ChangeMessageStatusRequest;
import com.ty.web3_mq.http.request.GetMessageHistoryRequest;
import com.ty.web3_mq.http.response.BaseResponse;
import com.ty.web3_mq.http.response.GetMessageHistoryResponse;
import com.ty.web3_mq.interfaces.ChangeMessageStatusRequestCallback;
import com.ty.web3_mq.interfaces.GetMessageHistoryCallback;
import com.ty.web3_mq.utils.CommonUtils;
import com.ty.web3_mq.utils.Constant;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.utils.Ed25519;
import com.ty.web3_mq.websocket.WebsocketConfig;

import org.bouncycastle.jcajce.provider.digest.SHA3;

import java.math.BigInteger;
import java.security.MessageDigest;

import web3mq.Message;

public class Web3MQMessage {
    private static final String TAG = "Web3MQNotification";
    private volatile static Web3MQMessage message;
    private Web3MQMessage() {
    }

    public static Web3MQMessage getInstance() {
        if (null == message) {
            synchronized (Web3MQMessage.class) {
                if (null == message) {
                    message = new Web3MQMessage();
                }
            }
        }
        return message;
    }

    public void changeMessageStatusRequest(String[] message_ids,String topic,String status, ChangeMessageStatusRequestCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
            String prv_key_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
            ChangeMessageStatusRequest request = new ChangeMessageStatusRequest();
            request.userid = "user:"+pub_key;
            request.messages = message_ids;
            request.topic = topic;
            request.status = status;
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.status+request.timestamp).getBytes());
            HttpManager.getInstance().post(ApiConfig.CHANGE_MESSAGE_STATUS, request, BaseResponse.class, new HttpManager.Callback<BaseResponse>() {
                @Override
                public void onResponse(BaseResponse response) {
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

    public void getMessageHistory(int page, int size, String topic_id, GetMessageHistoryCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
            String prv_key_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
            GetMessageHistoryRequest request = new GetMessageHistoryRequest();
            request.userid = "user:"+pub_key;
            request.topic = topic_id;
            request.page = page;
            request.size = size;
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.topic+request.timestamp).getBytes());
            HttpManager.getInstance().post(ApiConfig.GET_MESSAGE_HISTORY, request, GetMessageHistoryResponse.class, new HttpManager.Callback<GetMessageHistoryResponse>() {
                @Override
                public void onResponse(GetMessageHistoryResponse response) {
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

    public void sendMessage(String msg, String topic_id, boolean needStore){
        if(Web3MQClient.getInstance().getNodeId()==null || !Web3MQClient.getInstance().getSocketClient().isOpen()){
            Log.e(TAG,"websocket not connect");
            return;
        }
        try {
            String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
            String prv_key_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
            String user_id = "user:"+pub_key;
            long timestamp = System.currentTimeMillis();
            String node_id = Web3MQClient.getInstance().getNodeId();
            String msg_id = GenerateMessageID(user_id, topic_id, timestamp, msg.getBytes());
            String signContent = msg_id + user_id + topic_id + node_id + timestamp;
            String sign = Ed25519.ed25519Sign(prv_key_seed,signContent.getBytes());
            Message.Web3MQRequestMessage.Builder builder= Message.Web3MQRequestMessage.newBuilder();
            builder.setNodeId(node_id);
            builder.setTimestamp(System.currentTimeMillis());
            builder.setCipherSuite("NONE");
            builder.setPayloadType("text/plain; charset=utf-8");
            builder.setFromSign(sign);
            builder.setTimestamp(timestamp);
            builder.setMessageId(msg_id);
            builder.setVersion(1);
            builder.setComeFrom(user_id);
            builder.setContentTopic(topic_id);
            builder.setNeedStore(needStore);
            builder.setPayload(ByteString.copyFrom(msg.getBytes()));
            byte[] sendMessageBytes = CommonUtils.appendPrefix(WebsocketConfig.category, WebsocketConfig.PbTypeMessage, builder.build().toByteArray());
            Web3MQClient.getInstance().getSocketClient().send(sendMessageBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String GenerateMessageID(String user_id, String topic, long timestamp, byte[] payload){
        MessageDigest md = new SHA3.Digest224();
        md.update(user_id.getBytes());
        md.update(topic.getBytes());
        md.update((""+timestamp).getBytes());
        md.update(payload);
        byte[] messageDigest = md.digest();
        BigInteger no = new BigInteger(1, messageDigest);
        return no.toString(16);
    }
}
