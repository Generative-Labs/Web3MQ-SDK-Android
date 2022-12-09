package com.ty.web3_mq.http.request;

public class GetMessageHistoryRequest extends BaseRequest{
    public String userid;
    public String topic;
    public int page;
    public int size;
    public long timestamp;
    public String web3mq_signature;
}
