package com.ty.web3_mq.http.utils;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class SignUtils {
    private static final String TAG = "SignUtils";

    public static String getSign(String url,final Map<String, String> params) {
        String sign = null;
        StringBuilder signValue = new StringBuilder();
        signValue.append(url);
        signValue.append("_CoinTX_");
        if (null != params) {
            //排序key
            Set<String> sortedSet = new TreeSet<String>(new Comparator<String>() {
                @Override
                public int compare(String s, String t1) {
                    return s.compareTo(t1);
                }
            });
            sortedSet.addAll(params.keySet());

            for (String key : sortedSet) {
                signValue.append(key).append("=").append(params.get(key)).append("&");
            }

            if (signValue.length() > 0) {
                signValue.deleteCharAt(signValue.length() - 1);
                sign = signValue.toString();
            }
        }
        Log.i(TAG,"sign_value:"+sign);
        return encryptMd5(sign);
    }


//    public static String getBasicAuthorization(final String sign) {
//        String signed ;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            signed = ApiConfig.TokenType.BASIC + " " + Base64.getEncoder().encodeToString((ApiConfig.APP_KEY + ":" + sign).getBytes());
//            return signed;
//        }else {
//            signed = ApiConfig.TokenType.BASIC + " " + android.util.Base64.encodeToString((ApiConfig.APP_KEY + ":" + sign).getBytes(),android.util.Base64.NO_WRAP).trim();
//            return signed;
//        }
////        return ApiConfig.TokenType.BASIC + " " + Base64.getEncoder()
//    }

    public static String getAuthorization(final String token) {
        return token;
    }

    public static String encryptMd5(String context) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(context.getBytes());
            byte[] encryContext = md.digest();

            int i;
            StringBuffer buf = new StringBuffer("");
            for (byte b : encryContext) {
                i = b;
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static String getSortedStr(Map<String, String> unSortedStr) {
        return unSortedStr
                .entrySet()
                .stream()
                .filter(entry -> !TextUtils.isEmpty(entry.getValue()))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }
}
