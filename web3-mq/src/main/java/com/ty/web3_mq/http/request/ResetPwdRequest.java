package com.ty.web3_mq.http.request;

public class ResetPwdRequest extends BaseRequest{
    public String userid;
    public String did_type;
    public String did_value;
    public long timestamp;
    public String did_signature;
    public String signature_content;
    public String pubkey_type;
    public String pubkey_value;
    public String testnet_access_key;
}
