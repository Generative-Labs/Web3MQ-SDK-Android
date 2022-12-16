package com.ty.web3_mq;
import android.util.Log;

import com.ty.web3_mq.http.ApiConfig;
import com.ty.web3_mq.http.HttpManager;
import com.ty.web3_mq.http.request.GetMyProfileRequest;
import com.ty.web3_mq.http.request.GetUserInfoRequest;
import com.ty.web3_mq.http.request.PostMyProfileRequest;
import com.ty.web3_mq.http.request.SearchUsersRequest;
import com.ty.web3_mq.http.request.UserLoginRequest;
import com.ty.web3_mq.http.response.LoginResponse;
import com.ty.web3_mq.http.response.ProfileResponse;
import com.ty.web3_mq.http.response.SearchUsersResponse;
import com.ty.web3_mq.http.response.UserInfoResponse;
import com.ty.web3_mq.interfaces.GetMyProfileCallback;
import com.ty.web3_mq.interfaces.GetUserinfoCallback;
import com.ty.web3_mq.interfaces.PostMyProfileCallback;
import com.ty.web3_mq.interfaces.SearchUsersCallback;
import com.ty.web3_mq.interfaces.SignupCallback;
import com.ty.web3_mq.utils.CommonUtils;
import com.ty.web3_mq.utils.Constant;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.utils.Ed25519;

import net.i2p.crypto.eddsa.EdDSAPrivateKey;

import java.security.KeyPair;
import java.util.Locale;

public class Web3MQUser {
    private static final String TAG = "User";
    private volatile static Web3MQUser web3MQUser;
    private Web3MQUser() {
    }

    public static Web3MQUser getInstance() {
        if (null == web3MQUser) {
            synchronized (Web3MQUser.class) {
                if (null == web3MQUser) {
                    web3MQUser = new Web3MQUser();
                }
            }
        }
        return web3MQUser;
    }

    public void signUp(String eth_prv_key, String eth_address, SignupCallback callback){
        KeyPair keyPair = Ed25519.ed25519GenerateKeyPair();
        String prv_hex = Ed25519.bytesToHexString(keyPair.getPrivate().getEncoded());
        String pub_hex = Ed25519.bytesToHexString(keyPair.getPublic().getEncoded());
        EdDSAPrivateKey pv = (EdDSAPrivateKey) keyPair.getPrivate();
        Log.i("GenerateUser", "pub_hex:"+pub_hex);
        Log.i("GenerateUser", "prv_hex:"+prv_hex);
        String did_type = "eth";
        String did_value = eth_address.toLowerCase(Locale.ROOT);
        long timestamp = System.currentTimeMillis();
        String pubkey_type = "ed25519";
        String pubkey_value = pub_hex.toLowerCase(Locale.ROOT);
        String user_id = "user:" + pubkey_value;
        String nonceContent = CryptoUtils.SHA3_ENCODE(user_id + pubkey_type + pubkey_value +did_type + did_value + timestamp);
        String testnet_access_key = Web3MQClient.getInstance().getApiKey();
        String your_domain_url = "https://www.web3mq.com";
        String str_date = CommonUtils.getDate();
        Log.i("SignMessage","user_id:"+user_id);
        Log.i("SignMessage","timestamp:"+timestamp);
        Log.i("SignMessage","pub_hex:"+pub_hex);
        Log.i("SignMessage","nonceContent:"+nonceContent);
        Log.i("SignMessage","str_date:"+str_date);
        String signature_content = "Web3MQ wants you to sign in with your Ethereum account: \n" +
                eth_address+" \n" +
                "For Web3MQ registration \n" +
                "URI: "+your_domain_url+" \n" +
                "Version: 1 \n" +
                "Nonce: "+nonceContent+" \n" +
                "Issued At: " + str_date;
        String did_signature = CryptoUtils.signMessage(eth_prv_key, signature_content);

        UserLoginRequest request = new UserLoginRequest();
        request.userid = user_id;
        request.did_type = did_type;
        request.did_value = did_value;
        request.timestamp = timestamp;
        request.did_signature = did_signature;
        request.signature_content = signature_content;
        request.pubkey_type = pubkey_type;
        request.pubkey_value = pubkey_value;
        request.testnet_access_key = testnet_access_key;
        Log.i("request","userid:"+request.userid);
        Log.i("request","did_type:"+request.did_type);
        Log.i("request","did_value:"+request.did_value);
        Log.i("request","timestamp:"+request.timestamp);
        Log.i("request","did_signature:"+request.did_signature);
        Log.i("request","signature_content:"+request.signature_content);
        Log.i("request","pubkey_type:"+request.pubkey_type);
        Log.i("request","pubkey_value:"+request.pubkey_value);
        Log.i("request","testnet_access_key:"+request.testnet_access_key);

        HttpManager.getInstance().post(ApiConfig.USER_LOGIN, request, LoginResponse.class, new HttpManager.Callback<LoginResponse>() {
            @Override
            public void onResponse(LoginResponse response) {
                int code = response.getCode();
                Log.i("register","code:"+code+" msg:"+response.getMsg());
                if(code==0){
                    DefaultSPHelper.getInstance().put(Constant.SP_ED25519_PRV_SEED,Ed25519.bytesToHexString(pv.getSeed()));
                    DefaultSPHelper.getInstance().put(Constant.SP_ED25519_PUB_HEX_STR,pubkey_value);
                    Log.i(TAG,"save prv seed:"+Ed25519.bytesToHexString(pv.getSeed()));
                    Log.i(TAG,"save pub key:"+pubkey_value);
                    callback.onSuccess();
                }else{
                    callback.onFail("error code:"+code);
                }
            }

            @Override
            public void onError(String error) {
                Log.i("register","error:"+error);
                callback.onFail(error);
            }
        });
    }

