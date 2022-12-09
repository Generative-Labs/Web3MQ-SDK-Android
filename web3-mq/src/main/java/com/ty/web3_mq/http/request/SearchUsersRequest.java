package com.ty.web3_mq.http.request;

public class SearchUsersRequest extends BaseRequest{
    public String userid;
    public String web3mq_signature;
    public long timestamp;
    public String keyword;
}