package com.ty.web3_mq.http.request;

public class ChangeNotificationStatusRequest extends BaseRequest{
    public String userid;
    public String[] messages;
    public String status;
    public long timestamp;
    public String signature;
}
