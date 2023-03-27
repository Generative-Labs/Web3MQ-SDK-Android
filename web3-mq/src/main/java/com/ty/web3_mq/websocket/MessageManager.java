package com.ty.web3_mq.websocket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.http.beans.NotificationBean;
import com.ty.web3_mq.http.beans.NotificationPayload;
import com.ty.web3_mq.interfaces.BridgeConnectCallback;
import com.ty.web3_mq.interfaces.BridgeMessageCallback;
import com.ty.web3_mq.interfaces.ChatsMessageCallback;
import com.ty.web3_mq.interfaces.ConnectCallback;
import com.ty.web3_mq.interfaces.MessageCallback;
import com.ty.web3_mq.interfaces.NotificationMessageCallback;
import com.ty.web3_mq.interfaces.OnConnectCommandCallback;
import com.ty.web3_mq.interfaces.SendBridgeMessageCallback;
import com.ty.web3_mq.websocket.bean.BridgeMessage;
import com.ty.web3_mq.websocket.bean.MessageBean;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import web3mq.Bridge;
import web3mq.Heartbeat;
import web3mq.Message;


public class MessageManager {
    private NotificationMessageCallback notificationMessageCallback;
    private ConnectCallback connectCallback;
    private BridgeConnectCallback bridgeConnectCallback;
    private BridgeMessageCallback bridgeMessageCallback;
    private ChatsMessageCallback chatsMessageCallback;
    private HashMap<String, MessageCallback> DMMessageCallbackHashMap = new HashMap<>();
    private HashMap<String, MessageCallback> groupMessageCallbackHashMap = new HashMap<>();
    private HashMap<String, SendBridgeMessageCallback> sendBridgeMessageCallbackHashMap = new HashMap<>();

    private volatile static MessageManager messageManager;
    private static final String TAG = "MessageManager";
    private Handler handler = new Handler(Looper.getMainLooper());
    private Gson gson = new Gson();
    private OnConnectCommandCallback connectCommandCallback;

    public static final String MESSAGE_TYPE_BRIDGE = "Web3MQ/bridge";

    private MessageManager() {

    }
    public static MessageManager getInstance() {
        if (null == messageManager) {
            synchronized (Web3MQClient.class) {
                if (null == messageManager) {
                    messageManager = new MessageManager();
                }
            }
        }
        return messageManager;
    }

