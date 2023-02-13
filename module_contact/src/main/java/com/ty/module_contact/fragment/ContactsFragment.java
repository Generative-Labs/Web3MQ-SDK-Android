package com.ty.module_contact.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;
import com.ty.common.fragment.BaseFragment;
import com.ty.module_contact.R;

public class ContactsFragment extends BaseFragment {
    private TabLayout tabLayout;
    private static final String TAG = "ContactsFragment";
    private static volatile ContactsFragment instance;
    private Fragment currentFragment;
    private static final int FRAGMENT_ID_FOLLOWER = 0;
    private static final int FRAGMENT_ID_FOLLOWING = 1;
    private ImageView iv_new_message;
    private ToNewMessageListener toNewMessageListener;
    public static synchronized ContactsFragment getInstance() {
        if (instance == null) {
            instance = new ContactsFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_contacts);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        initView();
        setListener();
    }

    private void initView() {
        tabLayout = rootView.findViewById(R.id.tab_layout);
        iv_new_message = rootView.findViewById(R.id.iv_new_message);
        tabLayout.addTab(tabLayout.newTab().setText("Followers").setId(FRAGMENT_ID_FOLLOWER));
        tabLayout.addTab(tabLayout.newTab().setText("Following").setId(FRAGMENT_ID_FOLLOWING));
        switchContent(FollowersFragment.getInstance());
    }

    private void setListener(){
        iv_new_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // new message
                if(toNewMessageListener !=null){
                    toNewMessageListener.toNewMessageModule();
                }
            }
        });
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
                handleTab(tab);
            }
        });
    }


    private void handleTab(TabLayout.Tab tab){
        switch (tab.getId()){
            case FRAGMENT_ID_FOLLOWER:
                switchContent(FollowersFragment.getInstance());
                break;
            case FRAGMENT_ID_FOLLOWING:
                switchContent(FollowingFragment.getInstance());
                break;
        }
    }

    public void setToNewMessageListener(ToNewMessageListener toNewMessageListener){
        this.toNewMessageListener = toNewMessageListener;
    }

    public void switchContent(Fragment to) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
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

    public interface ToNewMessageListener {
        void toNewMessageModule();
    }
}
