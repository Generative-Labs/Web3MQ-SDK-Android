package com.ty.module_profile.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.bumptech.glide.Glide;
import com.ty.common.activity.BaseActivity;
import com.ty.common.config.AppConfig;
import com.ty.common.config.Constants;
import com.ty.common.config.RouterPath;
import com.ty.module_profile.ModuleProfile;
import com.ty.module_profile.R;
import com.ty.module_profile.view.FollowNumberTextView;
import com.ty.web3_mq.Web3MQFollower;
import com.ty.web3_mq.Web3MQSign;
import com.ty.web3_mq.Web3MQUser;
import com.ty.web3_mq.http.beans.NotificationBean;
import com.ty.web3_mq.http.beans.ProfileBean;
import com.ty.web3_mq.interfaces.BridgeConnectCallback;
import com.ty.web3_mq.interfaces.FollowCallback;
import com.ty.web3_mq.interfaces.GetPublicProfileCallback;
import com.ty.web3_mq.interfaces.OnConnectResponseCallback;
import com.ty.web3_mq.interfaces.OnSignResponseMessageCallback;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.websocket.bean.BridgeMessageProposer;
import com.ty.web3_mq.websocket.bean.BridgeMessageWalletInfo;

@Route(path = RouterPath.OTHER_PROFILE)
public class OtherProfileActivity extends BaseActivity {
    @Autowired
    String userid;
    ImageView iv_back,iv_avatar;
    TextView tv_wallet_address;
    FollowNumberTextView tv_follow_number;
    String wallet_address, nickname;
    ImageButton btn_talk;
    TextView tv_follow;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_other_profile);
        initView();
        setListener();
        requestData();
    }

    private void initView() {
        iv_back = findViewById(R.id.iv_back);
        iv_avatar = findViewById(R.id.iv_avatar);
        tv_follow_number = findViewById(R.id.tv_follow_number);
        btn_talk = findViewById(R.id.btn_talk);
        tv_follow = findViewById(R.id.tv_follow);
    }

    private void setListener() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toFollow(NotificationBean.ACTION_FOLLOW,userid);
            }
        });
        btn_talk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ModuleProfile.getInstance().getOnChatEvent()!=null){
                    ModuleProfile.getInstance().getOnChatEvent().onChat(userid);
                }
            }
        });
    }

    private void requestData(){
        Web3MQUser.getInstance().getPublicProfile(userid, new GetPublicProfileCallback() {
            @Override
            public void onSuccess(ProfileBean profileBean) {
                String avatar_url = profileBean.avatar_url;
                if(!TextUtils.isEmpty(avatar_url)){
                    Glide.with(OtherProfileActivity.this).load(avatar_url).into(iv_avatar);
                }
                nickname = profileBean.nickname;
                wallet_address = profileBean.wallet_address;
                tv_wallet_address.setText(wallet_address);
                tv_follow_number.setNumbers(profileBean.stats.total_followers,profileBean.stats.total_following);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(OtherProfileActivity.this,"get profile error:"+error,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toFollow(String action,String target_user_id){
        if(Web3MQSign.getInstance().initialized()){
            String deepLink = Web3MQSign.getInstance().generateConnectDeepLink(null, AppConfig.WebSite,AppConfig.REDIRECT_HOME_PAGE);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
            startActivity(intent);
        }else{
            Web3MQSign.getInstance().init(AppConfig.DAppID, new BridgeConnectCallback(){
                @Override
                public void onConnectCallback() {
                    String deepLink = Web3MQSign.getInstance().generateConnectDeepLink(null,AppConfig.WebSite,AppConfig.REDIRECT_HOME_PAGE);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
                    startActivity(intent);
                }
            });
        }
        Web3MQSign.getInstance().setOnConnectResponseCallback(new OnConnectResponseCallback() {
            @Override
            public void onApprove(BridgeMessageWalletInfo walletInfo) {
                toSign(action,walletInfo.walletType,walletInfo.address,target_user_id);
            }

            @Override
            public void onReject() {
                Toast.makeText(OtherProfileActivity.this,"connect reject",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toSign(String action,String wallet_type, String wallet_address,String target_user_id){
        String deepLink = Web3MQSign.getInstance().generateSignDeepLink();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
        startActivity(intent);
        BridgeMessageProposer proposer = new BridgeMessageProposer();
        proposer.name = "Web3MQ_DAPP_DEMO";
        proposer.url = AppConfig.WebSite;
        proposer.redirect = AppConfig.REDIRECT_HOME_PAGE;
        long timestamp = System.currentTimeMillis();
        String userid = DefaultSPHelper.getInstance().getUserID();
        String nonce = CryptoUtils.SHA3_ENCODE(userid + action + target_user_id + timestamp);
        String sign_raw = Web3MQFollower.getInstance().getFollowSignContent(wallet_type,wallet_address,nonce);
        Web3MQSign.getInstance().sendSignRequest(proposer,sign_raw,wallet_address,timestamp+"","",false);
        Web3MQSign.getInstance().setOnSignResponseMessageCallback(new OnSignResponseMessageCallback() {
            @Override
            public void onApprove(String signature) {
                Web3MQFollower.getInstance().follow(target_user_id, action, signature, sign_raw, timestamp, new FollowCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(OtherProfileActivity.this,"follow success",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFail(String error) {
                        Toast.makeText(OtherProfileActivity.this,"follow fail: "+error,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onReject() {
                Toast.makeText(OtherProfileActivity.this,"sign reject",Toast.LENGTH_SHORT).show();
            }
        });

    }
}

