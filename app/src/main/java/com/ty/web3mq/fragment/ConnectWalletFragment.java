package com.ty.web3mq.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.Web3MQUser;
import com.ty.web3_mq.http.beans.UserInfo;
import com.ty.web3_mq.interfaces.ConnectCallback;
import com.ty.web3_mq.interfaces.GetUserinfoCallback;
import com.ty.web3_mq.interfaces.LoginCallback;
import com.ty.web3_mq.interfaces.ResetPwdCallback;
import com.ty.web3_mq.interfaces.SignupCallback;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.Ed25519;
import com.ty.web3mq.R;
import com.ty.web3mq.activity.HomePageActivity;


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
        //TODO
        // 1.连接钱包，将wallet_type 和 wallet_address 存在本地
        // 2.如果是登录流程进来，跳转到登录页，注册流程进来跳转到注册
    }
}