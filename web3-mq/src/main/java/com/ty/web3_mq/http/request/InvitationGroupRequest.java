package com.ty.web3_mq.http.request;

public class InvitationGroupRequest extends BaseRequest{
    public String userid;
    public String groupid;
    public String[] members;
    public long timestamp;
    public String web3mq_signature;
}