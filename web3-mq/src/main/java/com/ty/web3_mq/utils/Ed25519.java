package com.ty.web3_mq.utils;


import android.os.Build;
import android.util.Log;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.KeyPairGenerator;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveSpec;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Base64;

public class Ed25519 {
    public static KeyPair ed25519GenerateKeyPair(){
        KeyPairGenerator edDsaKpg = new KeyPairGenerator();
        KeyPair keyPair = edDsaKpg.generateKeyPair();

        return keyPair;
    }

    public static PrivateKey getEd25519PrivateKey(String privateKeySeed) {
        EdDSANamedCurveSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
        return new EdDSAPrivateKey(new EdDSAPrivateKeySpec(hexStringToBytes(privateKeySeed), spec));
    }

    public static String ed25519Sign(String privateKeySeed, byte[] data) throws Exception {
        EdDSAEngine edEng  = new EdDSAEngine();
        edEng.initSign(getEd25519PrivateKey(privateKeySeed));
        edEng.setParameter(EdDSAEngine.ONE_SHOT_MODE);
        edEng.update(data);
        byte[] enEdata =  edEng.sign();
        return bytesToHexString(enEdata);
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
