package com.ty.web3_mq;

import com.ty.web3_mq.http.ApiConfig;
import com.ty.web3_mq.http.HttpManager;
import com.ty.web3_mq.http.request.CreateGroupRequest;
import com.ty.web3_mq.http.request.GetGroupListRequest;
import com.ty.web3_mq.http.request.GetGroupMembersRequest;
import com.ty.web3_mq.http.request.InvitationGroupRequest;
import com.ty.web3_mq.http.response.CreateGroupResponse;
import com.ty.web3_mq.http.response.GroupMembersResponse;
import com.ty.web3_mq.http.response.GroupsResponse;
import com.ty.web3_mq.http.response.InvitationGroupResponse;
import com.ty.web3_mq.interfaces.CreateGroupCallback;
import com.ty.web3_mq.interfaces.GetGroupListCallback;
import com.ty.web3_mq.interfaces.GetGroupMembersCallback;
import com.ty.web3_mq.interfaces.InvitationGroupCallback;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.utils.Ed25519;

import java.net.URLEncoder;

public class Web3MQGroup {
    private static final String TAG = "Web3MQGroup";
    private volatile static Web3MQGroup web3MQGroup;
    private Web3MQGroup() {
    }

    public static Web3MQGroup getInstance() {
        if (null == web3MQGroup) {
            synchronized (Web3MQGroup.class) {
                if (null == web3MQGroup) {
                    web3MQGroup = new Web3MQGroup();
                }
            }
        }
        return web3MQGroup;
    }

    public void createGroup(String group_name, CreateGroupCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            CreateGroupRequest request = new CreateGroupRequest();
            request.group_name = group_name;
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes());
            HttpManager.getInstance().post(ApiConfig.GROUP_CREATE, request,pub_key,did_key,CreateGroupResponse.class, new HttpManager.Callback<CreateGroupResponse>() {
                @Override
                public void onResponse(CreateGroupResponse response) {
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

    public void invitation(String groupid, String[] member_ids, InvitationGroupCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            InvitationGroupRequest request = new InvitationGroupRequest();
            request.groupid = groupid;
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.timestamp = System.currentTimeMillis();
            request.members = member_ids;
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.groupid+request.timestamp).getBytes());
            HttpManager.getInstance().post(ApiConfig.GROUP_INVITATION, request,pub_key,did_key, InvitationGroupResponse.class, new HttpManager.Callback<InvitationGroupResponse>() {
                @Override
                public void onResponse(InvitationGroupResponse response) {
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

    public void getGroupList(int page, int size, GetGroupListCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            GetGroupListRequest request = new GetGroupListRequest();
            request.page = page;
            request.size = size;
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = URLEncoder.encode(Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes()));
            HttpManager.getInstance().get(ApiConfig.GET_GROUP_LIST, request,pub_key,did_key, GroupsResponse.class, new HttpManager.Callback<GroupsResponse>() {
                @Override
                public void onResponse(GroupsResponse response) {
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

    public void getGroupMembers(int page, int size, String groupid, GetGroupMembersCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            GetGroupMembersRequest request = new GetGroupMembersRequest();
            request.page = page;
            request.size = size;
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.timestamp = System.currentTimeMillis();
            request.groupid = groupid;
            request.web3mq_signature = URLEncoder.encode(Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.groupid+request.timestamp).getBytes()));

            HttpManager.getInstance().get(ApiConfig.GET_GROUP_MEMBERS, request,pub_key, did_key, GroupMembersResponse.class, new HttpManager.Callback<GroupMembersResponse>() {
                @Override
                public void onResponse(GroupMembersResponse response) {
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
