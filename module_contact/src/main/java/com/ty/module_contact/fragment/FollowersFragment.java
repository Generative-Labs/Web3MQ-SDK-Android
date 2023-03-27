package com.ty.module_contact.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ty.common.config.AppConfig;
import com.ty.common.fragment.BaseFragment;
import com.ty.common.view.Web3MQListView;
import com.ty.module_contact.R;
import com.ty.module_contact.adapter.FollowersAdapter;
import com.ty.module_contact.bean.FollowItem;
import com.ty.web3_mq.utils.ConvertUtil;
import com.ty.web3_mq.Web3MQFollower;
import com.ty.web3_mq.websocket.bean.sign.Web3MQSign;
import com.ty.web3_mq.http.beans.FollowerBean;
import com.ty.web3_mq.http.beans.FollowersBean;
import com.ty.web3_mq.interfaces.BridgeConnectCallback;
import com.ty.web3_mq.interfaces.FollowCallback;
import com.ty.web3_mq.interfaces.GetMyFollowersCallback;
import com.ty.web3_mq.interfaces.OnConnectResponseCallback;
import com.ty.web3_mq.interfaces.OnSignResponseMessageCallback;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.websocket.bean.BridgeMessageProposer;
import com.ty.web3_mq.websocket.bean.BridgeMessageMetadata;

import java.util.ArrayList;

public class FollowersFragment extends BaseFragment {
    private static FollowersFragment instance;
    private Web3MQListView list_followers;
    private ArrayList<FollowItem> items = new ArrayList<>();
    private FollowersAdapter adapter;
    public static synchronized FollowersFragment getInstance() {
        if (instance == null) {
            instance = new FollowersFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_followers);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        requestData();
        initView();
        setListener();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            requestData();
        }
    }

    private void requestData() {
        Web3MQFollower.getInstance().getMyFollowers(1, 20, new GetMyFollowersCallback() {
            @Override
            public void onSuccess(String str) {
                FollowersBean followersBean = ConvertUtil.convertJsonToFollowersBean(str);
                ArrayList<FollowerBean> followerBeans =  followersBean.user_list;
                items.clear();
                if(followersBean.total_count>0){
                    list_followers.hideEmptyView();
                    for(FollowerBean followerBean: followerBeans){
                        FollowItem followItem = new FollowItem();
                        followItem.userName = followerBean.userid;
                        followItem.follow_status = followerBean.follow_status;
                        followItem.avatar_url = followerBean.avatar_url;
                        followItem.userid = followerBean.userid;
                        items.add(followItem);
                    }
                    updateListView();
                }else{
                    list_followers.showEmptyView();
                }
            }

            @Override
            public void onFail(String error) {
                list_followers.showEmptyView();
            }
        });
    }

    private void initView(){
        list_followers = rootView.findViewById(R.id.list_followers);
        list_followers.setEmptyIcon(R.mipmap.ic_empty_contact);
        list_followers.setEmptyMessage("Your contact list is empty");
    }

    private void setListener() {
        list_followers.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();
            }
        });
    }

    private void updateListView(){
        if(adapter == null){
            adapter = new FollowersAdapter(items,getActivity());
            adapter.setOnFollowClickListener(new FollowersAdapter.OnFollowClickListener() {
                @Override
                public void onItemClick(int position) {
                    FollowItem item = items.get(position);
                    toFollow(Web3MQFollower.ACTION_FOLLOW,item.userid);
                }
            });
            list_followers.setAdapter(adapter);
        }else{
            adapter.notifyDataSetChanged();
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
                Toast.makeText(getActivity(),"connect reject",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toSign(String action,String wallet_type, String wallet_address,String target_user_id) {
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
        String sign_raw = Web3MQFollower.getInstance().getFollowSignContent(wallet_type, wallet_address, nonce);
        Web3MQSign.getInstance().sendSignRequest(sign_raw, wallet_address, false,null);
        Web3MQSign.getInstance().setOnSignResponseMessageCallback(new OnSignResponseMessageCallback() {
            @Override
            public void onApprove(String signature) {
                Web3MQFollower.getInstance().follow(target_user_id, action, signature, sign_raw, timestamp, new FollowCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getActivity(), "unFollow success", Toast.LENGTH_SHORT).show();
                        requestData();
                    }

                    @Override
                    public void onFail(String error) {
                        Toast.makeText(getActivity(), "follow fail: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onReject() {
                Toast.makeText(getActivity(), "sign reject", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
