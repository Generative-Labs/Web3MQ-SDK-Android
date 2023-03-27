package com.ty.web3mq.fragment;

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

import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.websocket.bean.sign.Web3MQSign;
import com.ty.web3_mq.Web3MQUser;
import com.ty.web3_mq.interfaces.ConnectCallback;
import com.ty.web3_mq.interfaces.LoginCallback;
import com.ty.web3_mq.interfaces.OnConnectCommandCallback;
import com.ty.web3_mq.interfaces.OnSignResponseMessageCallback;
import com.ty.web3_mq.interfaces.SignupCallback;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.Ed25519;
import com.ty.web3_mq.websocket.bean.BridgeMessageProposer;
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
    private String wallet_name,wallet_type,wallet_address;
    private boolean keyGenerateSignSuccess = false;
    private static final String YOUR_DOMAIN_URL = "https://www.web3mq.com";
    private static final String REDIRECT_URL = "web3mq_dapp://";
    private String mainPrivateKeyHex,user_id,pubkey_value,registerSignContent;
    private long signInTimeStamp;
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
                LoginActivity activity = (LoginActivity) getActivity();
                activity.switchContent(StartFragment.getInstance());
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
                Toast.makeText(getActivity(),"sign reject",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setWalletInfo(String wallet_name, String wallet_type,String wallet_address){
        this.wallet_name = wallet_name;
        this.wallet_type = wallet_type;
        this.wallet_address = wallet_address;
        Log.i(TAG,"setWalletInfo wallet_name: "+wallet_name+" wallet_type:"+wallet_type+" wallet_address:"+wallet_address);
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
        Web3MQSign.getInstance().sendSignRequest(sign_raw,wallet_address,false,null);
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
                sendConnectCommand();
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

    private void sendConnectCommand(){
        Web3MQClient.getInstance().sendConnectCommand(new OnConnectCommandCallback() {
            @Override
            public void onConnectCommandResponse() {
                hideLoading();
                Intent intent = new Intent(getActivity(), HomePageActivity.class);
                startActivity(intent);
            }
        });
    }


}