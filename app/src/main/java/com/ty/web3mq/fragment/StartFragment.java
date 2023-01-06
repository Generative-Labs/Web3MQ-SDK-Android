package com.ty.web3mq.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ty.web3mq.R;
import com.ty.web3mq.activity.LoginActivity;

public class StartFragment extends BaseFragment{
    private static final String TAG = "StartFragment";
    private static StartFragment instance;
    private TextView tv_connect_wallet,tv_create_wallet,tv_check_out;

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
        tv_create_wallet = rootView.findViewById(R.id.tv_create_wallet);
        tv_check_out = rootView.findViewById(R.id.tv_check_out);
    }


    private void setListener() {
        tv_connect_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity loginActivity = (LoginActivity) getActivity();
                loginActivity.switchContent(LoginFragment.getInstance());
            }
        });

        tv_create_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity loginActivity = (LoginActivity) getActivity();
                loginActivity.switchContent(RegisterFragment.getInstance());
            }
        });
    }

}