package com.ty.web3_mq.websocket.bean;

public class BridgeMessageContent {
    public String action;
    public BridgeMessageProposer proposer;
    public BridgeMessageWalletInfo walletInfo;
    public String signRaw;
    public String address;
    public String signature;
    public boolean approve;
    public String requestId;
    public String userInfo;
}