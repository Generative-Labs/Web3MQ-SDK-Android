package com.ty.module_chat.bean;

public class ChatItem {
    public static final String CHAT_TYPE_USER = "user";
    public static final String CHAT_TYPE_GROUP = "group";
    public String title;
    public String content;
    public long timestamp;
    public String chatid;
    public String chat_type;
    public int unreadCount;
}
