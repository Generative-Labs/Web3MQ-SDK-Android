package com.ty.web3_mq.websocket;

import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;
import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.interfaces.ConnectCallback;
import com.ty.web3_mq.interfaces.OnNotificationMessageEvent;

import java.nio.ByteBuffer;

import web3mq.Heartbeat;
import web3mq.Message;


public class MessageManager {
    private OnNotificationMessageEvent onNotificationMessageEvent;
    private ConnectCallback connectCallback;
    private volatile static MessageManager messageManager;
    private static final String TAG = "MessageManager";
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

        byte[] data = new byte[length-2];
        System.arraycopy(bytes.array(), 2, data, 0, length-2);
        try {
            Heartbeat.ConnectCommand command = Heartbeat.ConnectCommand.parseFrom(data);
            Log.i(TAG,"NodeId: "+command.getNodeId());
            Log.i(TAG,"UserId: "+command.getUserId());
            Log.i(TAG,"Timestamp: "+command.getTimestamp());
            Log.i(TAG,"MsgSign: "+command.getMsgSign());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        switch (pbType){
            case WebsocketConfig.PbTypeConnectRespCommand:
                if(this.connectCallback!=null){
                    connectCallback.onSuccess();
                }
                break;
            case WebsocketConfig.PbTypeNotificationListResp:
                if(this.onNotificationMessageEvent!=null){
                    try {
                        Message.Web3MQMessageListResponse response = Message.Web3MQMessageListResponse.parseFrom(data);
                        this.onNotificationMessageEvent.onNotificationMessage(response);
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        Log.e(TAG,"NotificationListResp parse error");
                    }
                }
                break;
        }
    }

    public void setOnNotificationMessageEvent(OnNotificationMessageEvent onNotificationMessageEvent){
        this.onNotificationMessageEvent = onNotificationMessageEvent;
    }

    public void setConnectCallback(ConnectCallback connectCallback) {
        this.connectCallback = connectCallback;
    }
}
