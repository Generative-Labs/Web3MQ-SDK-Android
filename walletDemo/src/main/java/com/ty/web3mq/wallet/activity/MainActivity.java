package com.ty.web3mq.wallet.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.ty.module_sign.fragment.WalletSignFragment;
import com.ty.module_sign.interfaces.OnConnectCallback;
import com.ty.module_sign.interfaces.OnSignCallback;
import com.ty.module_sign.interfaces.WalletInitCallback;
import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.interfaces.BridgeConnectCallback;
import com.ty.web3_mq.interfaces.ConnectCallback;
import com.ty.web3_mq.interfaces.OnWebsocketClosedCallback;
import com.ty.web3_mq.utils.ConvertUtil;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.websocket.WebsocketConfig;
import com.ty.web3_mq.websocket.bean.ConnectRequest;
import com.ty.web3_mq.websocket.bean.SignRequest;
import com.ty.web3_mq.websocket.bean.sign.Participant;
import com.ty.web3_mq.websocket.bean.sign.Web3MQSession;
import com.ty.web3_mq.websocket.bean.sign.Web3MQSign;
import com.ty.web3_mq.interfaces.OnSignRequestMessageCallback;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.RandomUtils;
import com.ty.web3_mq.websocket.bean.BridgeMessageMetadata;
import com.ty.web3mq.wallet.R;
import com.yxing.ScanCodeActivity;
import com.yxing.ScanCodeConfig;
import com.yxing.def.ScanStyle;

import java.net.URLDecoder;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final boolean TEST_MODEL = false;
    private WalletSignFragment walletSignFragment;
    private String api_key = "rkkJARiziBQCscgg";
    private String dAppID = "web3MQ_test_wallet:wallet";
    private String ETH_ADDRESS = "0x99D3a969db5185C7980b5D77c9Bb47d88d16a1Fe";
    private String ETH_PRV_KEY = "70bd271d0cc2e41da5d74db3523155ab1a3d7b960a600320aa8a589b513011a5";
    private ImageView iv_scan;
//    private String handling_connect_uri = null;
    private ToggleButton btn_toggle;
    private boolean fromRemote = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=23) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},1);
        }

        Web3MQClient.getInstance().init(this,api_key);
        setContentView(R.layout.activity_main);
        btn_toggle = findViewById(R.id.btn_toggle);
        walletSignFragment = WalletSignFragment.getInstance();
        initView();
        setListener();
        Web3MQClient.getInstance().setOnWebsocketClosedCallback(new OnWebsocketClosedCallback() {
            @Override
            public void onClose() {
                //TODO showReconnectDialog
//                Web3MQClient.getInstance().reconnect();
//                Web3MQSign.getInstance().reconnect();
            }
        });
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fl_content, walletSignFragment).commitAllowingStateLoss();
        DefaultSPHelper.getInstance().clear();
        btn_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Web3MQClient.getInstance().switchUri(WebsocketConfig.WS_URL_TEST_NET);
                }else{
                    Web3MQClient.getInstance().switchUri(WebsocketConfig.WS_URL_DEV);
                }
            }
        });

//        if(getIntent().getData()==null){
//            checkPendingRequest();
//        }

