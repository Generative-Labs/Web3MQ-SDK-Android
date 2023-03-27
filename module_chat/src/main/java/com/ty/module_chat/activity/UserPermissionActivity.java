package com.ty.module_chat.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.ty.common.activity.BaseActivity;
import com.ty.common.config.AppConfig;
import com.ty.common.config.Constants;
import com.ty.common.config.RouterPath;
import com.ty.module_chat.ModuleChat;
import com.ty.module_chat.R;
import com.ty.web3_mq.Web3MQFollower;
import com.ty.web3_mq.Web3MQPermission;
import com.ty.web3_mq.websocket.bean.sign.Web3MQSign;
import com.ty.web3_mq.http.beans.NotificationBean;
import com.ty.web3_mq.http.beans.UserPermissionsBean;
import com.ty.web3_mq.interfaces.BridgeConnectCallback;
import com.ty.web3_mq.interfaces.FollowCallback;
import com.ty.web3_mq.interfaces.GetUserPermissionCallback;
import com.ty.web3_mq.interfaces.OnConnectResponseCallback;
import com.ty.web3_mq.interfaces.OnSignResponseMessageCallback;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.websocket.bean.BridgeMessageProposer;
import com.ty.web3_mq.websocket.bean.BridgeMessageMetadata;

@Route(path = RouterPath.CHAT_USER_PERMISSION)
public class UserPermissionActivity extends BaseActivity {
    @Autowired
    String chat_id;
    @Autowired
    String chat_type;

    String chat_user_permission;
    String follow_status;
    Button btn_action,btn_cancel,btn_follow;
    TextView tv_warn;
    private static final String STATE_NO_NEED = "no_need";
    private static final String STATE_NEED_FOLLOW = "need_follow";
    private static final String STATE_NEED_REQUEST = "need_request";
    private static final String STATE_NEED_FOLLOW_AND_REQUEST = "need_follow_and_request";
    private String state;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(chat_type.equals(Constants.CHAT_TYPE_GROUP)){
            ARouter.getInstance().build(RouterPath.CHAT_MESSAGE).withString(Constants.ROUTER_KEY_CHAT_TYPE,chat_type).withString(Constants.ROUTER_KEY_CHAT_ID,chat_id).navigation();
        }else if(chat_type.equals(Constants.CHAT_TYPE_USER)){
            setContent(R.layout.activity_chat_user_permission);
            initView();
            checkPermission();
        }
    }

    private void initView() {
        btn_action = findViewById(R.id.btn_action);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_follow = findViewById(R.id.btn_follow);
        tv_warn = findViewById(R.id.tv_warn);
    }


    private void checkPermission(){
        Web3MQPermission.getInstance().getUserPermission(chat_id, new GetUserPermissionCallback() {
            @Override
            public void onSuccess(UserPermissionsBean userPermissionsBean) {
                chat_user_permission = userPermissionsBean.chat_permission;
                follow_status = userPermissionsBean.follow_status;
                handlePermission();
                handleState();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(UserPermissionActivity.this,"get permission error : "+error,Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void handleState() {
        switch (state){
            case STATE_NO_NEED:
                ARouter.getInstance().build(RouterPath.CHAT_MESSAGE).withString(Constants.ROUTER_KEY_CHAT_TYPE,chat_type).withString(Constants.ROUTER_KEY_CHAT_ID,chat_id).navigation();
                break;
            case STATE_NEED_FOLLOW:
                tv_warn.setText("The other party has set the privacy permission you need to follow each other");
                btn_follow.setVisibility(View.GONE);
                btn_action.setText("Follow");
                btn_action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO follow
                        toFollow(NotificationBean.ACTION_FOLLOW,chat_id);
                    }
                });
                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

                break;
            case STATE_NEED_REQUEST:
                tv_warn.setText("The other party set the privacy permission need to ask the other party to follow you");
                btn_follow.setVisibility(View.GONE);
                btn_action.setText("Request");
                btn_action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO request
                        if(ModuleChat.getInstance().getToNewMessageRequestListener()!=null){
                            ModuleChat.getInstance().getToNewMessageRequestListener().toRequestFollow(chat_id);
                        }
                    }
                });
                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                break;
            case STATE_NEED_FOLLOW_AND_REQUEST:
                tv_warn.setText("The other party has set privacy rights, you need to follow and send a request message");
                btn_follow.setVisibility(View.VISIBLE);
                btn_follow.setText("Follow");
                btn_follow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO follow
                        toFollow(NotificationBean.ACTION_FOLLOW,chat_id);
                    }
                });
                btn_action.setText("Request");
                btn_action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO request
                        if(ModuleChat.getInstance().getToNewMessageRequestListener()!=null){
                            ModuleChat.getInstance().getToNewMessageRequestListener().toRequestFollow(chat_id);
                        }

                    }
                });
                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

                break;
        }
    }



    private void handlePermission() {
        switch (chat_user_permission){
            case Constants.CHAT_USER_PERMISSION_PUBLIC:
                state = STATE_NO_NEED;
                break;
            case Constants.CHAT_USER_PERMISSION_FOLLOWER:
                if(follow_status.equals(Constants.FOLLOW_STATUS_FOLLOWER) || follow_status.equals(Constants.FOLLOW_STATUS_EACH)){
                    state = STATE_NO_NEED;
                }else{
                    state = STATE_NEED_FOLLOW;
                }
                break;
            case Constants.CHAT_USER_PERMISSION_FOLLOWING:
                if(follow_status.equals(Constants.FOLLOW_STATUS_FOLLOWING) || follow_status.equals(Constants.FOLLOW_STATUS_EACH)){
                    state = STATE_NO_NEED;
                }else{
                    state = STATE_NEED_REQUEST;
                }
                break;
            case Constants.CHAT_USER_PERMISSION_FRIEND:
                if(follow_status.equals(Constants.FOLLOW_STATUS_EACH)){
                    state = STATE_NO_NEED;
                }else if(follow_status.equals(Constants.FOLLOW_STATUS_FOLLOWER)){
                    state = STATE_NEED_REQUEST;
                }else if(follow_status.equals(Constants.FOLLOW_STATUS_FOLLOWING)){
                    state = STATE_NEED_FOLLOW;
                }else {
                    state = STATE_NEED_FOLLOW_AND_REQUEST;
                }
                break;
        }
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
            public void onApprove(BridgeMessageMetadata walletInfo, String address) {
                toSign(action,walletInfo.walletType,address,target_user_id);
            }

            @Override
            public void onReject() {
                Toast.makeText(UserPermissionActivity.this,"connect reject",Toast.LENGTH_SHORT).show();
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
        Web3MQSign.getInstance().sendSignRequest(sign_raw,wallet_address,false,null);
        Web3MQSign.getInstance().setOnSignResponseMessageCallback(new OnSignResponseMessageCallback() {
            @Override
            public void onApprove(String signature) {
                Web3MQFollower.getInstance().follow(target_user_id, action, signature, sign_raw, timestamp, new FollowCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(UserPermissionActivity.this,"follow success",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFail(String error) {
                        Toast.makeText(UserPermissionActivity.this,"follow fail: "+error,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onReject() {
                Toast.makeText(UserPermissionActivity.this,"sign reject",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
