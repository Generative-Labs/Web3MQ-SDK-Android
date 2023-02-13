package com.ty.module_login.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.ty.common.activity.BaseActivity;
import com.ty.common.config.RouterPath;
import com.ty.module_login.ModuleLogin;
import com.ty.module_login.R;
import com.ty.module_login.view.InputPwdView;
import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.Web3MQSign;
import com.ty.web3_mq.Web3MQUser;
import com.ty.web3_mq.interfaces.ConnectCallback;
import com.ty.web3_mq.interfaces.LoginCallback;
import com.ty.web3_mq.interfaces.OnConnectCommandCallback;
import com.ty.web3_mq.interfaces.OnSignResponseMessageCallback;
import com.ty.web3_mq.interfaces.SignupCallback;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.Ed25519;
import com.ty.web3_mq.websocket.bean.BridgeMessageProposer;

@Route(path = RouterPath.LOGIN_REGISTER)
public class RegisterActivity extends BaseActivity {
    private static final String TAG = "LoginFragment";
    private Web3MQUser web3MQUser = Web3MQUser.getInstance();
    private ConstraintLayout cl_pwd_error;
    private TextView tv_wallet_address;
    private Button btn_create_new_user;
    private InputPwdView view_create_pwd, view_confirm_pwd;
    private ImageView iv_back;
    private boolean keyGenerateSignSuccess = false;
    private static final String YOUR_DOMAIN_URL = "https://www.web3mq.com";
    private static final String REDIRECT_URL = "web3mq_dapp_register://";
    private String mainPrivateKeyHex,user_id,pubkey_value,registerSignContent;
    private long signInTimeStamp;
    @Autowired
    String wallet_name;
    @Autowired
    String wallet_type;
    @Autowired
    String wallet_address;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_register);
        initView();
        setListener();
    }


    private void initView() {
        view_create_pwd = findViewById(R.id.view_create_pwd);
        view_confirm_pwd = findViewById(R.id.view_confirm_pwd);
        cl_pwd_error = findViewById(R.id.cl_pwd_error);
        tv_wallet_address = findViewById(R.id.tv_wallet_address);
        btn_create_new_user = findViewById(R.id.btn_create_new_user);
        iv_back = findViewById(R.id.iv_back);
        tv_wallet_address.setText(wallet_address);
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
                signKeyGenerate();
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Web3MQSign.getInstance().setOnSignResponseMessageCallback(new OnSignResponseMessageCallback() {
            @Override
            public void onApprove(String signature) {
                if(!keyGenerateSignSuccess){
                    //keyGenerate sign
                    mainPrivateKeyHex = CryptoUtils.SHA256_ENCODE(signature);
                    user_id = web3MQUser.generateUserID(wallet_type, wallet_address);
                    Log.i(TAG,"user_id:"+user_id);
                    String pubkey_type = "ed25519";
                    pubkey_value = Ed25519.generatePublicKey(mainPrivateKeyHex);
                    Log.i(TAG,"pubkey_value:"+pubkey_value + "pubkey_value len:"+pubkey_value.length());
                    signInTimeStamp = System.currentTimeMillis();
                    Log.i(TAG,"sha3_224 before:"+user_id+pubkey_type + pubkey_value + wallet_type + wallet_address + signInTimeStamp);
                    String nonce_content = CryptoUtils.SHA3_ENCODE(user_id+pubkey_type + pubkey_value + wallet_type + wallet_address + signInTimeStamp);
                    Log.i(TAG,"sha3_224 nonce_content:"+nonce_content);
                    registerSignContent = Web3MQUser.getInstance().getRegisterSignContent(wallet_name,wallet_address,YOUR_DOMAIN_URL,nonce_content);
                    keyGenerateSignSuccess = true;
                    sendSign(registerSignContent);
                }else {
                    //register sign
                    if(mainPrivateKeyHex==null || user_id==null || pubkey_value==null || registerSignContent== null || signInTimeStamp==0){
                        Log.e(TAG,"keyGenerateSign error");
                        return;
                    }
                    web3MQUser.signUp(user_id, wallet_type, wallet_address, mainPrivateKeyHex, registerSignContent, signature, signInTimeStamp, new SignupCallback() {
                        @Override
                        public void onSuccess() {
                            //user sign up success
                            loginRequest(user_id,wallet_type,wallet_address,mainPrivateKeyHex,pubkey_value);
                        }

                        @Override
                        public void onFail(String error) {
                            hideLoading();
                            Log.i(TAG,"signUp fail: "+error);
                        }
                    });
                }
            }

            @Override
            public void onReject() {
                hideLoading();
                Toast.makeText(RegisterActivity.this,"sign reject",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     *
     */
    private void signKeyGenerate(){
        String password = view_create_pwd.getPwd();
        String magicString = web3MQUser.generateMagicString(wallet_type,wallet_address,password);
        Log.i(TAG,"magicString:"+magicString);
        String keyGenerateContent = web3MQUser.getKeyGenerateSignContent(wallet_address,magicString);
        Log.i(TAG,"keyGenerateContent:"+keyGenerateContent);
        sendSign(keyGenerateContent);
    }

    private void sendSign(String sign_raw){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Web3MQSign.getInstance().generateSignDeepLink()));
        startActivity(intent);
        BridgeMessageProposer proposer = new BridgeMessageProposer();
        proposer.name = "Web3MQ_DAPP_DEMO";
        proposer.url = "www.web3mq_dapp.com";
        proposer.redirect = REDIRECT_URL;
        Web3MQSign.getInstance().sendSignRequest(proposer,sign_raw,wallet_address,System.currentTimeMillis()+"","",false);
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
//                sendConnectCommand();
                ModuleLogin.getInstance().getOnLoginSuccessCallback().onLoginSuccess();
            }

            @Override
            public void onFail(String error) {
                hideLoading();
                Toast.makeText(RegisterActivity.this,"connect fail", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void alreadyConnected() {

            }
        });
    }

//    private void sendConnectCommand(){
//        Web3MQClient.getInstance().sendConnectCommand(new OnConnectCommandCallback() {
//            @Override
//            public void onConnectCommandResponse() {
//                hideLoading();
//                ModuleLogin.getInstance().getOnLoginSuccessCallback().onLoginSuccess();
////                Intent intent = new Intent(RegisterActivity.this, HomePageActivity.class);
////                startActivity(intent);
//            }
//        });
//    }


}