package com.ty.web3_mq.http.beans;

public class NotificationBean {
    public static final String TYPE_FRIEND_REQUEST = "system.friend_request";
    public static final String TYPE_AGREE_FRIEND_REQUEST = "system.agree_friend_request";
    public static final String TYPE_GROUP_INVITATION = "system.group_invitation";
    public static final String TYPE_SUBSCRIPTION = "subscription";

    public static final String ACTION_FOLLOW = "follow";
    public static final String ACTION_CANCEL = "cancel";
    public String cipher_suite;
    public String from;
    public String topic;
    public String from_sign;
    public String messageid;
    public String payload_type;
    public long timestamp;
    public NotificationPayload payload;
    public int version;
}
