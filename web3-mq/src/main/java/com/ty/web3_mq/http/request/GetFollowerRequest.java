package com.ty.web3_mq.http.request;

public class GetFollowerRequest extends BaseRequest{
    public int page;
    public int size;
    public String userid;
    public long timestamp;
    public String web3mq_user_signature;
}