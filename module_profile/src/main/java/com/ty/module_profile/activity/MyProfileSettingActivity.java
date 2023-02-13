package com.ty.module_profile.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.ty.common.activity.BaseActivity;
import com.ty.common.config.RouterPath;
import com.ty.module_profile.ModuleProfile;
import com.ty.module_profile.R;
import com.ty.web3_mq.utils.DefaultSPHelper;

@Route(path = RouterPath.MY_PROFILE_SETTINGS)
public class MyProfileSettingActivity extends BaseActivity {
    Button btn_logout;
    ImageView iv_back;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_my_profile_settings);
        btn_logout = findViewById(R.id.btn_logout);
        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DefaultSPHelper.getInstance().clear();
                if(ModuleProfile.getInstance().getOnLogoutEvent()!=null){
                    ModuleProfile.getInstance().getOnLogoutEvent().onLogout();
                }
            }
        });
    }
}
