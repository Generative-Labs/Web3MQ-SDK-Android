package com.ty.web3_mq.utils;


import android.util.Base64;
import android.util.Log;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.KeyPairGenerator;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveSpec;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;

import org.bouncycastle.jcajce.provider.digest.SHA3;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;

public class Ed25519 {
    private static final String TAG = "Ed25519";
    public static KeyPair ed25519GenerateKeyPair(){
        KeyPairGenerator edDsaKpg = new KeyPairGenerator();
        KeyPair keyPair = edDsaKpg.generateKeyPair();
        return keyPair;
    }

    public static String[] generateKeyPair(){
        KeyPairGenerator edDsaKpg = new KeyPairGenerator();
        KeyPair keyPair = edDsaKpg.generateKeyPair();
        EdDSAPrivateKey pv = (EdDSAPrivateKey) keyPair.getPrivate();
        String privateKey = bytesToHexString(pv.getSeed());
        String publicKey = bytesToHexString(pv.getAbyte());
        return new String[]{privateKey,publicKey};
    }

    public static String generatePublicKey(String prv_key_hex){
        EdDSANamedCurveSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
        EdDSAPrivateKeySpec privKey = new EdDSAPrivateKeySpec(hexStringToBytes(prv_key_hex), spec);
        EdDSAPrivateKey prv_key = new EdDSAPrivateKey(privKey);
        String publicKey = bytesToHexString(prv_key.getAbyte());
        Log.i(TAG,"length:"+publicKey.length());
        return publicKey;
    }

    public static KeyPair ed25519GenerateKeyPair(String seed){
        EdDSANamedCurveSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
        EdDSAPrivateKeySpec privKey = new EdDSAPrivateKeySpec(toSeedBytes(seed), spec);
        EdDSAPublicKeySpec pubKey = new EdDSAPublicKeySpec(privKey.getA(), spec);
        return new KeyPair(new EdDSAPublicKey(pubKey), new EdDSAPrivateKey(privKey));
    }

    private static byte[] toSeedBytes(String seed){
        MessageDigest md = new SHA3.Digest224();
        byte[] seedBytes = new byte[32];
        byte[] messageDigest = md.digest(seed.getBytes());
        System.arraycopy(messageDigest,0,seedBytes,32-messageDigest.length,messageDigest.length);
        return seedBytes;
    }

    public static PrivateKey getEd25519PrivateKeyBySeed(String seed) {
        EdDSANamedCurveSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
        return new EdDSAPrivateKey(new EdDSAPrivateKeySpec(toSeedBytes(seed), spec));
    }

    public static PrivateKey getEd25519PrivateKey(String prv_key_hex) {
        EdDSANamedCurveSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
        return new EdDSAPrivateKey(new EdDSAPrivateKeySpec(hexStringToBytes(prv_key_hex), spec));
    }

    public static String ed25519Sign(String privateKeyHex, byte[] data) throws Exception {
        EdDSAEngine edEng  = new EdDSAEngine();
        edEng.initSign(getEd25519PrivateKey(privateKeyHex));
        edEng.setParameter(EdDSAEngine.ONE_SHOT_MODE);
        edEng.update(data);
        byte[] enEdata =  edEng.sign();
        return Base64.encodeToString(enEdata,Base64.NO_WRAP);
    }

    public static Boolean ed25519VerifySign(String publicKey, String data, String signData) {
        Boolean isSuccess = null;
        try {
            EdDSAEngine edEng  = new EdDSAEngine();
            EdDSANamedCurveSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
            byte[] publicKeyByte = hexStringToBytes(publicKey);
            PublicKey pk = new EdDSAPublicKey(new EdDSAPublicKeySpec(publicKeyByte, spec));
            edEng.initVerify(pk);
            edEng.setParameter(EdDSAEngine.ONE_SHOT_MODE);
            edEng.update(data.getBytes());
            isSuccess = edEng.verify(Base64.decode(signData,Base64.NO_WRAP));
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return isSuccess;
    }

    public static String ed25519SeedSign(String privateKeySeed, byte[] data) throws Exception {
        EdDSAEngine edEng  = new EdDSAEngine();
        edEng.initSign(getEd25519PrivateKeyBySeed(privateKeySeed));
        edEng.setParameter(EdDSAEngine.ONE_SHOT_MODE);
        edEng.update(data);
        byte[] enEdata =  edEng.sign();
        return Base64.encodeToString(enEdata,Base64.NO_WRAP);
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

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null) {
            return null;
        }
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() >> 1];
        int index = 0;
        for (int i = 0; i < hexString.length(); i++) {
            if (index  > hexString.length() - 1) {
                return byteArray;
            }
            byte highDit = (byte) (Character.digit(hexString.charAt(index), 16) & 0xFF);
            byte lowDit = (byte) (Character.digit(hexString.charAt(index + 1), 16) & 0xFF);
            byteArray[i] = (byte) (highDit << 4 | lowDit);
            index += 2;
        }
        return byteArray;
    }
}
