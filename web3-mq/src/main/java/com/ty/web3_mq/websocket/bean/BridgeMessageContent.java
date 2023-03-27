package com.ty.web3_mq.websocket.bean;

public class BridgeMessageContent {
//    public String action;
//    public BridgeMessageProposer proposer;
//    public BridgeMessageWalletInfo walletInfo;
//    public String signRaw;
//    public String address;
//    public String signature;
//    public boolean approve;
//    public String requestId;
//    public String userInfo;
    public static final String TYPE_CONNECT_REQUEST = "type_connect_request";
    public static final String TYPE_CONNECT_SUCCESS_RESPONSE = "type_connect_response";
    public static final String TYPE_SIGN_REQUEST = "type_sign_request";
    public static final String TYPE_SIGN_SUCCESS_RESPONSE = "type_sign_response";
    public static final String TYPE_CONNECT_ERROR_RESPONSE = "type_connect_error_response";
    public static final String TYPE_SIGN_ERROR_RESPONSE = "type_sign_error_response";
    public String type;
    public Object content;
}