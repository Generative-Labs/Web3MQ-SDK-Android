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
import com.ty.web3_mq.interfaces.SendBridgeMessageCallback;
import com.ty.web3_mq.websocket.bean.sign.Web3MQSign;
import com.ty.web3_mq.Web3MQUser;
import com.ty.web3_mq.interfaces.LoginCallback;
import com.ty.web3_mq.interfaces.OnConnectCommandCallback;
import com.ty.web3_mq.interfaces.OnSignResponseMessageCallback;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.Ed25519;
import com.ty.web3_mq.websocket.bean.BridgeMessageProposer;
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
    private String userID,wallet_type,wallet_address;
    private static final String ETH_ADDRESS = "0xa7F31Db454fE3c36c7Bb186d209fF7F433aE0314";
    private static final String ETH_PRV_KEY = "b189f059bddf6d87deb45e8c31fa93921f87af3d1849064aa1cad0fef35a3666";
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
                showLoading();
                sign();
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

    private void sign(){
        String wallet_type = "eth";
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
                loginRequest(userID,wallet_type,wallet_address,mainPrivateKeyHex,mainPublicKeyHex);
            }

            @Override
            public void onReject() {
                hideLoading();
                Toast.makeText(getActivity(),"sign reject",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendSign(String sign_raw){
        Web3MQSign.getInstance().sendSignRequest(sign_raw, wallet_address, false, new SendBridgeMessageCallback() {
            @Override
            public void onReceived() {

            }

            @Override
            public void onFail() {

            }

            @Override
            public void onTimeout() {

            }
        });
    }

    public void setUserInfo(String userid,String wallet_type,String wallet_address){
        this.userID = userid;
        this.wallet_type = wallet_type;
        this.wallet_address = wallet_address;
        Log.i(TAG,"setUserInfo userid:"+userid+" wallet_type:"+wallet_type+" wallet_address:"+wallet_address);
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
                Intent intent = new Intent(getActivity(), HomePageActivity.class);
                startActivity(intent);
            }
        });
    }
}