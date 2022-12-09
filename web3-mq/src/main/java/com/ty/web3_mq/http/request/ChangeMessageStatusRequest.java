package com.ty.web3_mq.http.request;

public class ChangeMessageStatusRequest extends BaseRequest{
    public String userid;
    public String[] messages;
    public String topic;
    public String status;
    public long timestamp;
    public String web3mq_signature;
}
