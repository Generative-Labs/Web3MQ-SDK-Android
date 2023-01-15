package com.ty.web3_mq;
import android.util.Base64;
import android.util.Log;

import com.ty.web3_mq.http.ApiConfig;
import com.ty.web3_mq.http.HttpManager;
import com.ty.web3_mq.http.request.GetMyProfileRequest;
import com.ty.web3_mq.http.request.GetUserInfoRequest;
import com.ty.web3_mq.http.request.PostMyProfileRequest;
import com.ty.web3_mq.http.request.SearchUsersRequest;
import com.ty.web3_mq.http.request.UserLoginRequest;
import com.ty.web3_mq.http.request.UserRegisterRequest;
import com.ty.web3_mq.http.response.LoginResponse;
import com.ty.web3_mq.http.response.RegisterResponse;
import com.ty.web3_mq.http.response.ProfileResponse;
import com.ty.web3_mq.http.response.SearchUsersResponse;
import com.ty.web3_mq.http.response.UserInfoResponse;
import com.ty.web3_mq.interfaces.GetMyProfileCallback;
import com.ty.web3_mq.interfaces.GetUserinfoCallback;
import com.ty.web3_mq.interfaces.LoginCallback;
import com.ty.web3_mq.interfaces.PostMyProfileCallback;
import com.ty.web3_mq.interfaces.ResetPwdCallback;
import com.ty.web3_mq.interfaces.SearchUsersCallback;
import com.ty.web3_mq.interfaces.SignupCallback;
import com.ty.web3_mq.utils.CommonUtils;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.utils.Ed25519;
import com.ty.web3_mq.utils.UserIDGenerator;


import org.bouncycastle.jcajce.provider.digest.SHA3;

import java.net.URLEncoder;
import java.security.MessageDigest;

public class Web3MQUser {
    private static final String TAG = "User";
    private String salt = "";
    private volatile static Web3MQUser web3MQUser;
    private long expiredTime = 24*60*60*1000;
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

    public void setSalt(String salt){
        this.salt = salt;
    }

    public String getKeyGenerateSignContent(String wallet_address, String magic_str){
        String signature_content = "Signing this message will allow this app to decrypt messages in the Web3MQ protocol for the following address: "+wallet_address+". This won’t cost you anything.\n" +
                "\n" +
                "If your Web3MQ wallet-associated password and this signature is exposed to any malicious app, this would result in exposure of Web3MQ account access and encryption keys, and the attacker would be able to read your messages.\n" +
                "\n" +
                "In the event of such an incident, don’t panic. You can call Web3MQ’s key revoke API and service to revoke access to the exposed encryption key and generate a new one!\n" +
                "\n" +
                "Nonce: "+magic_str;
        return signature_content;
    }

    public String getRegisterSignContent(String wallet_name, String wallet_address, String your_domain_url, String nonce_content){
        String str_date = CommonUtils.getDate();
        return "Web3MQ wants you to sign in with your "+wallet_name+" account: \n" +
                wallet_address+" \n" +
                "For Web3MQ registration \n" +
                "URI: "+your_domain_url+" \n" +
                "Version: 1 \n" +
                "Nonce: "+nonce_content+" \n" +
                "Issued At: " + str_date;
    }

    public String[] registerSign(String wallet_prv_key,String wallet_type_name, String wallet_address, String your_domain_url, String nonce_content){
        String str_date = CommonUtils.getDate();
        String signature_content = "Web3MQ wants you to sign in with your "+wallet_type_name+" account: \n" +
                wallet_address+" \n" +
                "For Web3MQ registration \n" +
                "URI: "+your_domain_url+" \n" +
                "Version: 1 \n" +
                "Nonce: "+nonce_content+" \n" +
                "Issued At: " + str_date;

        return new String[]{signature_content, CryptoUtils.signMessage(wallet_prv_key,signature_content)};
    }

    public String[] resetPwdSign(String wallet_prv_key,String wallet_type_name, String wallet_address, String your_domain_url, String nonce_content){
        String str_date = CommonUtils.getDate();
        String signature_content = "Web3MQ wants you to sign in with your "+wallet_type_name+" account: \n" +
                wallet_address+" \n" +
                "For Web3MQ reset password \n" +
                "URI: "+your_domain_url+" \n" +
                "Version: 1 \n" +
                "Nonce: "+nonce_content+" \n" +
                "Issued At: " + str_date;

        return new String[]{signature_content, CryptoUtils.signMessage(wallet_prv_key,signature_content)};
    }

