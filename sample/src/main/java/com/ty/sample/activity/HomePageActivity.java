package com.ty.sample.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.ty.module_chat.fragment.ChatsFragment;
import com.ty.sample.R;

public class HomePageActivity extends AppCompatActivity {
    private Fragment currentFragment;
    private BottomNavigationView bottom_navigation_view;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        initView();
        setListener();
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
//                        switchContent(ContactsFragment.getInstance());
                        break;
                    case R.id.navigation_notifications:
//                        switchContent(NotificationsFragment.getInstance());
                        break;
                    case R.id.navigation_profile:
//                        switchContent(ProfileFragment.getInstance());
                        break;
                }
                return true;
            }
        });
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
