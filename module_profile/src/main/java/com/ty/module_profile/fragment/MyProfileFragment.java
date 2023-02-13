package com.ty.module_profile.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.ty.common.config.Constants;
import com.ty.common.config.RouterPath;
import com.ty.common.fragment.BaseFragment;
import com.ty.common.utils.CommonUtils;
import com.ty.module_profile.R;
import com.ty.module_profile.view.FollowNumberTextView;
import com.ty.web3_mq.Web3MQUser;
import com.ty.web3_mq.http.beans.ProfileBean;
import com.ty.web3_mq.interfaces.GetMyProfileCallback;

public class MyProfileFragment extends BaseFragment {
    private ImageView iv_avatar;
    private TextView tv_edit;
    private ImageButton btn_setting,btn_copy;
    private TextView tv_wallet_address;
    private FollowNumberTextView tv_follow_number;
    private String wallet_address;
    private String nickname;
    private static volatile MyProfileFragment instance;
    public static synchronized MyProfileFragment getInstance() {
        if (instance == null) {
            instance = new MyProfileFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_my_profile);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        initView();
        setListener();
        requestData();
    }

    private void requestData() {
        Web3MQUser.getInstance().getMyProfile(new GetMyProfileCallback() {
            @Override
            public void onSuccess(ProfileBean profileBean) {
                String avatar_url = profileBean.avatar_url;
                if(!TextUtils.isEmpty(avatar_url)){
                    Glide.with(getContext()).load(avatar_url).into(iv_avatar);
                }
                nickname = profileBean.nickname;
                wallet_address = profileBean.wallet_address;
                tv_wallet_address.setText(wallet_address);
                tv_follow_number.setNumbers(profileBean.stats.total_followers,profileBean.stats.total_following);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"get profile error:"+error,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initView(){
        iv_avatar = rootView.findViewById(R.id.iv_avatar);
        tv_edit = rootView.findViewById(R.id.tv_edit);
        btn_setting = rootView.findViewById(R.id.btn_setting);
        btn_copy = rootView.findViewById(R.id.btn_copy);
        tv_wallet_address = rootView.findViewById(R.id.tv_wallet_address);
        tv_follow_number = rootView.findViewById(R.id.tv_follow_number);
    }

    private void setListener(){
        btn_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.copy(getActivity(),wallet_address);
                Toast.makeText(getActivity(),"copied",Toast.LENGTH_SHORT).show();
            }
        });
        tv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARouter.getInstance().build(RouterPath.MY_PROFILE_EDIT).navigation();
            }
        });
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARouter.getInstance().build(RouterPath.MY_PROFILE_SETTINGS).withString(Constants.ROUTER_KEY_NICKNAME,nickname).navigation();
            }
        });
    }
}
