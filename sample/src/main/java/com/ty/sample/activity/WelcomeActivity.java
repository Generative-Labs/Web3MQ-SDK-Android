package com.ty.sample.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ty.module_login.ModuleLogin;
import com.ty.module_login.interfaces.LoginSuccessCallback;
import com.ty.sample.R;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ModuleLogin moduleLogin = ModuleLogin.getInstance();
        moduleLogin.setOnLoginSuccessCallback(new LoginSuccessCallback() {
            @Override
            public void onLoginSuccess() {
                //TODO 跳转到主界面
                Intent intent = new Intent(WelcomeActivity.this, HomePageActivity.class);
                startActivity(intent);
            }
        });
        moduleLogin.launch();
    }
}
