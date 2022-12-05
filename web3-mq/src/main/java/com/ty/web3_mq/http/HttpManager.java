package com.ty.web3_mq.http;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interceptors.HttpLoggingInterceptor;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ty.web3_mq.http.interceptor.HeadersInterceptor;
import com.ty.web3_mq.http.interceptor.TokenInterceptor;
import com.ty.web3_mq.http.request.BaseRequest;
import com.ty.web3_mq.http.utils.SignUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 *
 */
public class HttpManager {
    private static final String TAG = "HttpManager";
//    private Context mContext;
    private Gson mGson;

    private HttpManager() {
    }

    private static class SingleHolder {
        private static final HttpManager INSTANCE = new HttpManager();
    }

    public static HttpManager getInstance() {
        return SingleHolder.INSTANCE;
    }

    public void initialize(final Context context) {
//        this.mContext = context;
        this.mGson = new Gson();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY );
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .addInterceptor(new TokenInterceptor())
                .addInterceptor(loggingInterceptor)
                .build();
        okHttpClient.dispatcher().setMaxRequests(60);

        AndroidNetworking.initialize(context, okHttpClient);
    }

    public void post(final String url, final BaseRequest request, final Class clazz,
                     final Callback callback) {
        ANRequest.PostRequestBuilder builder = AndroidNetworking.post(url)
                .addApplicationJsonBody(request)
                .setContentType(ApiConfig.Headers.JSON_CONTENT_TYPE);

        builder.build().getAsObject(clazz, new ParsedRequestListener() {
            @Override
            public void onResponse(Object response) {
                if (null != callback) {
                    callback.onResponse(response);
                }
            }

            @Override
            public void onError(ANError anError) {
                if (null != callback) {
                    callback.onError(anError.getErrorBody());
                }
            }
        });
    }

    public void get(final String url,final BaseRequest request, final Class clazz,
                     final Callback callback) {
        StringBuilder final_url = new StringBuilder(url);
        if(request!=null){
            Type empMapType = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> map = mGson.fromJson(mGson.toJson(request), empMapType);
            final_url.append("?");
            for(String key: map.keySet()){
                final_url.append(key).append("=").append(map.get(key)).append("&");
            }
            if (final_url.length() > 0) {
                final_url.deleteCharAt(final_url.length() - 1);
            }
        }

        Log.i(TAG,"final_url:"+final_url.toString());
        ANRequest.GetRequestBuilder builder = AndroidNetworking.get(final_url.toString());
        builder.build().getAsObject(clazz, new ParsedRequestListener() {
            @Override
            public void onResponse(Object response) {
                if (null != callback) {
                    callback.onResponse(response);
                }
            }

            @Override
            public void onError(ANError anError) {
                if (null != callback) {
                    callback.onError(anError.getErrorBody());
                }
            }
        });
    }

    public void upload(final String url, List<File> files, final Class clazz,
                       final Callback callback, final String token){
        String auth = SignUtils.getAuthorization(token);
        String encryption = "ee2b09a1f17c5d24d94748f3e209e62f";

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .addInterceptor(new HeadersInterceptor())
//                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS ))
                .build();
        JsonArray jsonArray = new JsonArray();
        for (File file :files){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("fileName",file.getName());
            jsonArray.add(jsonObject);
        }
        String metadata = jsonArray.toString();

        ANRequest.MultiPartBuilder builder = AndroidNetworking.upload(url)
                .addHeaders(ApiConfig.Headers.AUTHORIZATION, auth)
                .setOkHttpClient(okHttpClient)
                .addMultipartFileList("files",files)
                .addMultipartParameter("metadata",metadata)
                .addMultipartParameter("encryption",encryption);

        builder.build().getAsObject(clazz, new ParsedRequestListener() {
            @Override
            public void onResponse(Object response) {
                if (null != callback) {
                    callback.onResponse(response);
                }
            }

            @Override
            public void onError(ANError anError) {
                if (null != callback) {
                    callback.onError(anError.getErrorBody());
                }
            }
        });
    }

    public interface Callback<T> {
        void onResponse(T response);

        void onError(String error);
    }
}
