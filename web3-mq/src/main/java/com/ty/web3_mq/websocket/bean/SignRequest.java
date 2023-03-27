package com.ty.web3_mq.websocket.bean;

import java.util.ArrayList;

public class SignRequest {
    public String id;
    public String jsonrpc;
    public String method;
    //[message,address,password]
    public ArrayList<String> params;

    public String getAddress(){
        if(params!=null && params.size()>1){
            return params.get(1);
        }
        return null;
    }

    public String getSignRaw(){
        if(params!=null && params.size()>0){
            return params.get(0);
        }
        return null;
    }

    public String getPassword(){
        if(params!=null && params.size()>2){
            return params.get(2);
        }
        return null;
    }

//    public String action;
//    public BridgeMessageProposer proposer;
//    public BridgeMessageWalletInfo walletInfo;
//    public String signRaw;
//    public String address;
//    public String signature;
//    public boolean approve;
//    public String requestId;
//    public String userInfo;
}