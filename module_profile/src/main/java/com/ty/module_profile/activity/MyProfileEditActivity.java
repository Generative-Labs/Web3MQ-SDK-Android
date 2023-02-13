package com.ty.module_profile.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.ty.common.activity.BaseActivity;
import com.ty.common.config.RouterPath;
import com.ty.module_profile.ModuleProfile;
import com.ty.module_profile.R;
import com.ty.web3_mq.utils.DefaultSPHelper;

@Route(path = RouterPath.MY_PROFILE_EDIT)
public class MyProfileEditActivity extends BaseActivity {
    TextView tv_nickname;
    @Autowired
    String nickname;
    ImageView iv_back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_my_profile_edit);
        tv_nickname = findViewById(R.id.tv_nickname);
        iv_back = findViewById(R.id.iv_back);
        tv_nickname.setText(nickname);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
