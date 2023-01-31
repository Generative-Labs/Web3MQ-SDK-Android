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
import com.ty.web3_mq.interfaces.ConnectCallback;
import com.ty.web3_mq.interfaces.LoginCallback;
import com.ty.web3_mq.interfaces.ResetPwdCallback;
import com.ty.web3_mq.interfaces.SignupCallback;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.Ed25519;
import com.ty.web3mq.R;
import com.ty.web3mq.activity.HomePageActivity;


public class Login2Fragment extends BaseFragment {
    private static Login2Fragment instance;
    private static final String TAG = "LoginFragment";
    private Button btn_connect,btn_login,btn_sign,btn_reset_pwd;
    private EditText et_eth_address, et_eth_prv_key;
    private Web3MQUser web3MQUser = Web3MQUser.getInstance();
    //user1
//    private static final String ETH_ADDRESS = "0x53e75d284fa2aDB029Aed70421F2fa31a713b2bd";
//    private static final String ETH_PRV_KEY = "8222013c4c1e9100fc1872c386bacaf214bda01eb6e958f33113dc99c55564ee";
    //user2
    private static final String ETH_ADDRESS = "0x542a35C0ceCEce87C2E6036047EB9bf542f59ac0";
    private static final String ETH_PRV_KEY = "d5cb4b2ac9a2223f1edc9300784e580001b776c9d58ccb5ecfa45b2e0fddf523";
    public static synchronized Login2Fragment getInstance() {
        if (instance == null) {
            instance = new Login2Fragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_login2,false);
    }


    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        initView();
        setListener();
    }

    private void initView() {
        btn_connect = rootView.findViewById(R.id.btn_login_local);
        btn_login = rootView.findViewById(R.id.btn_login);
        btn_sign = rootView.findViewById(R.id.btn_sign);
        et_eth_address = rootView.findViewById(R.id.et_eth_address);
        et_eth_prv_key = rootView.findViewById(R.id.et_eth_prv_key);
        btn_reset_pwd = rootView.findViewById(R.id.btn_reset_pwd);
        et_eth_address.setText(ETH_ADDRESS);
        et_eth_prv_key.setText(ETH_PRV_KEY);
    }

    private void setListener() {
        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                connect();
            }
        });
        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sign_up();
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        btn_reset_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPwd();
            }
        });
    }

    private void sign_up(){
        showLoading();
        String eth_prv_key = ETH_PRV_KEY;
        String eth_address = ETH_ADDRESS;
        String wallet_type = "eth";
        String password = "123456";
        String magicString = web3MQUser.generateMagicString(wallet_type,eth_address,password);
        Log.i(TAG,"magicString:"+magicString);
        //wallet sign for generate key
        String keyGenerateSignature = web3MQUser.getKeyGenerateSignContent(eth_address,magicString);
        Log.i(TAG,"keyGenerateSignature:"+keyGenerateSignature);
        String mainPrivateKeyHex = CryptoUtils.SHA256_ENCODE(keyGenerateSignature);
        //wallet sign for register
        String wallet_type_name = "Ethereum";
        String your_domain_url = "https://www.web3mq.com";
        String user_id = web3MQUser.generateUserID(wallet_type, eth_address);
        Log.i(TAG,"user_id:"+user_id);
        String pubkey_type = "ed25519";
        String pubkey_value = Ed25519.generatePublicKey(mainPrivateKeyHex);
        Log.i(TAG,"pubkey_value:"+pubkey_value + "pubkey_value len:"+pubkey_value.length());
        long time_stamp = System.currentTimeMillis();
        Log.i(TAG,"sha3_224 before:"+user_id+pubkey_type + pubkey_value + wallet_type + eth_address + time_stamp);
        String nonce_content = CryptoUtils.SHA3_ENCODE(user_id+pubkey_type + pubkey_value + wallet_type + eth_address + time_stamp);
        Log.i(TAG,"sha3_224 nonce_content:"+nonce_content);
        String[] sign = web3MQUser.registerSign(eth_prv_key,wallet_type_name,eth_address,your_domain_url,nonce_content);
        web3MQUser.signUp(user_id, wallet_type, eth_address, mainPrivateKeyHex, sign[0], sign[1], time_stamp, new SignupCallback() {
            @Override
            public void onSuccess() {
                loginRequest(user_id,wallet_type,eth_address,mainPrivateKeyHex,pubkey_value);
            }

            @Override
            public void onFail(String error) {
                hideLoading();
                Log.i(TAG,"signUp fail: "+error);
            }
        });
    }

    private void login(){
//        String eth_prv_key = et_eth_prv_key.getText().toString().toLowerCase();
//        String eth_address = et_eth_address.getText().toString().toLowerCase();

        Log.e(TAG,"ETH_ADDRESS "+ETH_ADDRESS);
        Log.e(TAG,"et_eth_address "+et_eth_address.getText().toString().toLowerCase());
        String eth_prv_key = ETH_PRV_KEY;
        String eth_address = ETH_ADDRESS;
        String wallet_type = "eth";
        String password = "123456";
        String magicString = web3MQUser.generateMagicString(wallet_type,eth_address,password);
        String keyGenerateSignature = web3MQUser.getKeyGenerateSignContent(eth_address,magicString);
        String mainPrivateKeyHex = CryptoUtils.SHA256_ENCODE(keyGenerateSignature);
        String mainPublicKeyHex = Ed25519.generatePublicKey(mainPrivateKeyHex);
        Log.i(TAG,"mainPrivateKeyHex:"+mainPrivateKeyHex);
        Log.i(TAG,"mainPublicKeyHex:"+mainPublicKeyHex);
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
    }

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

    private void resetPwd(){
        showLoading();
        String eth_prv_key = et_eth_prv_key.getText().toString().toLowerCase();
        String eth_address = et_eth_address.getText().toString().toLowerCase();
        String wallet_type = "eth";
        String password = "12345";
        String magicString = web3MQUser.generateMagicString(wallet_type,eth_address,password);
        Log.i(TAG,"magicString:"+magicString);
        //wallet sign for generate key
        String keyGenerateSignature = web3MQUser.getKeyGenerateSignContent(eth_address,magicString);
        Log.i(TAG,"keyGenerateSignature:"+keyGenerateSignature);
        String mainPrivateKeyHex = CryptoUtils.SHA256_ENCODE(keyGenerateSignature);
        //wallet sign for register
        String wallet_type_name = "Ethereum";
        String your_domain_url = "https://www.web3mq.com";
        String user_id = web3MQUser.generateUserID(wallet_type, eth_address);
        Log.i(TAG,"user_id:"+user_id);
        String pubkey_type = "ed25519";
        String pubkey_value = Ed25519.generatePublicKey(mainPrivateKeyHex);
        Log.i(TAG,"pubkey_value:"+pubkey_value + "pubkey_value len:"+pubkey_value.length());
        long time_stamp = System.currentTimeMillis();
        Log.i(TAG,"sha3_224 before:"+user_id+pubkey_type + pubkey_value + wallet_type + eth_address + time_stamp);
        String nonce_content = CryptoUtils.SHA3_ENCODE(user_id+pubkey_type + pubkey_value + wallet_type + eth_address + time_stamp);
        Log.i(TAG,"sha3_224 nonce_content:"+nonce_content);
        String[] sign = web3MQUser.resetPwdSign(eth_prv_key,wallet_type_name,eth_address,your_domain_url,nonce_content);
        web3MQUser.resetPwd(user_id, wallet_type, eth_address, mainPrivateKeyHex, sign[0], sign[1], time_stamp, new ResetPwdCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG,"reset pwd success");
                hideLoading();
            }

            @Override
            public void onFail(String error) {
                hideLoading();
                Log.i(TAG,"reset pwd fail: "+error);
            }
        });
    }

    private void connect(){
        Web3MQClient.getInstance().startConnect(new ConnectCallback() {
            @Override
            public void onSuccess() {
                // connect success
                hideLoading();
                Intent intent = new Intent(getActivity(), HomePageActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFail(String error) {
                hideLoading();
                Toast.makeText(getActivity(),"connect fail", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void alreadyConnected() {

            }
        });
    }


}