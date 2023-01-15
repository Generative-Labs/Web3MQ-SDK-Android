package com.ty.web3mq.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.Web3MQSign;
import com.ty.web3_mq.Web3MQUser;
import com.ty.web3_mq.http.beans.UserInfo;
import com.ty.web3_mq.interfaces.BridgeConnectCallback;
import com.ty.web3_mq.interfaces.ConnectCallback;
import com.ty.web3_mq.interfaces.GetUserinfoCallback;
import com.ty.web3_mq.interfaces.OnConnectResponseCallback;
import com.ty.web3_mq.websocket.bean.BridgeMessageWalletInfo;
import com.ty.web3mq.R;
import com.ty.web3mq.activity.LoginActivity;
import com.ty.web3mq.utils.AppConfig;


public class ConnectWalletFragment extends BaseFragment {
    private static ConnectWalletFragment instance;
    private static final String TAG = "ConnectWalletFragment";
    private Web3MQUser web3MQUser = Web3MQUser.getInstance();
    //user1
    public static synchronized ConnectWalletFragment getInstance() {
        if (instance == null) {
            instance = new ConnectWalletFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_connect_wallet,false);
    }


    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        connectWallet();
    }

    private void connectWallet(){
        Web3MQClient.getInstance().startConnect(new ConnectCallback() {
            @Override
            public void onSuccess() {
                Web3MQSign.getInstance().init(AppConfig.DAppID, new BridgeConnectCallback() {
                    @Override
                    public void onConnectCallback() {
                        String deepLink = Web3MQSign.getInstance().generateConnectDeepLink(null,"www.web3mq.com","web3mq_dapp://");
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onFail(String error) {

            }
        });

        Web3MQSign.getInstance().setOnConnectResponseCallback(new OnConnectResponseCallback() {
            @Override
            public void onApprove(BridgeMessageWalletInfo walletInfo) {
                String walletName = walletInfo.name;
                String walletType = walletInfo.walletType;
                String walletAddress = walletInfo.address;
                Web3MQUser.getInstance().getUserInfo(walletType, walletAddress, new GetUserinfoCallback() {
                    @Override
                    public void onSuccess(UserInfo userInfo) {
                        // 有用户信息，跳转到登录
                        Log.i(TAG,"getUserInfo onSuccess");
                        LoginActivity activity = (LoginActivity) getActivity();
                        LoginFragment loginFragment = LoginFragment.getInstance();
                        loginFragment.setUserInfo(userInfo.userid, userInfo.wallet_type, userInfo.wallet_address);
                        activity.switchContent(loginFragment);
                    }

                    @Override
                    public void onUserNotRegister() {
                        // 没有用户信息，跳转到注册
                        Log.i(TAG,"getUserInfo onUserNotRegister");
                        LoginActivity activity = (LoginActivity) getActivity();
                        RegisterFragment registerFragment = RegisterFragment.getInstance();
                        registerFragment.setWalletInfo(walletName,walletType,walletAddress.toLowerCase());
                        activity.switchContent(registerFragment);
                    }

                    @Override
                    public void onFail(String error) {
                        Log.i(TAG,"getUserInfo onFail");
                        Toast.makeText(getActivity(),"getUserInfo error:"+error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onReject() {
                Toast.makeText(getActivity(),"connect rejected",Toast.LENGTH_SHORT).show();
            }
        });
    }
}