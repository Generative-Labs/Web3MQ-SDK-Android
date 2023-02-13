package com.ty.web3_mq.utils;


import com.google.gson.JsonObject;
import com.ty.web3_mq.http.beans.FollowerBean;
import com.ty.web3_mq.http.beans.FollowersBean;
import com.ty.web3_mq.http.beans.UserPermissionsBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ConvertUtil {
    public static FollowersBean convertJsonToFollowersBean(String json){
        FollowersBean bean = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject data = jsonObject.getJSONObject("data");
            bean = new FollowersBean();
            bean.total_count =data.getInt("total_count");
            ArrayList<FollowerBean> user_list = new ArrayList<>();
            JSONArray jsonArray = data.getJSONArray("user_list");
            for(int i =0;i<jsonArray.length();i++){
                JSONObject obj = jsonArray.getJSONObject(i);
                FollowerBean followerBean = new FollowerBean();
                followerBean.follow_status = obj.getString("follow_status");
                followerBean.avatar_url = obj.getString("avatar_url");
                followerBean.nickname = obj.getString("nickname");
                followerBean.userid = obj.getString("userid");
                followerBean.wallet_address = obj.getString("wallet_address");
                followerBean.wallet_type = obj.getString("wallet_type");
                followerBean.permissions = obj.getJSONObject("permissions").toString();
                user_list.add(followerBean);
            }
            bean.user_list = user_list;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bean;
    }

    public static UserPermissionsBean convertJsonToUserPermissions(String json){
        UserPermissionsBean bean = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject data = jsonObject.getJSONObject("data");
            bean = new UserPermissionsBean();
            bean.target_userid =data.getString("target_userid");
            bean.chat_permission = data.getJSONObject("permissions").getJSONObject("user:chat").getString("value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bean;
    }
}
