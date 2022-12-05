package com.ty.web3_mq.http.request;

public class HandleFriendRequest extends BaseRequest{
    public String userid;
    public String target_userid;
    public long timestamp;
    public String action;
    public String web3mq_signature;
}