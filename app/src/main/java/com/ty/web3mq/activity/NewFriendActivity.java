package com.ty.web3mq.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.ty.web3mq.R;
import com.ty.web3mq.fragment.NewFriendFragment;

public class NewFriendActivity extends AppCompatActivity {
    private NewFriendFragment newFriendFragment;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);
        newFriendFragment = NewFriendFragment.getInstance();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.add(R.id.fl_content,newFriendFragment).commitAllowingStateLoss();
    }
}
