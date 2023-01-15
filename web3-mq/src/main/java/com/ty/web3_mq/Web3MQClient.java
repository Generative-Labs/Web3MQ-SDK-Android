package com.ty.web3_mq;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.ty.web3_mq.http.ApiConfig;
import com.ty.web3_mq.http.HttpManager;
import com.ty.web3_mq.http.response.PingResponse;
import com.ty.web3_mq.interfaces.BridgeConnectCallback;
import com.ty.web3_mq.interfaces.ConnectCallback;
import com.ty.web3_mq.interfaces.OnConnectCommandCallback;
import com.ty.web3_mq.utils.CommonUtils;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.utils.Ed25519;
import com.ty.web3_mq.websocket.MessageManager;
import com.ty.web3_mq.websocket.Web3MQSocketClient;
import com.ty.web3_mq.websocket.WebsocketConfig;

import org.jetbrains.annotations.NotNull;

import java.net.URI;

import org.java_websocket.enums.ReadyState;

import web3mq.Bridge;
import web3mq.Heartbeat;

/**
 */
public class Web3MQClient {
    private static final String TAG = "CLIENT";
    private volatile static Web3MQClient web3MQClient;
    private Web3MQSocketClient socketClient;
    private String api_key;
    private String prv_key_seed;
    private String userid;
    private String node_id;
    private Web3MQClient() {

    }

    public static Web3MQClient getInstance() {
        if (null == web3MQClient) {
            synchronized (Web3MQClient.class) {
                if (null == web3MQClient) {
                    web3MQClient = new Web3MQClient();
                }
            }
        }
        return web3MQClient;
    }

    //初始化
    public void init(@NotNull Context context, @NotNull String api_key){
        this.api_key = api_key;
        HttpManager.getInstance().initialize(context);
        initWebSocket();
    }


    private void initWebSocket() {
        if(socketClient ==null){
            URI uri = URI.create(WebsocketConfig.WS_URL);
            Log.i(TAG,"ws_url:"+WebsocketConfig.WS_URL);
            socketClient = new Web3MQSocketClient(uri);
        }
    }

    public void startConnect(ConnectCallback connectCallback){
//        MessageManager.getInstance().setConnectCallback(connectCallback);
        //TODO nodeID做本地缓存策略
        HttpManager.getInstance().get(ApiConfig.PING, null,null,null, PingResponse.class, new HttpManager.Callback<PingResponse>() {
            @Override
            public void onResponse(PingResponse response) {
                node_id = response.getData().NodeID;
                Log.i("getNodeId","NodeID:"+node_id);
                connectWebSocket(connectCallback, node_id);
            }

            @Override
            public void onError(String error) {
                if(connectCallback!=null){
                    connectCallback.onFail("ping error");
                }
            }
        });
    }

    public void sendConnectCommand(OnConnectCommandCallback callback){
        if(socketClient.isClosed()||socketClient.isClosing()){
            Log.e(TAG,"websocket closed");
            return;
        }
        String userid = DefaultSPHelper.getInstance().getUserID();
        String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
        if(userid == null || prv_key_seed == null){
            Log.e(TAG,"user not login");
            return;
        }
        MessageManager.getInstance().setOnConnectCommandCallback(callback);

        String pub_key = DefaultSPHelper.getInstance().getTempPublic();
        Heartbeat.ConnectCommand.Builder builder = Heartbeat.ConnectCommand.newBuilder();
        builder.setNodeId(node_id);
        builder.setUserId(userid);
        long timestamp = System.currentTimeMillis();
        builder.setTimestamp(timestamp);
        builder.setValidatePubKey(Base64.encodeToString(Ed25519.hexStringToBytes(pub_key),Base64.NO_WRAP));
        String sign_content = node_id+userid+timestamp;
        try {
            builder.setMsgSign(Ed25519.ed25519Sign(prv_key_seed,sign_content.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"ed25519 Sign Error");
        }
        byte[] connectBytes = CommonUtils.appendPrefix(WebsocketConfig.category, WebsocketConfig.PbTypeConnectReqCommand, builder.build().toByteArray());
        socketClient.send(connectBytes);
        Log.i(TAG,"sendConnectCommand");
    }

    public void sendBridgeConnectCommand(String dAppID, String topic_id){
        Bridge.Web3MQBridgeConnectCommand.Builder builder  = Bridge.Web3MQBridgeConnectCommand.newBuilder();
        builder.setNodeID(node_id);
        builder.setDAppID(dAppID);
        builder.setTopicID(topic_id);
        byte[] connectBridgeBytes = CommonUtils.appendPrefix(WebsocketConfig.category, WebsocketConfig.PbTypeWeb3MQBridgeConnectCommand, builder.build().toByteArray());
        Web3MQClient.getInstance().getSocketClient().send(connectBridgeBytes);
    }



    private void connectWebSocket(ConnectCallback connectCallback, String node_id) {
        if(node_id!=null){
            if (socketClient == null) {
                return;
            }
            socketClient.initConnectionParam(node_id);
            if (!socketClient.isOpen()) {
                if (socketClient.getReadyState().equals(ReadyState.NOT_YET_CONNECTED)) {
                    try {
                        socketClient.setConnectCallback(connectCallback);
                        socketClient.connect();
                    } catch (IllegalStateException e) {
                    }
                } else if (socketClient.getReadyState().equals(ReadyState.CLOSING) || socketClient.getReadyState().equals(ReadyState.CLOSED)) {
                    socketClient.reconnect();
                }
            }
        }else{
            if(connectCallback!=null){
                connectCallback.onFail("connect websocket error");
            }
            Log.e(TAG,"node id is null or init error");
        }
    }

    public String getNodeId(){
        return this.node_id;
    }

    protected Web3MQSocketClient getSocketClient(){
        return this.socketClient;
    }

    public void close(){
        if(socketClient!=null){
            try {
                socketClient.closeBlocking();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected String getApiKey() {
        return this.api_key;
    }
}
