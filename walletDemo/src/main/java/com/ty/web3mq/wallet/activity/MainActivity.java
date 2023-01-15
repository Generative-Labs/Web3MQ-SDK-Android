package com.ty.web3mq.wallet.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private WalletSignFragment walletSignFragment;
    private FragmentTransaction transaction;
    private String api_key = "rkkJARiziBQCscgg";
    private String dAppID = "web3MQ_test_wallet:wallet";
//    private String ETH_ADDRESS = "0xa7F31Db454fE3c36c7Bb186d209fF7F433aE0314";
//    private String ETH_PRV_KEY = "b189f059bddf6d87deb45e8c31fa93921f87af3d1849064aa1cad0fef35a3666";
    private String ETH_ADDRESS = "0x54277Ee3b362C2E0eeb8D9D3aEe48840C3fD3cBd";
    private String ETH_PRV_KEY = "b438f33473ec9274c91cfb7900f35c3d86f415f3bca2c54f99d4560802b1489a";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Web3MQClient.getInstance().init(this,api_key);
        setContentView(R.layout.activity_main);
        transaction = getSupportFragmentManager().beginTransaction();
        walletSignFragment = WalletSignFragment.getInstance();
        initView();
        setListener();
//        Web3MQSign.getInstance().generate25519KeyPairTest();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Uri uri = getIntent().getData();
        if(uri!=null){
            String action = uri.getQueryParameter("action");
            Log.i(TAG,"url action:"+action);
            if(action.equals("connect")){
                String topicId = uri.getQueryParameter("topicId");
                String iconUrl = uri.getQueryParameter("iconUrl");
                String website = uri.getQueryParameter("website");
                String redirect = uri.getQueryParameter("redirect");
                Log.i(TAG,"topicId:"+topicId+" iconUrl:"+iconUrl+" website:"+website+" redirect:"+redirect);

                walletSignFragment.init(dAppID, topicId, new WalletInitCallback() {
                    @Override
                    public void onSuccess() {
                        BridgeMessageWalletInfo walletInfo = new BridgeMessageWalletInfo();
                        walletInfo.address = ETH_ADDRESS;
                        walletInfo.name = "Metamask";
                        walletInfo.description = "ETH wallet";
                        walletInfo.walletType = "eth";
                        walletSignFragment.showConnectBottomDialog(website,iconUrl,walletInfo);
                    }

                    @Override
                    public void onFail(String error) {
                        Toast.makeText(MainActivity.this,error,Toast.LENGTH_SHORT).show();
                    }
                });
                walletSignFragment.setOnConnectCallback(new OnConnectCallback() {

                    @Override
                    public void connectApprove() {
                        Log.i(TAG,"connectApprove");
                        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(redirect));
                        startActivity(intent);
                    }

                    @Override
                    public void connectReject() {
                        Log.i(TAG,"connectReject");
                        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(redirect));
                        startActivity(intent);
                    }
                });

                Web3MQSign.getInstance().setOnSignRequestMessageCallback(new OnSignRequestMessageCallback() {
                    @Override
                    public void onSignRequestMessage(BridgeMessageProposer proposer, String address, String sign_raw) {
                        walletSignFragment.showSignBottomDialog(proposer.url,proposer.iconUrl,address,sign_raw);
                    }
                });

                walletSignFragment.setOnSignCallback(new OnSignCallback() {

                    @Override
                    public String sign(String sign_raw) {
                        return CryptoUtils.signMessage(ETH_PRV_KEY,sign_raw);
                    }

                    @Override
                    public void signApprove() {
                        //TODO redirect 跳转
                    }

                    @Override
                    public void signReject() {
                        //TODO redirect 跳转
                    }
                });
            }
            if (!walletSignFragment.isAdded()) { // 先判断是否被add过
                transaction.add(R.id.fl_content, walletSignFragment).commitAllowingStateLoss(); // 隐藏当前的fragment，add下一个到Activity中
            }
        }
    }

    private void initView(){

    }

    private void setListener(){

    }

    public void hideSignFragment(){
        transaction.hide(walletSignFragment).commitAllowingStateLoss();
    }

}