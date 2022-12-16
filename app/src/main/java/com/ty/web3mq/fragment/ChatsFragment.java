package com.ty.web3mq.fragment;

import android.os.Bundle;


import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;
import com.ty.web3mq.R;

public class ChatsFragment extends BaseFragmentNoSwipeRefresh {
    private static final String TAG = "ChatsFragment";
    private static ChatsFragment instance;
    private Fragment currentFragment;
    private String[] titles = new String[]{"DM","Topic","Group"};
    private TabLayout tabLayout;
    private static final int TAB_ID_DM = 0;
    private static final int TAB_ID_TOPIC = 1;
    private static final int TAB_ID_GROUP = 2;
    public static synchronized ChatsFragment getInstance() {
        if (instance == null) {
            instance = new ChatsFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_chats);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        initView();
        switchContent(DMFragment.getInstance());
    }

    private void initView(){
        tabLayout = rootView.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(titles[0]).setId(TAB_ID_DM));
        tabLayout.addTab(tabLayout.newTab().setText(titles[1]).setId(TAB_ID_TOPIC));
        tabLayout.addTab(tabLayout.newTab().setText(titles[2]).setId(TAB_ID_GROUP));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                handleTab(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void handleTab(TabLayout.Tab tab){
        switch (tab.getId()){
            case TAB_ID_DM:
                switchContent(DMFragment.getInstance());
                break;
            case TAB_ID_TOPIC:
                switchContent(TopicFragment.getInstance());
                break;
            case TAB_ID_GROUP:
                switchContent(GroupFragment.getInstance());
                break;
        }
    }

    public void switchContent(Fragment to) {
        FragmentTransaction transaction = getChildFragmentManager()
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
