package com.ty.module_notification.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ty.common.config.AppConfig;
import com.ty.common.fragment.BaseFragment;
import com.ty.common.view.Web3MQListView;
import com.ty.module_notification.R;
import com.ty.module_notification.adapter.NotificationAdapter;
import com.ty.web3_mq.Web3MQFollower;
import com.ty.web3_mq.Web3MQNotification;
import com.ty.web3_mq.websocket.bean.sign.Web3MQSign;
import com.ty.web3_mq.http.beans.NotificationBean;
import com.ty.web3_mq.http.beans.NotificationsBean;
import com.ty.web3_mq.interfaces.BridgeConnectCallback;
import com.ty.web3_mq.interfaces.FollowCallback;
import com.ty.web3_mq.interfaces.GetNotificationHistoryCallback;
import com.ty.web3_mq.interfaces.NotificationMessageCallback;
import com.ty.web3_mq.interfaces.OnConnectResponseCallback;
import com.ty.web3_mq.interfaces.OnSignResponseMessageCallback;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.websocket.bean.BridgeMessageProposer;
import com.ty.web3_mq.websocket.bean.BridgeMessageMetadata;

import java.util.ArrayList;

public class NotificationFragment extends BaseFragment {
    private static NotificationFragment instance;
    private NotificationAdapter adapter;
    private static final String TAG = "NotificationsFragment";
    private ArrayList<NotificationBean> notifications = new ArrayList<>();
    private Web3MQListView list_notification;
    public static synchronized NotificationFragment getInstance() {
        if (instance == null) {
            instance = new NotificationFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_notification);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        requestData();
        initView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            requestData();
        }
    }

    public void listenToNotificationMessageEvent(){
        Web3MQNotification.getInstance().setOnNotificationMessageEvent(new NotificationMessageCallback() {
            @Override
            public void onNotificationMessage(ArrayList<NotificationBean> response) {
                notifications.addAll(0,response);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void removeNotificationMessageEvent(){

    }

    private void requestData() {
        Web3MQNotification.getInstance().getNotificationHistory(1, 20, new GetNotificationHistoryCallback() {
            @Override
            public void onSuccess(NotificationsBean notificationsBean) {
                notifications.clear();
                notifications = notificationsBean.result;
                if(notifications.size()>0){
                    list_notification.hideEmptyView();
                    updateList();
                }else{
                    list_notification.showEmptyView();
                }
                list_notification.setRefreshing(false);
            }

            @Override
            public void onFail(String error) {
                Log.e(TAG,"onFail:"+error);
                list_notification.setRefreshing(false);
            }
        });
    }

    private void initView() {
        list_notification = rootView.findViewById(R.id.list_notification);
        list_notification.setEmptyMessage("No notification message");
        list_notification.setEmptyIcon(R.mipmap.ic_empty_notification);
        list_notification.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();
            }
        });
    }

    private void updateList(){
        adapter = new NotificationAdapter(getActivity(),notifications);
        adapter.setOnFollowClickListener(new NotificationAdapter.OnFollowClickListener() {
            @Override
            public void onItemClick(int position) {
                NotificationBean notificationBean = notifications.get(position);
                String action = NotificationBean.ACTION_FOLLOW;
                toFollow(action,notificationBean.from);
            }
        });
        list_notification.setAdapter(adapter);
    }

    private void toFollow(String action,String target_user_id){
        if(Web3MQSign.getInstance().initialized()){
            String deepLink = Web3MQSign.getInstance().generateConnectDeepLink(null,AppConfig.WebSite,AppConfig.REDIRECT_HOME_PAGE);
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
                Toast.makeText(getActivity(),"connect reject",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(),"follow success",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFail(String error) {
                        Toast.makeText(getActivity(),"follow fail: "+error,Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onReject() {
                Toast.makeText(getActivity(),"sign reject",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
