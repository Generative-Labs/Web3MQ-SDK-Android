package com.ty.web3_mq.websocket;

import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import com.ty.web3_mq.interfaces.ConnectCallback;
import com.ty.web3_mq.interfaces.OnWebsocketClosedCallback;
import com.ty.web3_mq.utils.CommonUtils;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.utils.Ed25519;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLParameters;

import pb.KeepAlive;
import web3mq.Heartbeat;

public class Web3MQSocketClient extends WebSocketClient {
    private static final String TAG = "Web3MQClient";
    private String node_id;
    private ConnectCallback callback;
    private OnWebsocketClosedCallback onWebsocketClosedCallback;
    private Handler mHandler = new Handler();
    public Web3MQSocketClient(URI serverUri) {
        super(serverUri);
    }


    public void switchUri(URI uri){
        this.uri = uri;
    }



    public void initConnectionParam(String node_id){
        this.node_id = node_id;
        this.setConnectionLostTimeout(0);
    }

    @Override
    protected void onSetSSLParameters(SSLParameters sslParameters) {
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        if(callback!=null){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess();
                }
            });
        }
        Log.i(TAG,"WebSocketClient onOpen");
        Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                ping();
            }
        };
        timer.schedule(timerTask, 0,60000);
    }

    private void ping(){
        if(this.isClosed()||this.isClosing()){
            Log.e(TAG,"websocket closed");
            return;
        }
        Log.i(TAG,"send ping");
        KeepAlive.WebsocketPingCommand.Builder builder = KeepAlive.WebsocketPingCommand.newBuilder();
        builder.setNodeId(node_id);
        long timestamp = System.currentTimeMillis();
        builder.setTimestamp(timestamp);
        byte[] connectBytes = CommonUtils.appendPrefix(WebsocketConfig.category, WebsocketConfig.PbTypePingCommand, builder.build().toByteArray());
        this.send(connectBytes);
    }

    @Override
    public void onMessage(String message) {
        Log.i(TAG,"WebSocketClient onMessage "+message);
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        MessageManager.getInstance().onMessage(bytes);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.e(TAG,"WebSocketClient onClose code:"+code+" reason:"+reason+" remote:"+remote);
        if(onWebsocketClosedCallback!=null){
            onWebsocketClosedCallback.onClose();
        }
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG,"WebSocketClient onError "+ ex.getLocalizedMessage());
    }

    public void setConnectCallback(ConnectCallback connectCallback){
        this.callback = connectCallback;
    }

    public void setOnWebsocketClosedCallback(OnWebsocketClosedCallback callback) {
        onWebsocketClosedCallback = callback;
    }
}
