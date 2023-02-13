package com.ty.sample.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.ty.common.Web3MQUI;
import com.ty.common.config.Constants;
import com.ty.module_chat.ModuleChat;
import com.ty.module_chat.fragment.ChatsFragment;
import com.ty.module_contact.fragment.ContactsFragment;
import com.ty.module_login.ModuleLogin;
import com.ty.module_notification.fragment.NotificationFragment;
import com.ty.module_profile.ModuleProfile;
import com.ty.module_profile.fragment.MyProfileFragment;
import com.ty.moudle_new_message.NewMessageFragment;
import com.ty.sample.R;
import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.Web3MQNotification;
import com.ty.web3_mq.http.beans.NotificationBean;
import com.ty.web3_mq.interfaces.NotificationMessageCallback;
import com.ty.web3_mq.interfaces.OnConnectCommandCallback;

import java.util.ArrayList;

public class HomePageActivity extends AppCompatActivity {
    private Fragment currentFragment;
    private BottomNavigationView bottom_navigation_view;
    private static final String TAG = "HomePageActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Web3MQUI.getInstance().setInitCallback(new Web3MQUI.InitCallback() {
            @Override
            public void onSuccess() {
                Log.e(TAG,"init Success");
                sendConnectCommand();
            }

            @Override
            public void onFail() {
                Log.e(TAG,"init fail");
                Toast.makeText(HomePageActivity.this,"init fail",Toast.LENGTH_SHORT).show();
            }
        });
        initView();
        setListener();
    }

    private void sendConnectCommand(){
        Web3MQClient.getInstance().sendConnectCommand(new OnConnectCommandCallback() {
            @Override
            public void onConnectCommandResponse() {
                Log.i(TAG,"onConnectCommandResponse Success");
            }
        });
    }

    private void initView(){
        bottom_navigation_view = findViewById(R.id.bottom_navigation_view);
        switchContent(ChatsFragment.getInstance());
    }

    private void setListener() {
        bottom_navigation_view.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_chats:
                        switchContent(ChatsFragment.getInstance());
                        break;
                    case R.id.navigation_contact:
                        switchContent(ContactsFragment.getInstance());
                        break;
                    case R.id.navigation_notifications:
                        switchContent(NotificationFragment.getInstance());
                        NotificationFragment.getInstance().listenToNotificationMessageEvent();
                        break;
                    case R.id.navigation_profile:
                        switchContent(MyProfileFragment.getInstance());
                        break;
                }
                return true;
            }
        });
        ContactsFragment.getInstance().setToNewMessageListener(new ContactsFragment.ToNewMessageListener() {
            @Override
            public void toNewMessageModule() {
                //new message
                NewMessageFragment.getInstance().show(getSupportFragmentManager(),"new message");
            }
        });

        ChatsFragment.getInstance().setToNewMessageListener(new ChatsFragment.ToNewMessageListener() {
            @Override
            public void toNewMessageModule() {
                NewMessageFragment.getInstance().show(getSupportFragmentManager(),"new message");
            }
        });

        NewMessageFragment.getInstance().setToMessageListener(new NewMessageFragment.ToMessageListener() {
            @Override
            public void toMessage(String chat_type, String chat_id) {
                ModuleChat.getInstance().toMessageUI(chat_type, chat_id);
            }
        });
        ModuleProfile.getInstance().setOnChatEvent(new ModuleProfile.OnChatEvent() {
            @Override
            public void onChat(String userid) {
                ModuleChat.getInstance().toMessageUI(Constants.CHAT_TYPE_USER, userid);
            }
        });
        ModuleProfile.getInstance().setOnLogoutEvent(new ModuleProfile.OnLogoutEvent() {
            @Override
            public void onLogout() {
                ModuleLogin.getInstance().launch();
            }
        });
        ModuleChat.getInstance().setToNewMessageRequestListener(new ModuleChat.ToNewMessageRequestListener() {
            @Override
            public void toRequestFollow(String userid) {
                NewMessageFragment newMessageFragment = NewMessageFragment.getInstance();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.ROUTER_KEY_USER_ID, userid);
                newMessageFragment.setArguments(bundle);
                newMessageFragment.show(getSupportFragmentManager(),"new message");
            }
        });
    }

    private void listenToNotificationMessageEvent(){
        Web3MQNotification.getInstance().setOnNotificationMessageEvent(new NotificationMessageCallback() {
            @Override
            public void onNotificationMessage(ArrayList<NotificationBean> response) {
                //TODO 通知小红点
//                notifications.addAll(0,response);
//                adapter.notifyDataSetChanged();
            }
        });
    }

    public void switchContent(Fragment to) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        if(currentFragment == null){
            transaction.add(R.id.fl_contacts_content,to).commitAllowingStateLoss();
            currentFragment = to;
            return;
        }
        if (currentFragment != to) {
            if (!to.isAdded()) { // 先判断是否被add过
                transaction.hide(currentFragment).add(R.id.fl_contacts_content, to).commitAllowingStateLoss(); // 隐藏当前的fragment，add下一个到Activity中
            } else {
                transaction.hide(currentFragment).show(to).commitAllowingStateLoss(); // 隐藏当前的fragment，显示下一个
            }
            currentFragment = to;
        }
    }
}
