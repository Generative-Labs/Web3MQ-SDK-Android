package com.ty.web3_mq.http.interceptor;

import android.text.TextUtils;


import com.ty.web3_mq.http.ApiConfig;
import com.ty.web3_mq.utils.DateUtils;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HeadersInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request newRequest = request.newBuilder()
                .addHeader(ApiConfig.Headers.DATE_TIME, DateUtils.getDateTime())
                .addHeader(ApiConfig.Headers.REQUEST_ID, DateUtils.getUUID())
                .addHeader(ApiConfig.Headers.ACCEPT_LANGUAGE, getLanguage())
                .build();
        return chain.proceed(newRequest);
    }

    public static String getLanguage() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        String country = locale.getCountry();
        StringBuilder builder = new StringBuilder(language);
        if (!TextUtils.isEmpty(country))
            builder.append('-').append(country);
        return builder.toString();
    }
}
