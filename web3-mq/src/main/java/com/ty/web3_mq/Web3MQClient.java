package com.ty.web3_mq;

import android.content.Context;
import android.util.Log;

import com.ty.web3_mq.http.ApiConfig;
import com.ty.web3_mq.http.HttpManager;
import com.ty.web3_mq.http.response.PingResponse;
import com.ty.web3_mq.interfaces.ConnectCallback;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.websocket.MessageManager;
import com.ty.web3_mq.websocket.Web3MQSocketClient;
import com.ty.web3_mq.websocket.WebsocketConfig;

import org.jetbrains.annotations.NotNull;

import java.net.URI;

import org.java_websocket.enums.ReadyState;

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
        MessageManager.getInstance().setConnectCallback(connectCallback);
        this.prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
        String user_id = DefaultSPHelper.getInstance().getUserID();
        Log.i(TAG,"get prv seed:"+prv_key_seed);
        if(this.prv_key_seed==null || user_id==null){
            Log.e(TAG,"there is no account at local storage, please register first");
            return;
        }
        this.userid = user_id;
        Log.i(TAG,"prv_key_seed" + this.prv_key_seed);
        Log.i(TAG,"userid" + this.userid);
        HttpManager.getInstance().get(ApiConfig.PING, null,null,null, PingResponse.class, new HttpManager.Callback<PingResponse>() {
            @Override
            public void onResponse(PingResponse response) {
                node_id = response.getData().NodeID;
                Log.i("getNodeId","NodeID:"+node_id);
                socketClient.initConnectionParam(node_id,userid,prv_key_seed);
                connectWebSocket(connectCallback);
            }

            @Override
            public void onError(String error) {
                if(connectCallback!=null){
                    connectCallback.onFail("ping error");
                }
            }
        });
    }

    private void connectWebSocket(ConnectCallback connectCallback) {
        if(node_id!=null && userid!=null && prv_key_seed !=null){
            if (socketClient == null) {
                return;
            }
            if (!socketClient.isOpen()) {
                if (socketClient.getReadyState().equals(ReadyState.NOT_YET_CONNECTED)) {
                    try {
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
