package com.ty.web3_mq;

import com.ty.web3_mq.http.ApiConfig;
import com.ty.web3_mq.http.HttpManager;
import com.ty.web3_mq.http.request.AddFriendsRequest;
import com.ty.web3_mq.http.request.FollowRequest;
import com.ty.web3_mq.http.request.GetFollowerRequest;
import com.ty.web3_mq.http.response.BaseResponse;
import com.ty.web3_mq.http.response.FollowersResponse;
import com.ty.web3_mq.interfaces.FollowCallback;
import com.ty.web3_mq.interfaces.GetMyFollowersCallback;
import com.ty.web3_mq.interfaces.GetMyFollowingCallback;
import com.ty.web3_mq.interfaces.SendFriendRequestCallback;
import com.ty.web3_mq.utils.CommonUtils;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.utils.Ed25519;

import java.net.URLEncoder;

public class Web3MQFollower {
    public static final String ACTION_FOLLOW = "follow";
    public static final String ACTION_CANCEL = "cancel";
    private volatile static Web3MQFollower web3MQFollower;
    private Web3MQFollower() {
    }
    public static Web3MQFollower getInstance() {
        if (null == web3MQFollower) {
            synchronized (Web3MQFollower.class) {
                if (null == web3MQFollower) {
                    web3MQFollower = new Web3MQFollower();
                }
            }
        }
        return web3MQFollower;
    }

    public String getFollowSignContent(String wallet_type,String wallet_address, String nonce){
        String str_date = CommonUtils.getDate();
        return "Web3MQ wants you to sign in with your "+wallet_type+" account:\n" +
                wallet_address+"\n" +
                "\n" +
                "For follow signature\n" +
                "\n" +
                "Nonce: "+nonce+"\n" +
                "Issued At: "+str_date;
    }

    public void follow(String target_userid, String action, String did_signature, String sign_content, long timeStamp, FollowCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            FollowRequest request = new FollowRequest();
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.target_userid = target_userid;
            request.timestamp = timeStamp;
            request.action = action;
            request.did_type = did_key.split(":")[0];
            request.did_signature = did_signature;
            request.did_pubkey = did_key.split(":")[1];
            request.sign_content = sign_content;
            HttpManager.getInstance().post(ApiConfig.POST_FOLLOW, request,pub_key,did_key, BaseResponse.class, new HttpManager.Callback<BaseResponse>() {
                @Override
                public void onResponse(BaseResponse response) {
                    if(response.getCode()==0){
                        callback.onSuccess();
                    }else{
                        callback.onFail("error code: "+response.getCode()+" msg:"+ response.getMsg());
                    }
                }

                @Override
                public void onError(String error) {
                    callback.onFail("error: "+error);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFail("ed25519 sign error");
        }
    }

    public void getMyFollowers(int page, int size, GetMyFollowersCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            GetFollowerRequest request = new GetFollowerRequest();
            request.page = page;
            request.size = size;
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.timestamp = System.currentTimeMillis();
            request.web3mq_user_signature = URLEncoder.encode(Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes()));
            HttpManager.getInstance().get(ApiConfig.GET_MY_FOLLOWERS, request,pub_key,did_key, new HttpManager.Callback<String>() {
                @Override
                public void onResponse(String response) {
                    callback.onSuccess(response);
                }

                @Override
                public void onError(String error) {
                    callback.onFail("error: "+error);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFail("ed25519 sign error");
        }
    }

    public void getMyFollowing(int page, int size, GetMyFollowingCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            GetFollowerRequest request = new GetFollowerRequest();
            request.page = page;
            request.size = size;
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.timestamp = System.currentTimeMillis();
            request.web3mq_user_signature = URLEncoder.encode(Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes()));
            HttpManager.getInstance().get(ApiConfig.GET_MY_FOLLOWING, request,pub_key,did_key,  new HttpManager.Callback<String>() {
                @Override
                public void onResponse(String response) {
                    callback.onSuccess(response);
                }

                @Override
                public void onError(String error) {
                    callback.onFail("error: "+error);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFail("ed25519 sign error");
        }
    }

    public void getFollowerAndFollowing(int page, int size, GetMyFollowingCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            GetFollowerRequest request = new GetFollowerRequest();
            request.page = page;
            request.size = size;
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.timestamp = System.currentTimeMillis();
            request.web3mq_user_signature = URLEncoder.encode(Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes()));
            HttpManager.getInstance().get(ApiConfig.GET_FOLLOWERS_AND_FOLLOWING, request,pub_key,did_key, new HttpManager.Callback<String>() {
                @Override
                public void onResponse(String response) {
                    callback.onSuccess(response);
                }

                @Override
                public void onError(String error) {
                    callback.onFail("error: "+error);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFail("ed25519 sign error");
        }
    }

    public void sendFriendRequest(String target_userid, long timeStamp, String content, SendFriendRequestCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            AddFriendsRequest request = new AddFriendsRequest();
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.target_userid = target_userid;
            request.timestamp = timeStamp;
            request.content = content;
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.target_userid+content+timeStamp).getBytes());
            HttpManager.getInstance().post(ApiConfig.ADD_FRIENDS, request,pub_key,did_key, BaseResponse.class, new HttpManager.Callback<BaseResponse>() {
                @Override
                public void onResponse(BaseResponse response) {
                    if(response.getCode()==0){
                        callback.onSuccess();
                    }else{
                        callback.onFail("error code: "+response.getCode()+" msg:"+ response.getMsg());
                    }
                }

                @Override
                public void onError(String error) {
                    callback.onFail("error: "+error);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFail("ed25519 sign error");
        }
    }
}
