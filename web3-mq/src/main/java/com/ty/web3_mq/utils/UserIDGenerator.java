package com.ty.web3_mq.utils;


import android.util.Base64;

public class UserIDGenerator {
    public static String generateUserID(String wallet_type, String wallet_address){
        return "user:"+ Base64.encodeToString(CryptoUtils.SHA3_ENCODE(wallet_type+":"+wallet_address).getBytes(),Base64.NO_WRAP);
    }
}