package com.ty.web3_mq.http.request;

public class UpdateChatRequest extends BaseRequest{
    public String userid;
    public String web3mq_signature;
    public long timestamp;
    public String chatid;
    public String chat_type;
}