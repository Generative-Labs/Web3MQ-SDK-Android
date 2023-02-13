package com.ty.web3_mq.http.request;

public class AddFriendsRequest extends BaseRequest{
    public String userid;
    public String target_userid;
    public String content;
    public long timestamp;
    public String web3mq_signature;
}