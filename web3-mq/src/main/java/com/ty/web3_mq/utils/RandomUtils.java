package com.ty.web3_mq.utils;


import java.util.Random;

public class RandomUtils {
    public static String randomNonce(){
        Random random = new Random();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return bytesToHex(bytes);
    }

    private static String bytesToHex(byte[] bytes){
        StringBuilder hexString = new StringBuilder(bytes.length);
        for(int i=0;i<bytes.length;i++){
            String hex = Integer.toHexString(0xff & bytes[i]);
            if(hex.length() == 1){
                hexString.append("0");
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
