package com.ty.web3mq.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.ty.web3_mq.interfaces.SignupCallback;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.Ed25519;
import com.ty.web3mq.R;
import com.ty.web3mq.activity.HomePageActivity;
import com.ty.web3mq.activity.LoginActivity;
import com.ty.web3mq.view.InputPwdView;


public class RegisterFragment extends BaseFragment {
    private static RegisterFragment instance;
    private static final String TAG = "LoginFragment";
    private Web3MQUser web3MQUser = Web3MQUser.getInstance();
    private ConstraintLayout cl_pwd_error;
    private TextView tv_wallet_address;
    private Button btn_create_new_user;
    private InputPwdView view_create_pwd, view_confirm_pwd;
    private ImageView iv_back;
    private static final String ETH_ADDRESS = "0x9E321289C659b17cd0A8c06FF760279e329f2eDF";
    private static final String ETH_PRV_KEY = "02a713332838cf01b29b335fc5c276c2fac52be353b01ff6a80f08f43949cad2";
    public static synchronized RegisterFragment getInstance() {
        if (instance == null) {
            instance = new RegisterFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_register,false);
    }


    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        initView();
        setListener();
    }

    private void initView() {
        view_create_pwd = rootView.findViewById(R.id.view_create_pwd);
        view_confirm_pwd = rootView.findViewById(R.id.view_confirm_pwd);
        cl_pwd_error = rootView.findViewById(R.id.cl_pwd_error);
        tv_wallet_address = rootView.findViewById(R.id.tv_wallet_address);
        btn_create_new_user = rootView.findViewById(R.id.btn_create_new_user);
        iv_back = rootView.findViewById(R.id.iv_back);
        tv_wallet_address.setText(ETH_ADDRESS);
    }

    private void setListener() {
        InputPwdView.EmptyWatcher watcher = new InputPwdView.EmptyWatcher() {
            @Override
            public void onEmptyChange(boolean empty) {
                if(empty){
                    btn_create_new_user.setEnabled(false);
                }else if(!view_create_pwd.getPwd().equals(view_confirm_pwd.getPwd())){
                    btn_create_new_user.setEnabled(false);
                }else{
                    btn_create_new_user.setEnabled(true);
                }
            }
        };
        view_create_pwd.setEmptyWatcher(watcher);

        view_confirm_pwd.setEmptyWatcher(watcher);

        btn_create_new_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sign_up();
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

    private void sign_up(){
        String eth_prv_key = ETH_PRV_KEY.toLowerCase();
        String eth_address = ETH_ADDRESS.toLowerCase();
        String wallet_type = "eth";
        String password = view_create_pwd.getPwd();
        String magicString = web3MQUser.generateMagicString(wallet_type,eth_address,password);
        Log.i(TAG,"magicString:"+magicString);
        //TODO 钱包签名生成私钥
        String keyGenerateSignature = web3MQUser.keyGenerateSign(eth_prv_key,eth_address,magicString);
        Log.i(TAG,"keyGenerateSignature:"+keyGenerateSignature);
        String mainPrivateKeyHex = CryptoUtils.SHA256_ENCODE(keyGenerateSignature);
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
        //TODO 注册接口签名
        String[] sign = web3MQUser.registerSign(eth_prv_key,wallet_type_name,eth_address,your_domain_url,nonce_content);
        web3MQUser.signUp(user_id, wallet_type, eth_address, mainPrivateKeyHex, sign[0], sign[1], time_stamp, new SignupCallback() {
            @Override
            public void onSuccess() {
                loginRequest(user_id,wallet_type,eth_address,mainPrivateKeyHex,pubkey_value);
            }

            @Override
            public void onFail(String error) {
                hideLoadingDialog();
                Log.i(TAG,"signUp fail: "+error);
            }
        });
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