package com.ty.web3_mq;

import com.ty.web3_mq.http.ApiConfig;
import com.ty.web3_mq.http.HttpManager;
import com.ty.web3_mq.http.request.ContractListRequest;
import com.ty.web3_mq.http.request.GetReceviFriendRequestListRequest;
import com.ty.web3_mq.http.request.GetSentFriendRequestListRequest;
import com.ty.web3_mq.http.request.HandleFriendRequest;
import com.ty.web3_mq.http.request.SearchContactRequest;
import com.ty.web3_mq.http.request.SendFriendRequest;
import com.ty.web3_mq.http.response.BaseResponse;
import com.ty.web3_mq.http.response.ContactsResponse;
import com.ty.web3_mq.http.response.FriendRequestListResponse;
import com.ty.web3_mq.interfaces.FriendRequestCallback;
import com.ty.web3_mq.interfaces.GetContactsCallback;
import com.ty.web3_mq.interfaces.GetReceiveFriendRequestListCallback;
import com.ty.web3_mq.interfaces.GetSentFriendRequestListCallback;
import com.ty.web3_mq.interfaces.HandleFriendRequestCallback;
import com.ty.web3_mq.interfaces.SearchContactsCallback;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.utils.Ed25519;

import java.net.URLEncoder;

public class Web3MQContacts {
    private static final String TAG = "Chats";
    private volatile static Web3MQContacts contacts;
    private Web3MQContacts() {
    }
    public static Web3MQContacts getInstance() {
        if (null == contacts) {
            synchronized (Web3MQContacts.class) {
                if (null == contacts) {
                    contacts = new Web3MQContacts();
                }
            }
        }
        return contacts;
    }

    public void getContactList(int page, int size, GetContactsCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            ContractListRequest request = new ContractListRequest();
            request.page = page;
            request.size = size;
            request.timestamp = System.currentTimeMillis();
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.web3mq_signature = URLEncoder.encode(Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes()));
            HttpManager.getInstance().get(ApiConfig.GET_CONTACT_LIST, request, pub_key, did_key, ContactsResponse.class, new HttpManager.Callback<ContactsResponse>() {
                @Override
                public void onResponse(ContactsResponse response) {
                    if(response.getCode()==0){
                        callback.onSuccess(response.getData());
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

    public void searchContact(String keyword, SearchContactsCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            SearchContactRequest request = new SearchContactRequest();
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.keyword = keyword;
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = URLEncoder.encode(Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.keyword+request.timestamp).getBytes()));
            HttpManager.getInstance().get(ApiConfig.GET_CONTACT_LIST, request, pub_key, did_key,ContactsResponse.class, new HttpManager.Callback<ContactsResponse>() {
                @Override
                public void onResponse(ContactsResponse response) {
                    if(response.getCode()==0){
                        callback.onSuccess(response.getData());
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

    public void sendFriendRequest(String target_userid, FriendRequestCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            SendFriendRequest request = new SendFriendRequest();
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.target_userid = target_userid;
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.target_userid+request.timestamp).getBytes());
            HttpManager.getInstance().post(ApiConfig.POST_FRIEND_REQUEST, request,pub_key,did_key, BaseResponse.class, new HttpManager.Callback<BaseResponse>() {
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

    public void getSentFriendRequestList(int page, int size, GetSentFriendRequestListCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            GetSentFriendRequestListRequest request = new GetSentFriendRequestListRequest();
            request.page = page;
            request.size = size;
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = URLEncoder.encode(Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes()));
            HttpManager.getInstance().get(ApiConfig.GET_SENT_FRIEND_REQUEST_LIST, request,pub_key,did_key, FriendRequestListResponse.class, new HttpManager.Callback<FriendRequestListResponse>() {
                @Override
                public void onResponse(FriendRequestListResponse response) {
                    if(response.getCode()==0){
                        callback.onSuccess(response.getData());
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

    public void handleFriendRequest(String target_userid, String action, HandleFriendRequestCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            HandleFriendRequest request = new HandleFriendRequest();
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.target_userid = target_userid;
            request.timestamp = System.currentTimeMillis();
            request.action = action;
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.action+request.target_userid+request.timestamp).getBytes());
            HttpManager.getInstance().post(ApiConfig.HANDLE_FRIEND_REQUEST, request,pub_key,did_key, BaseResponse.class, new HttpManager.Callback<BaseResponse>() {
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

    public void getReceiveFriendRequestList(int page, int size, GetReceiveFriendRequestListCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            GetReceviFriendRequestListRequest request = new GetReceviFriendRequestListRequest();
            request.page = page;
            request.size = size;
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = URLEncoder.encode(Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes()));
            HttpManager.getInstance().get(ApiConfig.GET_RECEIVE_FRIEND_REQUEST_LIST, request,pub_key,did_key, FriendRequestListResponse.class, new HttpManager.Callback<FriendRequestListResponse>() {
                @Override
                public void onResponse(FriendRequestListResponse response) {
                    if(response.getCode()==0){
                        callback.onSuccess(response.getData());
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
