package com.ty.web3_mq;

import com.ty.web3_mq.http.ApiConfig;
import com.ty.web3_mq.http.HttpManager;
import com.ty.web3_mq.http.beans.GroupMembersBean;
import com.ty.web3_mq.http.request.CreateGroupRequest;
import com.ty.web3_mq.http.request.GetGroupListRequest;
import com.ty.web3_mq.http.request.GetGroupMembersRequest;
import com.ty.web3_mq.http.request.InvitationGroupRequest;
import com.ty.web3_mq.http.response.ContactsResponse;
import com.ty.web3_mq.http.response.CreateGroupResponse;
import com.ty.web3_mq.http.response.GroupMembersResponse;
import com.ty.web3_mq.http.response.GroupsResponse;
import com.ty.web3_mq.http.response.InvitationGroupResponse;
import com.ty.web3_mq.interfaces.CreateGroupCallback;
import com.ty.web3_mq.interfaces.GetGroupListCallback;
import com.ty.web3_mq.interfaces.GetGroupMembersCallback;
import com.ty.web3_mq.interfaces.InvitationGroupCallback;
import com.ty.web3_mq.utils.Constant;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.utils.Ed25519;

public class Web3MQGroup {
    private static final String TAG = "Web3MQGroup";
    private volatile static Web3MQGroup notification;
    private Web3MQGroup() {
    }

    public static Web3MQGroup getInstance() {
        if (null == notification) {
            synchronized (Web3MQGroup.class) {
                if (null == notification) {
                    notification = new Web3MQGroup();
                }
            }
        }
        return notification;
    }

    public void createGroup(String group_name, CreateGroupCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
            String prv_key_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
            CreateGroupRequest request = new CreateGroupRequest();
            request.group_name = group_name;
            request.userid = "user:"+pub_key;
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes());
            HttpManager.getInstance().post(ApiConfig.GROUP_CREATE, request, CreateGroupResponse.class, new HttpManager.Callback<CreateGroupResponse>() {
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
            String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
            String prv_key_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
            InvitationGroupRequest request = new InvitationGroupRequest();
            request.groupid = groupid;
            request.userid = "user:"+pub_key;
            request.timestamp = System.currentTimeMillis();
            request.members = member_ids;
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.groupid+request.timestamp).getBytes());
            HttpManager.getInstance().post(ApiConfig.GROUP_INVITATION, request, InvitationGroupResponse.class, new HttpManager.Callback<InvitationGroupResponse>() {
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
            String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
            String prv_key_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
            GetGroupListRequest request = new GetGroupListRequest();
            request.page = page;
            request.size = size;
            request.userid = "user:"+pub_key;
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes());
            HttpManager.getInstance().get(ApiConfig.GET_GROUP_LIST, request, GroupsResponse.class, new HttpManager.Callback<GroupsResponse>() {
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
            String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
            String prv_key_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
            GetGroupMembersRequest request = new GetGroupMembersRequest();
            request.page = page;
            request.size = size;
            request.userid = "user:"+pub_key;
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes());
            request.groupid = groupid;
            HttpManager.getInstance().get(ApiConfig.GET_GROUP_MEMBERS, request, GroupMembersResponse.class, new HttpManager.Callback<GroupMembersResponse>() {
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
