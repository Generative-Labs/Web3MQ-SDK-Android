package com.ty.web3_mq.websocket.bean;

public class BridgeMessage {
    public static final String ACTION_CONNECT_REQUEST = "connectRequest";
    public static final String ACTION_CONNECT_RESPONSE = "connectResponse";

    public static final String ACTION_SIGN_REQUEST = "signRequest";
    public static final String ACTION_SIGN_RESPONSE = "signResponse";

    public String publicKey;
    public String content;
}
