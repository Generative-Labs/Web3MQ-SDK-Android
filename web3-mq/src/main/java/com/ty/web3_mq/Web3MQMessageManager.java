package com.ty.web3_mq;

import android.util.Base64;
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
import com.ty.web3_mq.interfaces.MessageCallback;
import com.ty.web3_mq.utils.CommonUtils;
import com.ty.web3_mq.utils.Constant;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.utils.Ed25519;
import com.ty.web3_mq.websocket.MessageManager;
import com.ty.web3_mq.websocket.WebsocketConfig;

import org.bouncycastle.jcajce.provider.digest.SHA3;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;

import web3mq.Message;

public class Web3MQMessageManager {
    private static final String TAG = "Web3MQNotification";
    private volatile static Web3MQMessageManager message;
    private Web3MQMessageManager() {
    }

    public static Web3MQMessageManager getInstance() {
        if (null == message) {
            synchronized (Web3MQMessageManager.class) {
                if (null == message) {
                    message = new Web3MQMessageManager();
                }
            }
        }
        return message;
    }

    public void changeMessageStatusRequest(String[] message_ids,String topic,String status, ChangeMessageStatusRequestCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            ChangeMessageStatusRequest request = new ChangeMessageStatusRequest();
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.messages = message_ids;
            request.topic = topic;
            request.status = status;
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.status+request.timestamp).getBytes());
            HttpManager.getInstance().post(ApiConfig.CHANGE_MESSAGE_STATUS, request,pub_key,did_key,BaseResponse.class, new HttpManager.Callback<BaseResponse>() {
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
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            GetMessageHistoryRequest request = new GetMessageHistoryRequest();
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.topic = topic_id;
            request.page = page;
            request.size = size;
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = URLEncoder.encode(Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.topic+request.timestamp).getBytes()));
            HttpManager.getInstance().get(ApiConfig.GET_MESSAGE_HISTORY, request,pub_key,did_key, GetMessageHistoryResponse.class, new HttpManager.Callback<GetMessageHistoryResponse>() {
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
        Log.i(TAG,"-----sendMessage-----");
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String user_id = DefaultSPHelper.getInstance().getUserID();
            long timestamp = System.currentTimeMillis();
            String node_id = Web3MQClient.getInstance().getNodeId();
            String msg_id = GenerateMessageID(user_id, topic_id, timestamp, msg.getBytes());
            String signContent = msg_id + user_id + topic_id + node_id + timestamp;
            String sign = Ed25519.ed25519Sign(prv_key_seed,signContent.getBytes());
            Message.Web3MQMessage.Builder builder= Message.Web3MQMessage.newBuilder();
            builder.setNodeId(node_id);
            Log.i(TAG,"node_id:"+node_id);
            builder.setCipherSuite("NONE");
            builder.setPayloadType("text/plain; charset=utf-8");
            builder.setFromSign(sign);
            Log.i(TAG,"sign:"+sign);
            builder.setTimestamp(timestamp);
            Log.i(TAG,"timestamp:"+timestamp);
            builder.setMessageId(msg_id);
            Log.i(TAG,"msg_id:"+msg_id);
            builder.setVersion(1);
            builder.setComeFrom(user_id);
            Log.i(TAG,"comfrom:"+user_id);
            builder.setContentTopic(topic_id);
            Log.i(TAG,"topic_id:"+topic_id);
            builder.setNeedStore(needStore);
            Log.i(TAG,"needStore:"+needStore);
            builder.setPayload(ByteString.copyFrom(msg.getBytes()));
            Log.i(TAG,"payload:"+msg);
            builder.setValidatePubKey(Base64.encodeToString(Ed25519.hexStringToBytes(pub_key),Base64.NO_WRAP));
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

    public void addDMCallback(String from, MessageCallback callback){
        MessageManager.getInstance().addDMMessageCallback(from,callback);
    }

    public void removeDMCallback(String from){
        MessageManager.getInstance().removeDMMessageCallback(from);
    }

    public void addGroupMessageCallback(String group_id, MessageCallback callback){
        MessageManager.getInstance().addGroupMessageCallback(group_id,callback);
    }

    public void removeGroupMessageCallback(String group_id){
        MessageManager.getInstance().removeGroupMessageCallback(group_id);
    }
}
