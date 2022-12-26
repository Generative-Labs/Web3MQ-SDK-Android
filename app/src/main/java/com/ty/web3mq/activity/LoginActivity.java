package com.ty.web3mq.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

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
import com.ty.web3mq.fragment.LoginFragment;
import com.ty.web3mq.fragment.NewFriendFragment;


public class LoginActivity extends AppCompatActivity {
    private LoginFragment loginFragment;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Web3MQClient.getInstance().init(this,"rkkJARiziBQCscgg");
        setContentView(R.layout.activity_login);
        loginFragment = LoginFragment.getInstance();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.add(R.id.fl_content,loginFragment).commitAllowingStateLoss();
    }
}