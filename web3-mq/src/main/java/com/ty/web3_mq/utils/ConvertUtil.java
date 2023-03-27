package com.ty.web3_mq.utils;


import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.ty.web3_mq.http.beans.FollowerBean;
import com.ty.web3_mq.http.beans.FollowersBean;
import com.ty.web3_mq.http.beans.UserPermissionsBean;
import com.ty.web3_mq.websocket.bean.BridgeMessageContent;
import com.ty.web3_mq.websocket.bean.ConnectRequest;
import com.ty.web3_mq.websocket.bean.ConnectSuccessResponse;
import com.ty.web3_mq.websocket.bean.ErrorResponse;
import com.ty.web3_mq.websocket.bean.SignRequest;
import com.ty.web3_mq.websocket.bean.SignSuccessResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ConvertUtil {
    private static final String TAG = "ConvertUtil";
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

    public static BridgeMessageContent convertJsonToBridgeMessageContent(String json_content, Gson gson) {
        BridgeMessageContent bridgeMessageContent = new BridgeMessageContent();
        JsonParser jsonParser = new JsonParser();
        JsonElement element = jsonParser.parse(json_content);
        if(element.getAsJsonObject().has("params")){
            if(element.getAsJsonObject().get("method").getAsString().equals("provider_authorization")){
                // connect request
                bridgeMessageContent.type = BridgeMessageContent.TYPE_CONNECT_REQUEST;
                bridgeMessageContent.content = gson.fromJson(json_content, ConnectRequest.class);
            }else if(element.getAsJsonObject().get("method").getAsString().equals("personal_sign")){
                // sign request
                bridgeMessageContent.type = BridgeMessageContent.TYPE_SIGN_REQUEST;
                bridgeMessageContent.content = gson.fromJson(json_content, SignRequest.class);
            }
        }else{
            //response
            if(element.getAsJsonObject().has("result")){
                //success response
                if(element.getAsJsonObject().get("method").getAsString().equals("provider_authorization")){
                    //connect response
                    bridgeMessageContent.type = BridgeMessageContent.TYPE_CONNECT_SUCCESS_RESPONSE;
                    bridgeMessageContent.content = gson.fromJson(json_content, ConnectSuccessResponse.class);
                }else if(element.getAsJsonObject().get("method").getAsString().equals("personal_sign")){
                    //sign response
                    bridgeMessageContent.type = BridgeMessageContent.TYPE_SIGN_SUCCESS_RESPONSE;
                    bridgeMessageContent.content = gson.fromJson(json_content, SignSuccessResponse.class);
                }
            }else if(element.getAsJsonObject().has("error")){
                //error response
                if(element.getAsJsonObject().get("method").getAsString().equals("provider_authorization")){
                    bridgeMessageContent.type = BridgeMessageContent.TYPE_CONNECT_ERROR_RESPONSE;
                }else if(element.getAsJsonObject().get("method").getAsString().equals("personal_sign")){
                    bridgeMessageContent.type = BridgeMessageContent.TYPE_SIGN_ERROR_RESPONSE;
                }
                bridgeMessageContent.content = gson.fromJson(json_content, ErrorResponse.class);
            }
        }
        return bridgeMessageContent;
    }

    public static ConnectRequest convertDeepLinkToConnectRequest(String deepLink){
        ConnectRequest request = new ConnectRequest();
        String[] strs = deepLink.replace("web3mq://?","").split("&");
        for(String str: strs){
            Log.i(TAG,"str:"+str);
            String[] as = str.split("=");
            if(as.length<2){
                continue;
            }
            String key = as[0];
            String value = as[1];
            request.icons = new ArrayList<>();
            if(key.equals("topic")){
                request.topic = URLDecoder.decode(value);
            }
            if(key.equals("request[id]")){
                request.id = URLDecoder.decode(value);
            }
            if(key.equals("request[jsonrpc]")){
                request.jsonrpc = URLDecoder.decode(value);
            }
            if(key.equals("request[method]")){
                request.method = URLDecoder.decode(value);
            }
            if(key.equals("proposer[appMetadata][description]")){
                request.description = URLDecoder.decode(value);
            }
            if(key.equals("proposer[publicKey]")){
                request.publicKey = URLDecoder.decode(value);
            }
            if(key.equals("proposer[metadata][icons][]")){
                request.icons.add(URLDecoder.decode(value));
            }
            if(key.equals("proposer[metadata][redirect]")){
                request.redirect = URLDecoder.decode(value);
            }
            if(key.equals("proposer[appMetadata][name]")){
                request.name = URLDecoder.decode(value);
            }
            if(key.equals("proposer[appMetadata][url]")){
                request.url = URLDecoder.decode(value);
            }
            if(key.equals("request[params][sessionProperties][expiry]")){
                request.expiry = URLDecoder.decode(value);
            }
        }


//        request.topic = uri.getQueryParameter("topic");
//        request.id = uri.getQueryParameter(Uri.decode("request[id]").replace("[","%5B").replace("]", "%5D"));
//        Log.i(TAG,"request id:"+request.id);
//        request.jsonrpc = uri.getQueryParameter(Uri.decode("request[jsonrpc]"));
//        request.method = uri.getQueryParameter(Uri.decode("request[method]"));
//        request.description = uri.getQueryParameter(Uri.decode("proposer[appMetadata][description]"));
//        request.publicKey = uri.getQueryParameter(Uri.decode("proposer[publicKey]"));
//        request.icons = uri.getQueryParameters(Uri.decode("proposer[metadata][icons]"));
//        request.redirect = uri.getQueryParameter(Uri.decode("proposer[metadata][redirect]"));
//        request.name = uri.getQueryParameter(Uri.decode("proposer[appMetadata][name]"));
//        request.url = uri.getQueryParameter(Uri.decode("proposer[appMetadata][url]"));
        return request;
    }

    public static String convertConnectRequestToDeepLink(ConnectRequest request){
        String url =  "web3mq://?request[id]="+request.id+
                "&topic="+request.topic+
                "&request[jsonrpc]=" +request.jsonrpc+
                "&request[method]=" +request.method+
                "&proposer[appMetadata][description]" +request.description+
                "&proposer[publicKey]=" +request.publicKey+
                "&proposer[metadata][redirect]=" +request.redirect+
                "&proposer[appMetadata][name]=" +request.name+
                "&proposer[appMetadata][url]=" +request.url+
                "&request[params][sessionProperties][expiry]="+request.expiry;
        StringBuilder deepLink =new StringBuilder(url);
        if(request.icons!=null){
            for(String icon:request.icons){
                deepLink.append("&").append("proposer[appMetadata][icons][]=").append(icon);
            }
        }
        return deepLink.toString();
    }
}
