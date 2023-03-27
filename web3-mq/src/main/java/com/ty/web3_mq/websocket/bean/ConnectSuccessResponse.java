package com.ty.web3_mq.websocket.bean;

public class ConnectSuccessResponse {
    public String id;
    public String jsonrpc;
    public String method;
    public AuthorizationResponseSuccessData result;

    public String getETHAddress(){
        if(result!=null&&result.sessionNamespaces!=null){
            Namespaces namespaces = result.sessionNamespaces.get("eip155");
            if(namespaces!=null&&namespaces.accounts!=null&&namespaces.accounts.size()>0){
                String account = namespaces.accounts.get(0);
                //eip155:42161:0x0910e12C68d02B561a34569E1367c9AAb42bd810
                String[] account_ = account.split(":");
                if(account_.length==3){
                    return account_[2];
                }
            }
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