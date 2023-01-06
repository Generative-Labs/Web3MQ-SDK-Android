package com.ty.web3mq.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.ty.web3_mq.Web3MQClient;
import com.ty.web3mq.R;
import com.ty.web3mq.fragment.StartFragment;


public class LoginActivity extends AppCompatActivity {
    private StartFragment startFragment;
    private Fragment currentFragment;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Web3MQClient.getInstance().init(this,"rkkJARiziBQCscgg");
        setContentView(R.layout.activity_login);
        startFragment = StartFragment.getInstance();
        switchContent(startFragment);
    }

    public void switchContent(Fragment to) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        if(currentFragment == null){
            transaction.add(R.id.fl_content,to).commitAllowingStateLoss();
            currentFragment = to;
            return;
        }
        if (currentFragment != to) {
            if (!to.isAdded()) { // 先判断是否被add过
                transaction.hide(currentFragment).add(R.id.fl_content, to).commitAllowingStateLoss(); // 隐藏当前的fragment，add下一个到Activity中
            } else {
                transaction.hide(currentFragment).show(to).commitAllowingStateLoss(); // 隐藏当前的fragment，显示下一个
            }
            currentFragment = to;
        }
    }
}