//        DefaultSPHelper.getInstance().showSessionInfo();



    }

    private boolean checkPendingRequest(){
        Web3MQSession lastSession = Web3MQSign.getInstance().getLastSession();
        if(lastSession!=null){
            SignRequest signRequest = Web3MQSign.getInstance().checkPendingRequest(lastSession);
            if(signRequest!=null){
                Web3MQClient.getInstance().startConnect(new ConnectCallback() {
                    @Override
                    public void onSuccess() {
                        Web3MQSign.getInstance().switchSession(dAppID,lastSession);
                        walletSignFragment.showSignBottomDialog(signRequest.id,
                                Web3MQSign.getInstance().getCurrentSession().peerParticipant,
                                signRequest.getAddress(), signRequest.getSignRaw());

                    }

                    @Override
                    public void onFail(String error) {
                        Toast.makeText(MainActivity.this,"error",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void alreadyConnected() {
                        Web3MQSign.getInstance().switchSession(dAppID,lastSession);
                        walletSignFragment.showSignBottomDialog(signRequest.id,
                                Web3MQSign.getInstance().getCurrentSession().peerParticipant,
                                signRequest.getAddress(), signRequest.getSignRaw());
                    }
                });

                return true;
            }
        }
        return false;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.i(TAG,"onNewIntent");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume");
        Uri uri = getIntent().getData();
        if(uri != null){
            fromRemote = false;
            handleConnectUri(uri);
        }
    }

    private void initSignFragment(ConnectRequest request){
        walletSignFragment.showLoadingDialog();
        walletSignFragment.init(dAppID, request.topic,request.publicKey, new WalletInitCallback() {
            @Override
            public void initSuccess() {
                BridgeMessageMetadata metaData = new BridgeMessageMetadata();
                metaData.name = "Web3MQ Wallet";
                metaData.description = "ETH wallet";
                metaData.walletType = "eth";
                String icon = null;
                if(request.icons!=null&&request.icons.size()>0){
                    icon = request.icons.get(0);
                }
                walletSignFragment.hideLoadingDialog();
                walletSignFragment.showConnectBottomDialog(MainActivity.this,request.id, request.url,icon,metaData,ETH_ADDRESS);
            }

            @Override
            public void onFail(String error) {
                walletSignFragment.hideLoadingDialog();
                Toast.makeText(MainActivity.this,error,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleConnectUri(Uri uri) {
        if(uri ==null){
            return;
        }
//        Log.i(TAG,"handling_connect_uri:"+handling_connect_uri+" uri:"+uri);
//        if(!uri.toString().equals(handling_connect_uri)){
//            handling_connect_uri = uri.toString();
            ConnectRequest request = ConvertUtil.convertDeepLinkToConnectRequest(uri.toString());
            if(request.method!=null && request.method.equals("provider_authorization")){
                Web3MQSign.getInstance().walletBuildAndSaveSession(request);
                walletSignFragment.setOnConnectCallback(new OnConnectCallback() {

                    @Override
                    public void connectApprove() {
                        if(!fromRemote){
                            moveTaskToBack(true);
                        }

//                        if(!TextUtils.isEmpty(request.redirect)){
//                            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(request.redirect));
//                            startActivity(intent);
//                        }
                    }

                    @Override
                    public void connectReject() {
                        if(!fromRemote){
                            moveTaskToBack(true);
                        }
                        Log.i(TAG,"connectReject redirect:"+request.redirect);
//                        if(!TextUtils.isEmpty(request.redirect)){
//                            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(request.redirect));
//                            startActivity(intent);
//                        }
                    }
                });

                Web3MQSign.getInstance().setOnSignRequestMessageCallback(new OnSignRequestMessageCallback() {
                    @Override
                    public void onSignRequestMessage(String id, Participant participant, String address, String sign_raw) {
                        Log.i(TAG,"sign_raw: "+sign_raw);
                        if(TEST_MODEL){
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(RandomUtils.randomBoolean()){
                                        String signature = CryptoUtils.signMessage(ETH_PRV_KEY,sign_raw);
                                        Web3MQSign.getInstance().sendSignResponse(id,true,signature,false,null);
                                        if(!TextUtils.isEmpty(request.redirect)){
                                            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(request.redirect));
                                            startActivity(intent);
                                        }

                                    }else{
                                        Web3MQSign.getInstance().sendSignResponse(id,false,null,false,null);
                                        if(!TextUtils.isEmpty(request.redirect)){
                                            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(request.redirect));
                                            startActivity(intent);
                                        }
                                    }
                                }
                            },1000);
                        }else{
                            walletSignFragment.showSignBottomDialog(id, participant,address,sign_raw);
                        }
                    }
                });

                walletSignFragment.setOnSignCallback(new OnSignCallback() {

                    @Override
                    public String sign(String sign_raw) {
                        return CryptoUtils.signMessage(ETH_PRV_KEY,sign_raw);
                    }

                    @Override
                    public void signApprove(String redirect) {
//                        moveTaskToBack(true);
//                        if(!TextUtils.isEmpty(redirect)){
////                            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(redirect));
////                            startActivity(intent);
//                            moveTaskToBack(true);
//                        }

                        if(!fromRemote){
                            moveTaskToBack(true);
                        }

                    }

                    @Override
                    public void signReject(String redirect) {
                        if(!fromRemote){
                            moveTaskToBack(true);
                        }
//                        if(!TextUtils.isEmpty(redirect)){
////                            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(redirect));
////                            startActivity(intent);
//                            moveTaskToBack(true);
//                        }

                    }
                });
                initSignFragment(request);
            }
//        }

    }

    private void initView(){
        iv_scan = findViewById(R.id.iv_scan);
    }

    private void setListener(){
        iv_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //scan
                ScanCodeConfig.create(MainActivity.this)
                        .setStyle(ScanStyle.WECHAT)
                        //扫码成功是否播放音效  true ： 播放   false ： 不播放
                        .setPlayAudio(false)
                        .buidler()
                        //跳转扫码页   扫码页可自定义样式
                        .start(ScanCodeActivity.class);
            }
        });
    }

    public void hideSignFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(walletSignFragment).commitAllowingStateLoss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //接收扫码结果
        if(resultCode == RESULT_OK && requestCode == ScanCodeConfig.QUESTCODE && data != null){
            Bundle extras = data.getExtras();
            if(extras != null){
                String code = extras.getString(ScanCodeConfig.CODE_KEY);
                Log.i(TAG,"code:"+code);
                fromRemote = true;
                handleConnectUri(Uri.parse(code));

            }
        }
    }
}