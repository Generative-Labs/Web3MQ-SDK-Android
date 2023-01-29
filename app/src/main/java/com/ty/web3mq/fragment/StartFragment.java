package com.ty.web3mq.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ty.web3_mq.Web3MQSign;
import com.ty.web3_mq.interfaces.BridgeConnectCallback;
import com.ty.web3mq.R;
import com.ty.web3mq.activity.LoginActivity;

public class StartFragment extends BaseFragment{
    private static final String TAG = "StartFragment";
    private static StartFragment instance;
    private TextView tv_connect_wallet,tv_check_out;
    public static synchronized StartFragment getInstance() {
        if (instance == null) {
            instance = new StartFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_start,false);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        initView();
        setListener();
    }


    private void initView(){
        tv_connect_wallet = rootView.findViewById(R.id.tv_connect_wallet);
        tv_check_out = rootView.findViewById(R.id.tv_check_out);
    }


    private void setListener() {
        tv_connect_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity loginActivity = (LoginActivity) getActivity();
                loginActivity.switchContent(ConnectWalletFragment.getInstance());
            }
        });
    }

}