package com.ty.module_login.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.ty.common.activity.BaseActivity;
import com.ty.common.config.AppConfig;
import com.ty.common.config.Constants;
import com.ty.common.config.RouterPath;
import com.ty.module_login.R;
import com.ty.web3_mq.websocket.bean.sign.Web3MQSign;
import com.ty.web3_mq.Web3MQUser;
import com.ty.web3_mq.http.beans.UserInfo;
import com.ty.web3_mq.interfaces.BridgeConnectCallback;
import com.ty.web3_mq.interfaces.GetUserinfoCallback;
import com.ty.web3_mq.interfaces.OnConnectResponseCallback;
import com.ty.web3_mq.websocket.bean.BridgeMessageMetadata;

@Route(path = RouterPath.LOGIN_CONNECT_WALLET)
public class ConnectWalletActivity extends BaseActivity {
    private static final String TAG = "ConnectWalletActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_connect_wallet);
        connectWallet();
    }

    private void connectWallet(){
        Web3MQSign.getInstance().init(AppConfig.DAppID, new BridgeConnectCallback() {
            @Override
            public void onConnectCallback() {
                String deepLink = Web3MQSign.getInstance().generateConnectDeepLink(null,AppConfig.WebSite,AppConfig.REDIRECT_WALLET_CONNECT);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
                startActivity(intent);
            }
        });

        Web3MQSign.getInstance().setOnConnectResponseCallback(new OnConnectResponseCallback() {

            @Override
            public void onApprove(BridgeMessageMetadata walletInfo, String address) {
                String walletName = walletInfo.name;
                String walletType = walletInfo.walletType;
                Web3MQUser.getInstance().getUserInfo(walletType, address, new GetUserinfoCallback() {
                    @Override
                    public void onSuccess(UserInfo userInfo) {
                        // get user info success
                        Log.i(TAG,"getUserInfo onSuccess");
                        ARouter.getInstance().build(RouterPath.LOGIN_LOGIN).
                                withString(Constants.ROUTER_KEY_LOGIN_USER_ID,userInfo.userid).
                                withString(Constants.ROUTER_KEY_LOGIN_WALLET_TYPE,userInfo.wallet_type).
                                withString(Constants.ROUTER_KEY_LOGIN_WALLET_ADDRESS,userInfo.wallet_address.toLowerCase()).
                                navigation();
                    }

                    @Override
                    public void onUserNotRegister() {
                        // user not register
                        Log.i(TAG,"getUserInfo onUserNotRegister");
                        ARouter.getInstance().build(RouterPath.LOGIN_REGISTER).
                                withString(Constants.ROUTER_KEY_LOGIN_WALLET_NAME,walletName).
                                withString(Constants.ROUTER_KEY_LOGIN_WALLET_TYPE,walletType).
                                withString(Constants.ROUTER_KEY_LOGIN_WALLET_ADDRESS,address.toLowerCase()).
                                navigation();
                    }

                    @Override
                    public void onFail(String error) {
                        Log.i(TAG,"getUserInfo onFail");
                        Toast.makeText(ConnectWalletActivity.this,"getUserInfo error:"+error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onReject() {
                Toast.makeText(ConnectWalletActivity.this,"connect rejected",Toast.LENGTH_SHORT).show();
            }
        });
    }
}