    /**
     *注册流程：
     * 1.生成magicString sha3_224(user_id+wallet_type+wallet_address+password)
     * 2.对magicString 签名
     * 3.使用这个签名作为seed生成prv_key
     * 4.
     */

    public String generateMagicString(String wallet_type, String wallet_address, String password){
        int keyIndex = 1;
        MessageDigest md = new SHA3.Digest224();
        String magicString = "web3mq"+wallet_type+":"+wallet_address+keyIndex+password+"web3mq";
        byte[] messageDigest = md.digest(magicString.getBytes());
        String magic_str = Base64.encodeToString(messageDigest, Base64.NO_WRAP);
        return magic_str;
    }

//    public String[] generateKeyPair(String keyGenerateSign){
//        KeyPair keyPair = Ed25519.ed25519GenerateKeyPair(keyGenerateSign);
//        EdDSAPrivateKey pv = (EdDSAPrivateKey) keyPair.getPrivate();
//        String prv_key = Ed25519.bytesToHexString(keyPair.getPrivate().getEncoded());
//        String pub_key = Ed25519.bytesToHexString( keyPair.getPrivate());
//        return new String[]{prv_key, pub_key};
//    }

    public String generateUserID(String wallet_type, String wallet_address){
        String user_id = UserIDGenerator.generateUserID(wallet_type,wallet_address);
        return user_id;
    }

