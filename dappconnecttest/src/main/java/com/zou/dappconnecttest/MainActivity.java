package com.zou.dappconnecttest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.interfaces.SendBridgeMessageCallback;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.websocket.bean.sign.Web3MQSign;
import com.ty.web3_mq.interfaces.BridgeConnectCallback;
import com.ty.web3_mq.interfaces.ConnectCallback;
import com.ty.web3_mq.interfaces.OnConnectResponseCallback;
import com.ty.web3_mq.interfaces.OnSignResponseMessageCallback;
import com.ty.web3_mq.utils.RandomUtils;
import com.ty.web3_mq.websocket.bean.BridgeMessageMetadata;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private boolean connectSuccess = false;
    private Button btn_start,btn_sign;
    private static final String APIKey = "rkkJARiziBQCscgg";
    private static final String DAppID = "web3MQ_dapp_test:dapp";
    private String ETH_ADDRESS = "0x3a71d76262729144B0E833AF463Ed459179327aF";
    private Web3MQSign web3MQSignDApp = Web3MQSign.getInstance();
    private int signSendCount = 0;
    private int signReceiveCount = 0;
    private int connectSendCount = 0;
    private int connectReceiveCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");
        setContentView(R.layout.activity_main);
        btn_start = findViewById(R.id.btn_start);
        btn_sign = findViewById(R.id.btn_sign);
        Web3MQClient.getInstance().init(this, APIKey);
        Web3MQClient.getInstance().startConnect(new ConnectCallback() {
            @Override
            public void onSuccess() {
                web3MQSignDApp.init(DAppID, new BridgeConnectCallback() {
                    @Override
                    public void onConnectCallback() {
                        btn_start.setEnabled(true);
                        Log.i(TAG,"web3MQ Sign DApp init success");
                    }
                });
            }

            @Override
            public void onFail(String error) {
                Log.e(TAG,"connect web3mq network error:"+error);
            }

            @Override
            public void alreadyConnected() {
                Log.i(TAG,"web3MQ Sign DApp already connected");
            }
        });

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTest();
            }
        });
        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSign();
            }
        });
        DefaultSPHelper.getInstance().clear();
//        DefaultSPHelper.getInstance().showSessionInfo();

    }

    private void startTest(){
        //connect
        connect();
        //listen
        web3MQSignDApp.setOnConnectResponseCallback(new OnConnectResponseCallback() {
            @Override
            public void onApprove(BridgeMessageMetadata walletInfo, String address) {
                connectReceiveCount++;
                Log.i(TAG,"-----Receive Connect Response----count:"+connectReceiveCount);
                Log.i(TAG,"approve : true");
                Log.i(TAG,"walletInfo address: "+address);
                Log.i(TAG,"walletInfo walletType: "+walletInfo.walletType);
                Log.i(TAG,"walletInfo name: "+walletInfo.name);
                Log.i(TAG,"walletInfo description: "+walletInfo.description);
                Log.i(TAG,"-----Receive Connect End----");
                btn_sign.setEnabled(true);
            }

            @Override
            public void onReject() {
                Log.i(TAG,"-----Receive Connect Response----");
                Log.i(TAG,"approve : false");
                Log.i(TAG,"-----Receive Connect End----");
            }
        });

        web3MQSignDApp.setOnSignResponseMessageCallback(new OnSignResponseMessageCallback() {
            @Override
            public void onApprove(String signature) {
                signReceiveCount++;
                Log.i(TAG,"-----Receive Sign Response----count:"+ signReceiveCount);
                Log.i(TAG,"approve : true");
                Log.i(TAG,"signature:"+signature);
                Log.i(TAG,"-----Receive Sign Request End----");

//                sendSign();
//                web3MQSignDApp.close();
            }

            @Override
            public void onReject() {
                Log.i(TAG,"-----Receive Sign Response----");
                Log.i(TAG,"approve : false");
                Log.i(TAG,"-----Receive Sign Request End----");
//                web3MQSignDApp.close();
            }
        });
    }

    private void connect(){
        connectSendCount++;
        Log.i(TAG,"-----Send Connect Request ----count:"+ connectSendCount);
        String deepLink = web3MQSignDApp.generateConnectDeepLink(null,"www.web3mq_test.com","web3mq_dapp_test://");
        Log.i(TAG,"generate Connect DeepLink:"+deepLink);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
        startActivity(intent);
        Log.i(TAG,"-----Send Connect End ----");
    }

    private void sendSign(){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Web3MQSign.getInstance().generateSignDeepLink()));
        startActivity(intent);
        //sign request
        signSendCount++;
        Log.i(TAG,"----Send Sign Request----count:"+ signSendCount);
        String wallet_address = ETH_ADDRESS;
        String sign_raw = RandomUtils.randomNonce();
        Log.i(TAG,"wallet address:"+wallet_address);
        Log.i(TAG,"sign raw:"+sign_raw);
        web3MQSignDApp.sendSignRequest(sign_raw, wallet_address, false, new SendBridgeMessageCallback() {
            @Override
            public void onReceived() {

            }

            @Override
            public void onFail() {

            }

            @Override
            public void onTimeout() {

            }
        });
        Log.i(TAG,"----Send Sign Request End----");
    }

}