    public void onMessage(ByteBuffer bytes){
        int length = bytes.array().length;
        Log.i(TAG,"WebSocketClient onMessage bytes length"+length);
        int categoryType = bytes.array()[0];
        byte pbType = bytes.array()[1];
        Log.i(TAG,"categoryType: "+categoryType);
        Log.i(TAG,"pbType: "+pbType);
        if(pbType == WebsocketConfig.PbTypePongCommand){
            Log.i(TAG,"pong response");
        }

        byte[] data = new byte[length-2];
        System.arraycopy(bytes.array(), 2, data, 0, length-2);
        switch (pbType){
            case WebsocketConfig.PbTypeConnectRespCommand:
                Log.i(TAG,"ConnectResp");
                if(this.connectCommandCallback!=null){
                    try {
                        Heartbeat.ConnectCommand command = Heartbeat.ConnectCommand.parseFrom(data);
                        Log.i(TAG,"NodeId: "+command.getNodeId());
                        Log.i(TAG,"UserId: "+command.getUserId());
                        Log.i(TAG,"Timestamp: "+command.getTimestamp());
                        Log.i(TAG,"MsgSign: "+command.getMsgSign());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            connectCommandCallback.onConnectCommandResponse();
                        }
                    });

                }
                break;
            case WebsocketConfig.PbTypeNotificationListResp:
                Log.i(TAG,"NotificationListResp");
                if(this.notificationMessageCallback !=null){
                    try {
                        Message.Web3MQMessageListResponse response = Message.Web3MQMessageListResponse.parseFrom(data);
                        List<Message.MessageItem> messageList= response.getDataList();
                        ArrayList<NotificationBean> notificationList = new ArrayList<>();
                        for(Message.MessageItem message: messageList){
                            NotificationBean notification = new NotificationBean();
                            notification.from = message.getComeFrom();
                            notification.messageid = message.getMessageId();
                            notification.payload = gson.fromJson(message.getPayload().toStringUtf8(),NotificationPayload.class);
                            notification.timestamp = message.getTimestamp();
                            notification.cipher_suite = message.getCipherSuite();
                            notification.from_sign = message.getFromSign();
                            notification.topic = message.getContentTopic();
                            notification.payload_type =message.getPayloadType();
                            notificationList.add(notification);
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                notificationMessageCallback.onNotificationMessage(notificationList);
                            }
                        });

                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        Log.e(TAG,"NotificationListResp parse error");
                    }
                }
                break;
            case WebsocketConfig.PbTypeMessage:
                Log.i(TAG,"Message callback");
                try {
                    Message.Web3MQMessage message = Message.Web3MQMessage.parseFrom(data);
                    Log.i(TAG,"MessageType:"+message.getMessageType());
                    Log.i(TAG,"MessageId:"+message.getMessageId());
                    Log.i(TAG,"MessageType:"+message.getMessageType());
                    Log.i(TAG,"ComeFrom:"+message.getComeFrom());
                    Log.i(TAG,"Payload:"+message.getPayload().toStringUtf8());
                    Log.i(TAG,"PayloadType:"+message.getPayloadType());
                    Log.i(TAG,"ContentTopic:"+message.getContentTopic());
                    if(MESSAGE_TYPE_BRIDGE.equals(message.getMessageType())){
                        if(bridgeMessageCallback!=null){
                            BridgeMessage bridgeMessage = gson.fromJson(message.getPayload().toStringUtf8(), BridgeMessage.class);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    bridgeMessageCallback.onBridgeMessage(message.getComeFrom(), bridgeMessage.publicKey, bridgeMessage.content);
                                }
                            });
                        }

                    }else{
                        if(chatsMessageCallback!=null){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    chatsMessageCallback.onMessage(message);
                                }
                            });
                        }
                    }

                    for(String come_from: DMMessageCallbackHashMap.keySet()){
                        if(come_from.equals(message.getComeFrom())){
                            MessageBean messageBean= new MessageBean();
                            messageBean.from = message.getComeFrom();
                            messageBean.payload = message.getPayload().toStringUtf8();
                            messageBean.timestamp = message.getTimestamp();
                            MessageCallback callback = DMMessageCallbackHashMap.get(come_from);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onMessage(messageBean);
                                }
                            });
                        }
                    }
                    for(String group_id: groupMessageCallbackHashMap.keySet()){
                        if(group_id.equals(message.getContentTopic())){
                            MessageBean messageBean= new MessageBean();
                            messageBean.from = message.getComeFrom();
                            messageBean.payload = message.getPayload().toStringUtf8();
                            messageBean.timestamp = message.getTimestamp();
                            MessageCallback callback = groupMessageCallbackHashMap.get(group_id);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onMessage(messageBean);
                                }
                            });
                        }
                    }

                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }

                break;
            case WebsocketConfig.PbTypeMessageStatusResp:
                Log.i(TAG,"MessageStatusResp");
                try {
                    Message.Web3MQMessageStatusResp response = Message.Web3MQMessageStatusResp.parseFrom(data);
                    Log.i(TAG,"ComeFrom: "+response.getComeFrom());
                    Log.i(TAG,"MessageId: "+response.getMessageId());
                    Log.i(TAG,"Timestamp: "+response.getTimestamp());
                    Log.i(TAG,"ContentTopic: "+response.getContentTopic());
                    Log.i(TAG,"messageStatus: "+response.getMessageStatus());

                    SendBridgeMessageCallback callback = sendBridgeMessageCallbackHashMap.get(response.getMessageId());
                    if(callback!=null){
                        if(response.getMessageStatus().equals("received")){
                            callback.onReceived();
                        }else{
                            callback.onFail();
                        }
                        sendBridgeMessageCallbackHashMap.remove(response.getMessageId());
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                    Log.e(TAG,"parse error");
                }
                break;
            case WebsocketConfig.PbTypeWeb3MQBridgeConnectResp:
                Log.i(TAG,"BridgeConnectResp");
                if(bridgeConnectCallback!=null){
                    try {
                        Bridge.Web3MQBridgeConnectCommand connectCommand = Bridge.Web3MQBridgeConnectCommand.parseFrom(data);
                        Log.i(TAG,"Node ID:" + connectCommand.getNodeID());
                        Log.i(TAG,"DApp ID:" + connectCommand.getDAppID());
                        Log.i(TAG,"Topic ID:" + connectCommand.getTopicID());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                bridgeConnectCallback.onConnectCallback();
                            }
                        });
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        Log.e(TAG,"parse error");
                    }
                }
                break;
        }
    }

    public void setOnConnectCommandCallback(OnConnectCommandCallback connectCommandCallback){
        this.connectCommandCallback = connectCommandCallback;
    }


    public void setBridgeMessageCallback(BridgeMessageCallback callback){
        this.bridgeMessageCallback = callback;
    }

    public void removeBridgeMessageCallback(){
        this.bridgeMessageCallback = null;
    }

    public void setBridgeConnectCallback(BridgeConnectCallback callback){
        this.bridgeConnectCallback = callback;
    }

    public void removeBridgeConnectCallback(){
        this.bridgeConnectCallback = null;
    }

    public void setOnNotificationMessageEvent(NotificationMessageCallback notificationMessageCallback){
        this.notificationMessageCallback = notificationMessageCallback;
    }

    public void removeNotificationMessageEvent(){
        this.notificationMessageCallback = null;
    }

    public void setConnectCallback(ConnectCallback connectCallback) {
        this.connectCallback = connectCallback;
    }

    public void addDMMessageCallback(String from, MessageCallback callback) {
        DMMessageCallbackHashMap.put(from,callback);
    }
    public void removeDMMessageCallback(String from){
        DMMessageCallbackHashMap.remove(from);
    }

    public void addGroupMessageCallback(String group_id, MessageCallback callback) {
        groupMessageCallbackHashMap.put(group_id,callback);
    }
    public void removeGroupMessageCallback(String group_id){
        groupMessageCallbackHashMap.remove(group_id);
    }

    public void addSendBridgeMessageCallback(String messageID, SendBridgeMessageCallback callback){
        sendBridgeMessageCallbackHashMap.put(messageID,callback);
    }

    public void removeSendBridgeMessageCallback(String messageID){
        sendBridgeMessageCallbackHashMap.remove(messageID);
    }

//    public void addTopicMessageCallback(String topic_id, MessageCallback callback){
//        topicMessageCallbackHashMap.put(topic_id,callback);
//    }
//
//    public void removeTopicMessageCallback(String Topic_id){
//        topicMessageCallbackHashMap.remove(Topic_id);
//    }

    public void setChatsMessageCallback(ChatsMessageCallback callback){
        chatsMessageCallback = callback;
    }

    public void removeChatsMessageCallback(){
        this.chatsMessageCallback = null;
    }
}
