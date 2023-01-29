package com.ty.web3_mq.http.request;

public class FollowRequest extends BaseRequest{
    public String userid;
    public String target_userid;
    public String action;
    public long timestamp;
    public String did_type;
    public String did_signature;
    public String did_pubkey;
    public String sign_content;
}