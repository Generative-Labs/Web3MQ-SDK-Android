package com.ty.web3_mq.http.request;

public class GetNotificationHistoryRequest extends BaseRequest{
    public String userid;
    public String notice_type;
    public int page;
    public int size;
    public long timestamp;
    public String web3mq_signature;
}
