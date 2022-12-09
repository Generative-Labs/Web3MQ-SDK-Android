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
import com.ty.web3_mq.interfaces.SignupCallback;
import com.ty.web3mq.R;
import com.ty.web3mq.activity.HomePageActivity;


public class LoginFragment extends BaseFragment {
    private static LoginFragment instance;
    private static final String TAG = "LoginFragment";
    private Button btn_login,btn_sign;
    private EditText et_eth_address, et_eth_prv_key;
    private Web3MQUser web3MQUser = Web3MQUser.getInstance();
    private SignupCallback signupCallback;
    private static final String ETH_ADDRESS = "0x1E6c3eed532d4a3937FBE178909739E60A327e19";
    private static final String ETH_PRV_KEY = "74cceffe2a4970e40b0bd55c623d724c72b79c25daa60c511f1a5ef17697faf1";
    public static synchronized LoginFragment getInstance() {
        if (instance == null) {
            instance = new LoginFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_login);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        initView();
        setListener();
    }

    private void initView() {
        btn_login = rootView.findViewById(R.id.btn_login);
        btn_sign = rootView.findViewById(R.id.btn_sign);
        et_eth_address = rootView.findViewById(R.id.et_eth_address);
        et_eth_prv_key = rootView.findViewById(R.id.et_eth_prv_key);
        et_eth_address.setText(ETH_ADDRESS);
        et_eth_prv_key.setText(ETH_PRV_KEY);
        btn_login.setEnabled(web3MQUser.isLocalAccountExist());
    }

    private void setListener() {
        Web3MQClient.getInstance().setConnectCallback(new ConnectCallback() {
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

        signupCallback = new SignupCallback() {
            @Override
            public void onSuccess() {
                Web3MQClient.getInstance().startConnect();
            }

            @Override
            public void onFail(String error) {
                hideLoadingDialog();
                Log.i(TAG,"signUp fail: "+error);
            }
        };

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingDialog();
                Web3MQClient.getInstance().startConnect();
            }
        });
        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoadingDialog();
                String eth_prv_key = et_eth_prv_key.getText().toString();
                String eth_pub_key = et_eth_address.getText().toString();
                web3MQUser.signUp(eth_prv_key, eth_pub_key, signupCallback);
            }
        });
    }
}