package com.ty.web3_mq.websocket;

import android.os.Handler;
import android.util.Log;

import com.ty.web3_mq.utils.CommonUtils;
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
    private String userid;
    private String prv_key_seed;
    public Web3MQSocketClient(URI serverUri) {
        super(serverUri);
    }

    public void initConnectionParam(String node_id, String userid, String prv_key_seed){
        this.node_id = node_id;
        this.userid = userid;
        this.prv_key_seed = prv_key_seed;
    }

    @Override
    protected void onSetSSLParameters(SSLParameters sslParameters) {
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        sendConnectCommand();
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
        builder.setUserId(userid);
        long timestamp = System.currentTimeMillis();
        builder.setTimestamp(timestamp);
        String sign_content = node_id+userid+timestamp;
        try {
            builder.setMsgSign(Ed25519.ed25519Sign(prv_key_seed,sign_content.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"ed25519 Sign Error");
        }
        byte[] connectBytes = CommonUtils.appendPrefix(WebsocketConfig.category, WebsocketConfig.PbTypePingCommand, builder.build().toByteArray());
        this.send(connectBytes);
    }

    private void sendConnectCommand(){
        Heartbeat.ConnectCommand.Builder builder = Heartbeat.ConnectCommand.newBuilder();
        builder.setNodeId(node_id);
        builder.setUserId(userid);
        long timestamp = System.currentTimeMillis();
        builder.setTimestamp(timestamp);
        String sign_content = node_id+userid+timestamp;
        try {
            builder.setMsgSign(Ed25519.ed25519Sign(prv_key_seed,sign_content.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"ed25519 Sign Error");
        }
        byte[] connectBytes = CommonUtils.appendPrefix(WebsocketConfig.category, WebsocketConfig.PbTypeConnectReqCommand, builder.build().toByteArray());
        this.send(connectBytes);
        Log.i(TAG,"sendConnectCommand");
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
        Log.i(TAG,"WebSocketClient onClose code:"+code+" reason:"+reason+" remote:"+remote);
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG,"WebSocketClient onError "+ ex.getLocalizedMessage());
    }
}