    public void signUp(String user_id,String wallet_type, String wallet_address, String mainPrivateKeyHex,String registerSignatureContent,String registerSign, long timestamp, SignupCallback callback){
        String pub_key = Ed25519.generatePublicKey(mainPrivateKeyHex);
        String did_type = wallet_type;
        String did_value = wallet_address.toLowerCase();
        String pubkey_type = "ed25519";
        String pubkey_value = pub_key;

        String testnet_access_key = Web3MQClient.getInstance().getApiKey();
        UserRegisterRequest request = new UserRegisterRequest();
        request.userid = user_id;
        request.did_type = did_type;
        request.did_value = did_value;
        request.timestamp = timestamp;
        request.did_signature = registerSign;
        request.signature_content = registerSignatureContent;
        request.pubkey_type = pubkey_type;
        request.pubkey_value = pubkey_value;
        request.testnet_access_key = testnet_access_key;

        HttpManager.getInstance().post(ApiConfig.USER_REGISTER, request,null,null, RegisterResponse.class, new HttpManager.Callback<RegisterResponse>() {
            @Override
            public void onResponse(RegisterResponse response) {
                int code = response.getCode();
                Log.i("register","code:"+code+" msg:"+response.getMsg());
                if(code==0){
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

    public void login(String user_id,String wallet_type,String wallet_address,String mainPrivateKey,String main_pubkey, LoginCallback callback){
        try {
            UserLoginRequest request = new UserLoginRequest();
            request.userid = user_id;
            request.did_type = wallet_type;
            request.did_value = wallet_address;
            request.timestamp = System.currentTimeMillis();
            request.pubkey_type = "ed25519";
            String[] keyPair = Ed25519.generateKeyPair();
            request.pubkey_value = keyPair[1];
            String temporary_prv = keyPair[0];
            String temporary_pub = request.pubkey_value;
            request.pubkey_expired_timestamp = request.timestamp+this.expiredTime;
            request.signature_content = CryptoUtils.SHA3_ENCODE(user_id+request.pubkey_value+request.pubkey_expired_timestamp+request.timestamp);
            request.main_pubkey = main_pubkey;
            request.login_signature = Ed25519.ed25519Sign(mainPrivateKey,request.signature_content.getBytes());
            Log.i(TAG,"login_signature:"+request.login_signature);
            HttpManager.getInstance().post(ApiConfig.USER_LOGIN, request, null, null, LoginResponse.class, new HttpManager.Callback<LoginResponse>() {
                @Override
                public void onResponse(LoginResponse response) {
                    int code = response.getCode();
                    if(code==0){
                        DefaultSPHelper.getInstance().saveMainPrivate(mainPrivateKey);
                        DefaultSPHelper.getInstance().saveMainPublic(main_pubkey);
                        DefaultSPHelper.getInstance().saveTempPrivate(temporary_prv);
                        DefaultSPHelper.getInstance().saveTempPublic(temporary_pub);
                        DefaultSPHelper.getInstance().saveUserID(user_id);
                        String did_key = wallet_type+":"+wallet_address;
                        DefaultSPHelper.getInstance().saveDidKey(did_key);
                        callback.onSuccess();
                    }else{
                        callback.onFail("error code:"+code);
                    }
                }

                @Override
                public void onError(String error) {
                    callback.onFail(error);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            callback.onFail("ed25519 sign error");
        }
    }

    public void resetPwd(String user_id,String wallet_type, String wallet_address, String mainPrivateKeyHex,String resetPwdSignatureContent,String resetPwdSign, long timestamp, ResetPwdCallback callback){
        String pub_key = Ed25519.generatePublicKey(mainPrivateKeyHex);
        String did_type = wallet_type;
        String did_value = wallet_address.toLowerCase();
        String pubkey_type = "ed25519";
        String pubkey_value = pub_key;

        String testnet_access_key = Web3MQClient.getInstance().getApiKey();
        UserRegisterRequest request = new UserRegisterRequest();
        request.userid = user_id;
        request.did_type = did_type;
        request.did_value = did_value;
        request.timestamp = timestamp;
        request.did_signature = resetPwdSign;
        request.signature_content = resetPwdSignatureContent;
        request.pubkey_type = pubkey_type;
        request.pubkey_value = pubkey_value;
        request.testnet_access_key = testnet_access_key;


        HttpManager.getInstance().post(ApiConfig.USER_RESET_PWD, request,null,null, RegisterResponse.class, new HttpManager.Callback<RegisterResponse>() {
            @Override
            public void onResponse(RegisterResponse response) {
                int code = response.getCode();
                Log.i("register","code:"+code+" msg:"+response.getMsg());
                if(code==0){
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

    public void setPubKeyExpiredTime(long expiredTime){
        this.expiredTime = expiredTime;
    }

    public boolean isLocalAccountExist(){
        String mainPrivate = DefaultSPHelper.getInstance().getTempPrivate();
        return mainPrivate!=null;
    }

    public void getMyProfile(GetMyProfileCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            GetMyProfileRequest request = new GetMyProfileRequest();
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.timestamp = System.currentTimeMillis();
            request.web3mq_signature = URLEncoder.encode(Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes()));
            HttpManager.getInstance().get(ApiConfig.GET_MY_PROFILE, request,pub_key,did_key,ProfileResponse.class, new HttpManager.Callback<ProfileResponse>() {
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
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            PostMyProfileRequest request = new PostMyProfileRequest();
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.timestamp = System.currentTimeMillis();
            request.nickname = nickname;
            request.avatar_url = avatar_url;
            request.web3mq_signature = Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.timestamp).getBytes());
            HttpManager.getInstance().post(ApiConfig.POST_MY_PROFILE, request,pub_key,did_key, ProfileResponse.class, new HttpManager.Callback<ProfileResponse>() {
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
        return DefaultSPHelper.getInstance().getUserID();
    }

    public void getUserInfo(String did_type, String did_value, GetUserinfoCallback callback){
        try {
            GetUserInfoRequest request = new GetUserInfoRequest();
            request.timestamp = System.currentTimeMillis();
            request.did_type = did_type;
            request.did_value = did_value;
            HttpManager.getInstance().post(ApiConfig.GET_USER_INFO, request,null,null, UserInfoResponse.class, new HttpManager.Callback<UserInfoResponse>() {
                @Override
                public void onResponse(UserInfoResponse response) {
                    if(response.getCode()==0){
                        callback.onSuccess(response.getData());
                    }else if(response.getCode() == 404){
                        callback.onUserNotRegister();
                    }else {
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

    public void searchUsers(String keyword, SearchUsersCallback callback){
        try {
            String pub_key = DefaultSPHelper.getInstance().getTempPublic();
            String prv_key_seed = DefaultSPHelper.getInstance().getTempPrivate();
            String did_key = DefaultSPHelper.getInstance().getDidKey();
            SearchUsersRequest request = new SearchUsersRequest();
            request.userid = DefaultSPHelper.getInstance().getUserID();
            request.timestamp = System.currentTimeMillis();
            request.keyword = keyword;
            request.web3mq_signature = URLEncoder.encode(Ed25519.ed25519Sign(prv_key_seed,(request.userid+request.keyword+request.timestamp).getBytes()));
            HttpManager.getInstance().get(ApiConfig.SEARCH_USERS, request,pub_key,did_key, SearchUsersResponse.class, new HttpManager.Callback<SearchUsersResponse>() {
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
