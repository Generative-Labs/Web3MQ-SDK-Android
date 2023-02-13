package com.ty.web3_mq;

import com.ty.web3_mq.http.ApiConfig;
import com.ty.web3_mq.http.HttpManager;
import com.ty.web3_mq.http.request.GetUserPermissionsRequest;
import com.ty.web3_mq.interfaces.GetUserPermissionCallback;
import com.ty.web3_mq.utils.ConvertUtil;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.utils.Ed25519;

import java.net.URLEncoder;

public class Web3MQPermission {
    private static final String TAG = "Web3MQSign";


    private volatile static Web3MQPermission instance;
    public static Web3MQPermission getInstance() {
        if (null == instance) {
            synchronized (Web3MQPermission.class) {
                if (null == instance) {
                    instance = new Web3MQPermission();
                }
            }
        }
        return instance;
    }
    private Web3MQPermission(){}

    public void getUserPermission(String target_userid, GetUserPermissionCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            GetUserPermissionsRequest request = new GetUserPermissionsRequest();
            request.target_userid = target_userid;
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.timestamp = System.currentTimeMillis();
            request.web3mq_user_signature = URLEncoder.encode(Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.target_userid+request.timestamp).getBytes()));
            HttpManager.getInstance().get(ApiConfig.GET_USER_PERMISSIONS, request,pub_key,did_key, new HttpManager.Callback<String>() {
                @Override
                public void onResponse(String response) {
                    callback.onSuccess(ConvertUtil.convertJsonToUserPermissions(response));
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
