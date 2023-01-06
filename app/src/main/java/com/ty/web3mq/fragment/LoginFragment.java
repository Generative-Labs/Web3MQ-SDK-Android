package com.ty.web3mq.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.Web3MQUser;
import com.ty.web3_mq.http.beans.UserInfo;
import com.ty.web3_mq.interfaces.ConnectCallback;
import com.ty.web3_mq.interfaces.GetUserinfoCallback;
import com.ty.web3_mq.interfaces.LoginCallback;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.Ed25519;
import com.ty.web3mq.R;
import com.ty.web3mq.activity.HomePageActivity;
import com.ty.web3mq.activity.LoginActivity;
import com.ty.web3mq.view.InputPwdView;


public class LoginFragment extends BaseFragment {
    private static LoginFragment instance;
    private static final String TAG = "LoginFragment";
    private Web3MQUser web3MQUser = Web3MQUser.getInstance();
    private InputPwdView view_input_pwd;
    private ConstraintLayout cl_pwd_error;
    private TextView tv_wallet_address;
    private Button btn_login;
    private ImageView iv_back;
    private static final String ETH_ADDRESS = "0x9E321289C659b17cd0A8c06FF760279e329f2eDF";
    private static final String ETH_PRV_KEY = "02a713332838cf01b29b335fc5c276c2fac52be353b01ff6a80f08f43949cad2";
    public static synchronized LoginFragment getInstance() {
        if (instance == null) {
            instance = new LoginFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_login,false);
    }


    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        initView();
        setListener();
    }

    private void initView() {
        cl_pwd_error = rootView.findViewById(R.id.cl_pwd_error);
        tv_wallet_address = rootView.findViewById(R.id.tv_wallet_address);
        view_input_pwd = rootView.findViewById(R.id.view_input_pwd);
        btn_login = rootView.findViewById(R.id.btn_login);
        iv_back = rootView.findViewById(R.id.iv_back);
        tv_wallet_address.setText(ETH_ADDRESS);
    }

    private void setListener() {
        view_input_pwd.setEmptyWatcher(new InputPwdView.EmptyWatcher() {
            @Override
            public void onEmptyChange(boolean empty) {
                btn_login.setEnabled(!empty);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity activity = (LoginActivity) getActivity();
                activity.switchContent(StartFragment.getInstance());
            }
        });
    }

    private void login(){
        String eth_prv_key = ETH_PRV_KEY.toLowerCase();
        String eth_address = ETH_ADDRESS.toLowerCase();
        String wallet_type = "eth";
        String password = view_input_pwd.getPwd();
        String magicString = web3MQUser.generateMagicString(wallet_type,eth_address,password);
        String keyGenerateSignature = web3MQUser.keyGenerateSign(eth_prv_key,eth_address,magicString);
        String mainPrivateKeyHex = CryptoUtils.SHA256_ENCODE(keyGenerateSignature);
        String mainPublicKeyHex = Ed25519.generatePublicKey(mainPrivateKeyHex);
        Log.i(TAG,"mainPrivateKeyHex:"+mainPrivateKeyHex);
        Log.i(TAG,"mainPublicKeyHex:"+mainPublicKeyHex);
        Web3MQUser.getInstance().getUserInfo(wallet_type, eth_address, new GetUserinfoCallback() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                loginRequest(userInfo.userid,wallet_type,eth_address,mainPrivateKeyHex,mainPublicKeyHex);
            }

            @Override
            public void onFail(String error) {
                Log.i(TAG,"GetUserInfo error "+error);
            }
        });
    }


//    private void login(){
//        String eth_prv_key = ETH_PRV_KEY;
//        String eth_address = ETH_ADDRESS;
//        String wallet_type = "eth";
//        String password = view_input_pwd.getPwd();
//        String magicString = web3MQUser.generateMagicString(wallet_type,eth_address,password);
//        //TODO 钱包签名
//        String keyGenerateSignature = web3MQUser.keyGenerateSign(eth_prv_key,eth_address,magicString);
//        String mainPrivateKeyHex = CryptoUtils.SHA256_ENCODE(keyGenerateSignature);
//        String mainPublicKeyHex = Ed25519.generatePublicKey(mainPrivateKeyHex);
//        Log.i(TAG,"mainPrivateKeyHex:"+mainPrivateKeyHex);
//        Log.i(TAG,"mainPublicKeyHex:"+mainPublicKeyHex);
//        Web3MQUser.getInstance().getUserInfo(wallet_type, eth_address, new GetUserinfoCallback() {
//            @Override
//            public void onSuccess(UserInfo userInfo) {
//                loginRequest(userInfo.userid,wallet_type,eth_address,mainPrivateKeyHex,mainPublicKeyHex);
//            }
//
//            @Override
//            public void onFail(String error) {
//                Log.i(TAG,"GetUserInfo error "+error);
//            }
//        });
//    }

    private void loginRequest(String user_id,String wallet_type,String wallet_address,String main_prv_key,String main_pubkey){
        Web3MQUser.getInstance().login(user_id, wallet_type, wallet_address, main_prv_key, main_pubkey, new LoginCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG,"login success");
                connect();
            }

            @Override
            public void onFail(String error) {
                Log.i(TAG,"login error "+error);
            }
        });
    }


    private void connect(){
        Web3MQClient.getInstance().startConnect(new ConnectCallback() {
            @Override
            public void onSuccess() {
                // connect success
                hideLoadingDialog();
                Intent intent = new Intent(getActivity(), HomePageActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFail(String error) {
                hideLoadingDialog();
                Toast.makeText(getActivity(),"connect fail", Toast.LENGTH_SHORT).show();
            }
        });
    }


}