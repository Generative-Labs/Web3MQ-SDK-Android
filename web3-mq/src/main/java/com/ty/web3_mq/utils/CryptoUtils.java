package com.ty.web3_mq.utils;

//import org.web3j.crypto.Credentials;
//import org.web3j.crypto.Sign;
//import org.web3j.utils.Numeric;

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
//        return "";
    }

    public static String SHA3_ENCODE(String input){
//        try {
            Log.i("CryptoUtils","SHA3 String:"+input);
//            Security.addProvider(new BouncyCastleProvider());

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
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        return null;
    }
}
