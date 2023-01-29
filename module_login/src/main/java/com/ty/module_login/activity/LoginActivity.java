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
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.ty.common.activity.BaseActivity;
import com.ty.common.config.RouterPath;
import com.ty.module_login.ModuleLogin;
import com.ty.module_login.R;
import com.ty.module_login.view.InputPwdView;
import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.Web3MQSign;
import com.ty.web3_mq.Web3MQUser;
import com.ty.web3_mq.interfaces.LoginCallback;
import com.ty.web3_mq.interfaces.OnConnectCommandCallback;
import com.ty.web3_mq.interfaces.OnSignResponseMessageCallback;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.Ed25519;
import com.ty.web3_mq.websocket.bean.BridgeMessageProposer;

@Route(path = RouterPath.LOGIN_LOGIN)
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    private Web3MQUser web3MQUser = Web3MQUser.getInstance();
    private InputPwdView view_input_pwd;
    private ConstraintLayout cl_pwd_error;
    private TextView tv_wallet_address;
    private Button btn_login;
    private ImageView iv_back;
    @Autowired
    String user_id;
    @Autowired
    String wallet_type;
    @Autowired
    String wallet_address;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_login);
        initView();
        setListener();
    }

    private void initView() {
        cl_pwd_error = findViewById(R.id.cl_pwd_error);
        tv_wallet_address = findViewById(R.id.tv_wallet_address);
        view_input_pwd = findViewById(R.id.view_input_pwd);
        btn_login = findViewById(R.id.btn_login);
        iv_back = findViewById(R.id.iv_back);
        if(wallet_address!=null){
            tv_wallet_address.setText(wallet_address);
        }
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
                showLoading();
                sign();
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void sign(){
        String password = view_input_pwd.getPwd();
        String magicString = web3MQUser.generateMagicString(wallet_type,wallet_address,password);
        String signContent = web3MQUser.getKeyGenerateSignContent(wallet_address,magicString);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Web3MQSign.getInstance().generateSignDeepLink()));
        startActivity(intent);

        sendSign(signContent);
        Web3MQSign.getInstance().setOnSignResponseMessageCallback(new OnSignResponseMessageCallback() {
            @Override
            public void onApprove(String signature) {
                Log.i(TAG,"signature:"+signature);
                String mainPrivateKeyHex = CryptoUtils.SHA256_ENCODE(signature);
                String mainPublicKeyHex = Ed25519.generatePublicKey(mainPrivateKeyHex);
                Log.i(TAG,"mainPrivateKeyHex:"+mainPrivateKeyHex);
                Log.i(TAG,"mainPublicKeyHex:"+mainPublicKeyHex);
                loginRequest(user_id,wallet_type,wallet_address,mainPrivateKeyHex,mainPublicKeyHex);
            }

            @Override
            public void onReject() {
                hideLoading();
                Toast.makeText(LoginActivity.this,"sign reject",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendSign(String sign_raw){
        BridgeMessageProposer proposer = new BridgeMessageProposer();
        proposer.name = "Web3MQ_DAPP_DEMO";
        proposer.url = "www.web3mq_dapp.com";
        proposer.redirect = "web3mq_dapp_login://";
        Web3MQSign.getInstance().sendSignRequest(proposer,sign_raw,wallet_address,System.currentTimeMillis()+"","",false);
    }

//    public void setUserInfo(String userid,String wallet_type,String wallet_address){
//        this.userID = userid;
//        this.wallet_type = wallet_type;
//        this.wallet_address = wallet_address;
//        Log.i(TAG,"setUserInfo userid:"+userid+" wallet_type:"+wallet_type+" wallet_address:"+wallet_address);
//    }

    private void loginRequest(String user_id,String wallet_type,String wallet_address,String main_prv_key,String main_pubkey){
        Web3MQUser.getInstance().login(user_id, wallet_type, wallet_address, main_prv_key, main_pubkey, new LoginCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG,"login success");
                sendConnectCommand();
            }

            @Override
            public void onFail(String error) {
                hideLoading();
                Log.i(TAG,"login error "+error);
            }
        });
    }

    private void sendConnectCommand(){
        Web3MQClient.getInstance().sendConnectCommand(new OnConnectCommandCallback() {
            @Override
            public void onConnectCommandResponse() {
                hideLoading();
                ModuleLogin.getInstance().getOnLoginSuccessCallback().onLoginSuccess();
                //TODO
            }
        });
    }
}