    public boolean isLocalAccountExist(){
        String prv_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
        String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
        return prv_seed!=null && pub_key!=null;
    }

    public void getMyProfile(GetMyProfileCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
            String prv_key_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
            GetMyProfileRequest request = new GetMyProfileRequest();
            request.userid = "user:"+pub_key;
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes());
            HttpManager.getInstance().get(ApiConfig.GET_MY_PROFILE, request, ProfileResponse.class, new HttpManager.Callback<ProfileResponse>() {
                @Override
                public void onResponse(ProfileResponse response) {
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

    public void postMyProfile(String nickname, String avatar_url, PostMyProfileCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
            String prv_key_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
            PostMyProfileRequest request = new PostMyProfileRequest();
            request.userid = "user:"+pub_key;
            request.timestamp = System.currentTimeMillis();
            request.nickname = nickname;
            request.avatar_url = avatar_url;
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes());
            HttpManager.getInstance().post(ApiConfig.POST_MY_PROFILE, request, ProfileResponse.class, new HttpManager.Callback<ProfileResponse>() {
                @Override
                public void onResponse(ProfileResponse response) {
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

    public String getMyUserId(){
        String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
        if(pub_key!=null){
            return "user:"+pub_key;
        }
        return null;
    }

    public void GetUserInfo(String did_type, String did_value, GetUserinfoCallback callback){
        try {
            GetUserInfoRequest request = new GetUserInfoRequest();
            request.timestamp = System.currentTimeMillis();
            request.did_type = did_type;
            request.did_value = did_value;
            HttpManager.getInstance().post(ApiConfig.GET_USER_INFO, request, UserInfoResponse.class, new HttpManager.Callback<UserInfoResponse>() {
                @Override
                public void onResponse(UserInfoResponse response) {
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

    public void SearchUsers(String keyword, SearchUsersCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PUB_HEX_STR,null);
            String prv_key_seed = DefaultSPHelper.getInstance().getString(Constant.SP_ED25519_PRV_SEED,null);
            SearchUsersRequest request = new SearchUsersRequest();
            request.userid = "user:"+pub_key;
            request.timestamp = System.currentTimeMillis();
            request.keyword = keyword;
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.keyword+request.timestamp).getBytes());
            HttpManager.getInstance().get(ApiConfig.SEARCH_USERS, request, SearchUsersResponse.class, new HttpManager.Callback<SearchUsersResponse>() {
                @Override
                public void onResponse(SearchUsersResponse response) {
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
