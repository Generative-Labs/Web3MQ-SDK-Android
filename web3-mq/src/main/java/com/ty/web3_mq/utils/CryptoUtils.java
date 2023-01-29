package com.ty.web3_mq.utils;

//import org.web3j.crypto.Credentials;
//import org.web3j.crypto.Sign;
//import org.web3j.utils.Numeric;

import android.util.Base64;
import android.util.Log;

import org.bouncycastle.jcajce.provider.asymmetric.rsa.DigestSignatureSpi;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

public class CryptoUtils {
    public static String signMessage(String pri_key,String message){
        Credentials credentials = Credentials.create(pri_key);
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        Sign.SignatureData signature = Sign.signPrefixedMessage(messageBytes, credentials.getEcKeyPair());
        byte[] retval = new byte[65];
        System.arraycopy(signature.getR(), 0, retval, 0, 32);
        System.arraycopy(signature.getS(), 0, retval, 32, 32);
        System.arraycopy(signature.getV(), 0, retval, 64, 1);
        return Numeric.toHexString(retval);
    }

    public static String SHA3_ENCODE(String input){
        Log.i("CryptoUtils","SHA3 String:"+input);
        MessageDigest md = new SHA3.Digest224();
        // digest() method is called
        // to calculate message digest of the input string
        // returned as array of byte
        byte[] messageDigest = md.digest(input.getBytes());

        // Convert byte array into signum representation
        BigInteger no = new BigInteger(1, messageDigest);

        // Convert message digest into hex value
        String hashtext = no.toString(16);

        // Add preceding 0s to make it 32 bit
        while (hashtext.length() < 56) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }

    public static String SHA256_ENCODE(String input){
        MessageDigest md = null;
        byte[] bt = input.getBytes();
        try {
            md = MessageDigest.getInstance("SHA-256");// 将此换成SHA-1、SHA-512、SHA-384等参数
            md.update(bt);
            return bytesToHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String SHA1_ENCODE(String input){
        return SHA1_ENCODE(input.getBytes());
    }

    public static String SHA1_ENCODE(byte[] input){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");// 将此换成SHA-1、SHA-512、SHA-384等参数
            md.update(input);
            return bytesToHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            // 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }
}
