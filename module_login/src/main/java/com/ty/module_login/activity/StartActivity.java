package com.ty.module_login.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.ty.common.activity.BaseActivity;
import com.ty.common.config.RouterPath;
import com.ty.module_login.ModuleLogin;
import com.ty.module_login.R;
import com.ty.module_login.config.UIConfigStart;
import com.ty.web3_mq.Web3MQUser;

@Route(path = RouterPath.LOGIN_START)
public class StartActivity extends BaseActivity {
    private TextView tv_connect_wallet,tv_check_out;
    private ImageView iv_logo;
    @Autowired
    UIConfigStart uiConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_start);
        if(Web3MQUser.getInstance().hasLogged()){
            ModuleLogin.getInstance().getOnLoginSuccessCallback().onLoginSuccess();
        }else {
            initView();
            setListener();
        }
    }



    private void initView(){
        tv_connect_wallet = findViewById(R.id.tv_connect_wallet);
        tv_check_out = findViewById(R.id.tv_check_out);
        iv_logo = findViewById(R.id.iv_logo);
        if(uiConfig.logoResID!=0){
            iv_logo.setImageResource(uiConfig.logoResID);
        }
    }


    private void setListener() {
        tv_connect_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARouter.getInstance().build(RouterPath.LOGIN_CONNECT_WALLET).navigation();
            }
        });
    }
}
