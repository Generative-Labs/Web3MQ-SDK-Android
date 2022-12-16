package com.ty.web3mq.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.ty.web3mq.R;
import com.ty.web3mq.fragment.MessageDMFragment;
import com.ty.web3mq.utils.Constants;

public class DMChatActivity extends AppCompatActivity {
    private MessageDMFragment messageDmFragment;
    private String chat_id,chat_type;
    private static final String TAG = "DMChatActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dm_chat);
        chat_id = getIntent().getStringExtra(Constants.INTENT_CHAT_ID);
        chat_type = getIntent().getStringExtra(Constants.INTENT_CHAT_TYPE);
        if(chat_id !=null && chat_type!=null){
            messageDmFragment = MessageDMFragment.getInstance(chat_id,chat_type);
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction();
            transaction.add(R.id.fl_content, messageDmFragment).commitAllowingStateLoss();
        }else{
            Log.e(TAG,"no chat_id or chat_type");
            finish();
        }

    }

}
