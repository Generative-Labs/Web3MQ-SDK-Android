package com.ty.web3_mq.http.request;

public class UserLoginRequest extends BaseRequest{
    public String userid;
    public String did_type;
    public String did_value;
    public long timestamp;
    public String login_signature;
    public String signature_content;
    public String main_pubkey;
    public String pubkey_type;
    public String pubkey_value;
    public long pubkey_expired_timestamp;
}
