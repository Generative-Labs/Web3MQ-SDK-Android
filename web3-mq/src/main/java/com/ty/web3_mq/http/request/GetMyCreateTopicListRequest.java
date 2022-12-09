package com.ty.web3_mq.http.request;

public class GetMyCreateTopicListRequest extends BaseRequest{
    public String userid;
    public int page;
    public int size;
    public long timestamp;
    public String web3mq_signature;
}
