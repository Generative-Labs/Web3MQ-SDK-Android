package com.ty.web3mq.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.Web3MQUser;
import com.ty.web3_mq.interfaces.ConnectCallback;
import com.ty.web3_mq.interfaces.SignupCallback;
import com.ty.web3mq.R;

/**
 *  pub_hex
 *  302A300506032B65700321004A49C07BF63FF45624FF8068FD4910945530EFD5F6B7D68172ED134D0DEC13B5
 *  prv_hex
 *  302E020100300506032B6570042204208839A83E406D5B68F78FBD827BD2BD82C7016B29596E278FC567D065E3603DAA
 */

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private Button btn_login,btn_sign;
    private EditText et_eth_address, et_eth_prv_key;
    private Web3MQUser web3MQUser = Web3MQUser.getInstance();
    private SignupCallback signupCallback;
    private static final String ETH_ADDRESS = "0x1E6c3eed532d4a3937FBE178909739E60A327e19";
    private static final String ETH_PRV_KEY = "74cceffe2a4970e40b0bd55c623d724c72b79c25daa60c511f1a5ef17697faf1";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        setListener();
    }

    private void initView() {
        btn_login = findViewById(R.id.btn_login);
        btn_sign = findViewById(R.id.btn_sign);
        et_eth_address = findViewById(R.id.et_eth_address);
        et_eth_prv_key = findViewById(R.id.et_eth_prv_key);
        et_eth_address.setText(ETH_ADDRESS);
        et_eth_prv_key.setText(ETH_PRV_KEY);
        btn_login.setEnabled(web3MQUser.existLocalAccount());
    }

    private void setListener() {
        Web3MQClient.getInstance().setConnectCallback(new ConnectCallback() {
            @Override
            public void onSuccess() {
                // connect success
                Intent intent = new Intent(LoginActivity.this,HomePageActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(LoginActivity.this,"connect fail", Toast.LENGTH_SHORT).show();
            }
        });

        signupCallback = new SignupCallback() {
            @Override
            public void onSuccess() {
                Web3MQClient.getInstance().startConnect();
            }

            @Override
            public void onFail(String error) {
                Log.i(TAG,"signUp fail: "+error);
            }
        };

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Web3MQClient.getInstance().startConnect();
            }
        });
        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String eth_prv_key = et_eth_prv_key.getText().toString();
                String eth_pub_key = et_eth_address.getText().toString();
                web3MQUser.signUp(eth_prv_key, eth_pub_key, signupCallback);
            }
        });
    }




}