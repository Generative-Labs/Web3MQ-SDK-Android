package com.ty.web3_mq.http.beans;

public class MessageStatus {
    public static final String STATUS_READ = "read";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_RECEIVED = "received";
    public String status;
    public long timestamp;
}
