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
import com.ty.web3_mq.websocket.bean.BridgeMessageProposer;
import com.ty.web3_mq.websocket.bean.BridgeMessageWalletInfo;
import com.ty.web3mq.wallet.R;
import com.yxing.ScanCodeActivity;
import com.yxing.ScanCodeConfig;
import com.yxing.def.ScanStyle;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private WalletSignFragment walletSignFragment;
    private FragmentTransaction transaction;
    private String api_key = "rkkJARiziBQCscgg";
    private String dAppID = "web3MQ_test_wallet:wallet";
//    private String ETH_ADDRESS = "0xa7F31Db454fE3c36c7Bb186d209fF7F433aE0314";
//    private String ETH_PRV_KEY = "b189f059bddf6d87deb45e8c31fa93921f87af3d1849064aa1cad0fef35a3666";
//    private String ETH_ADDRESS = "0x54277Ee3b362C2E0eeb8D9D3aEe48840C3fD3cBd";
//    private String ETH_PRV_KEY = "b438f33473ec9274c91cfb7900f35c3d86f415f3bca2c54f99d4560802b1489a";
//    private String ETH_ADDRESS = "0xa9731372887B49Ee93a063547975809aFBdD47A8";
//    private String ETH_PRV_KEY = "5e82803521388cc0fa8a1013c4fa414dfe489940f97c647dbf066c662a70d54e";
    private String ETH_ADDRESS = "0x3a71d76262729144B0E833AF463Ed459179327aF";
    private String ETH_PRV_KEY = "5e2d12df322724fad44de516bfe3be420d356417b05ca044dbe8ad4b500f9018";
    private String redirect,topicId,iconUrl,website,ed25519Pubkey;
    private ImageView iv_scan;
    private boolean handleUri = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=23) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},1);
        }

        Web3MQClient.getInstance().init(this,api_key);
        setContentView(R.layout.activity_main);
        transaction = getSupportFragmentManager().beginTransaction();
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
        handleUri(uri);
    }

    private void initSignFragment(){
        if (!walletSignFragment.isAdded()){
            walletSignFragment.init(dAppID, topicId,ed25519Pubkey, new WalletInitCallback() {
                @Override
                public void onSuccess() {
                    BridgeMessageWalletInfo walletInfo = new BridgeMessageWalletInfo();
                    walletInfo.address = ETH_ADDRESS;
                    walletInfo.name = "Metamask";
                    walletInfo.description = "ETH wallet";
                    walletInfo.walletType = "eth";
                    //TODO
//                    walletSignFragment.showConnectBottomDialog(website,iconUrl,walletInfo);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handleUri = false;
                            Log.i(TAG, "change handleUri:"+ false);
                            Web3MQSign.getInstance().sendConnectResponse(true,walletInfo,false);
                            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(redirect));
                            startActivity(intent);
                        }
                    },5000);

                }

                @Override
                public void onFail(String error) {
                    Toast.makeText(MainActivity.this,error,Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            BridgeMessageWalletInfo walletInfo = new BridgeMessageWalletInfo();
            walletInfo.address = ETH_ADDRESS;
            walletInfo.name = "Metamask";
            walletInfo.description = "ETH wallet";
            walletInfo.walletType = "eth";
            //TODO
//            walletSignFragment.showConnectBottomDialog(website,iconUrl,walletInfo);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    handleUri = false;
                    Log.i(TAG, "change handleUri:"+ false);
                    Web3MQSign.getInstance().sendConnectResponse(true,walletInfo,false);
                    Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(redirect));
                    startActivity(intent);
                }
            },5000);
        }
    }

    private void handleUri(Uri uri){
        Log.i(TAG,"handleUri:"+handleUri);
        if(uri!=null&& !handleUri){
            handleUri = true;
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
                        //TODO
//                       walletSignFragment.showSignBottomDialog(proposer,address,sign_raw,requestId,userInfo);
                       new Handler().postDelayed(new Runnable() {
                           @Override
                           public void run() {
                               String signature = CryptoUtils.signMessage(ETH_PRV_KEY,sign_raw);
                               Web3MQSign.getInstance().sendSignResponse(true,signature,requestId,userInfo,false);
                               Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(redirect));
                               startActivity(intent);
                           }
                       },5000);
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
                handleUri(Uri.parse(code));
            }
        }
    }
}