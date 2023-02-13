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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.ty.module_sign.fragment.WalletSignFragment;
import com.ty.module_sign.interfaces.OnConnectCallback;
import com.ty.module_sign.interfaces.OnSignCallback;
import com.ty.module_sign.interfaces.WalletInitCallback;
import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.Web3MQSign;
import com.ty.web3_mq.interfaces.OnSignRequestMessageCallback;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.RandomUtils;
import com.ty.web3_mq.websocket.bean.BridgeMessageProposer;
import com.ty.web3_mq.websocket.bean.BridgeMessageWalletInfo;
import com.ty.web3mq.wallet.R;
import com.yxing.ScanCodeActivity;
import com.yxing.ScanCodeConfig;
import com.yxing.def.ScanStyle;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final boolean TEST_MODEL = false;
    private WalletSignFragment walletSignFragment;
    private String api_key = "rkkJARiziBQCscgg";
    private String dAppID = "web3MQ_test_wallet:wallet";
    private String ETH_ADDRESS = "0x54277Ee3b362C2E0eeb8D9D3aEe48840C3fD3cBd";
    private String ETH_PRV_KEY = "b438f33473ec9274c91cfb7900f35c3d86f415f3bca2c54f99d4560802b1489a";
//    private String ETH_ADDRESS = "0x9b6a5A1dD55Ea481f76B782862e7df2977dFfE6C";
//    private String ETH_PRV_KEY = "132f28f780af6516ecb1d31bc836293b5a46a0cc2cbb812579dcd1334cca7c7b";

//    private String ETH_ADDRESS = "0xa9731372887B49Ee93a063547975809aFBdD47A8";
//    private String ETH_PRV_KEY = "5e82803521388cc0fa8a1013c4fa414dfe489940f97c647dbf066c662a70d54e";

//    private String ETH_ADDRESS = "0xa9731372887B49Ee93a063547975809aFBdD47A8";
//    private String ETH_PRV_KEY = "5e82803521388cc0fa8a1013c4fa414dfe489940f97c647dbf066c662a70d54e";

    private String redirect,topicId,iconUrl,website,ed25519Pubkey;
    private ImageView iv_scan;
    private String handling_connect_uri = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=23) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},1);
        }

        Web3MQClient.getInstance().init(this,api_key);
        setContentView(R.layout.activity_main);

        walletSignFragment = WalletSignFragment.getInstance();
        initView();
        setListener();

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.i(TAG,"onNewIntent");
//        String action = intent.getData().getQueryParameter("action");
//        Log.i(TAG,"onNewIntent url action:"+action);
//        Uri uri = intent.getData();
//        handleUri(uri);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume");
        Uri uri = getIntent().getData();
        handleConnectUri(uri);
    }

    private void initSignFragment(){
        if (walletSignFragment.isAdded()){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(walletSignFragment).commitAllowingStateLoss();
        }
        walletSignFragment.init(dAppID, topicId,ed25519Pubkey, new WalletInitCallback() {
            @Override
            public void onSuccess() {
                BridgeMessageWalletInfo walletInfo = new BridgeMessageWalletInfo();
                walletInfo.address = ETH_ADDRESS;
                walletInfo.name = "Metamask";
                walletInfo.description = "ETH wallet";
                walletInfo.walletType = "eth";
                if(TEST_MODEL) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "change handleUri:" + false);
                            if(RandomUtils.randomBoolean()) {
                                Web3MQSign.getInstance().sendConnectResponse(true, walletInfo, false);
                                if(redirect!=null){
                                    Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(redirect));
                                    startActivity(intent);
                                }
                            }else{
                                Web3MQSign.getInstance().sendConnectResponse(false, null, false);
                                if(!TextUtils.isEmpty(redirect)){
                                    Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(redirect));
                                    startActivity(intent);
                                }
                            }
                        }
                    }, 1000);
                }else{
                    walletSignFragment.showConnectBottomDialog(MainActivity.this, website,iconUrl,walletInfo);
                }

            }

            @Override
            public void onFail(String error) {
                Toast.makeText(MainActivity.this,error,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleConnectUri(Uri uri){
        if(uri == null){
            return;
        }
        Log.i(TAG,"handling_connect_uri:"+handling_connect_uri+" uri:"+uri.toString());
        if(!uri.toString().equals(handling_connect_uri)){
            handling_connect_uri = uri.toString();
            String action = uri.getQueryParameter("action");
            Log.i(TAG,"url action:"+action);
            if(action.equals("connect")){
                topicId = uri.getQueryParameter("topicId");
                iconUrl = uri.getQueryParameter("iconUrl");
                website = uri.getQueryParameter("website");
                redirect = uri.getQueryParameter("redirect");
                ed25519Pubkey = uri.getQueryParameter("ed25519Pubkey");
                Log.i(TAG,"topicId:"+topicId+" iconUrl:"+iconUrl+" website:"+website+" redirect:"+redirect);

                walletSignFragment.setOnConnectCallback(new OnConnectCallback() {

                    @Override
                    public void connectApprove() {
                        Log.i(TAG,"connectApprove redirect:"+redirect);
                        if(!TextUtils.isEmpty(redirect)){
                            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(redirect));
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void connectReject() {
                        Log.i(TAG,"connectReject redirect:"+redirect);
                        if(!TextUtils.isEmpty(redirect)){
                            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(redirect));
                            startActivity(intent);
                        }
                    }
                });

                Web3MQSign.getInstance().setOnSignRequestMessageCallback(new OnSignRequestMessageCallback() {
                    @Override
                    public void onSignRequestMessage(BridgeMessageProposer proposer, String address, String sign_raw,String requestId, String userInfo) {
                        Log.i(TAG,"sign_raw: "+sign_raw);
                        if(TEST_MODEL){
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(RandomUtils.randomBoolean()){
                                        String signature = CryptoUtils.signMessage(ETH_PRV_KEY,sign_raw);
                                        Web3MQSign.getInstance().sendSignResponse(true,signature,requestId,userInfo,false);
                                        if(!TextUtils.isEmpty(redirect)){
                                            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(redirect));
                                            startActivity(intent);
                                        }

                                    }else{
                                        Web3MQSign.getInstance().sendSignResponse(false,null,requestId,userInfo,false);
                                        if(!TextUtils.isEmpty(redirect)){
                                            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(redirect));
                                            startActivity(intent);
                                        }
                                    }
                                }
                            },1000);
                        }else{
                            walletSignFragment.showSignBottomDialog(proposer,address,sign_raw,requestId,userInfo);
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
                        if(!TextUtils.isEmpty(redirect)){
                            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(redirect));
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void signReject(String redirect) {
                        if(!TextUtils.isEmpty(redirect)){
                            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(redirect));
                            startActivity(intent);
                        }
                    }
                });
                initSignFragment();

            }
            if (!walletSignFragment.isAdded()) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.fl_content, walletSignFragment).commitAllowingStateLoss();
            }
        }
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
                handleConnectUri(Uri.parse(code));
            }
        }
    }
}