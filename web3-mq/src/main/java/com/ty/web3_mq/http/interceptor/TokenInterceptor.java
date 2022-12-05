package com.ty.web3_mq.http.interceptor;

import android.util.Log;

import com.google.gson.Gson;
import com.ty.web3_mq.http.response.BaseResponse;
import com.ty.web3_mq.utils.DefaultSPHelper;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class TokenInterceptor implements Interceptor {
    private static final String TAG = "TokenInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
//        String token = response.header("Authorization");

        //TODO
//        if (token!=null) {
//            Log.i(TAG,"get token :"+token);
//            DefaultSPHelper.getInstance().put(Constants.SP_LOCAL_TOKEN, token);
//        }


        //根据和服务端的约定判断token过期
//        if (isTokenExpired(response)) {
//            //同步请求方式，获取最新的Token
//            if(HttpManager.getInstance().postSyncRefresh()){
//                String newToken = DefaultSPHelper.getInstance().getString(StringConstant.SP_ACCESS_TOKEN);
//                //使用新的Token，创建新的请求
//                Request newRequest = chain.request()
//                        .newBuilder()
//                        .header("Authorization", ApiConfig.TokenType.BEARER + " " + newToken)
//                        .build();
//                //重新请求
//                return chain.proceed(newRequest);
//            }
//        }
        return response;
    }



    private boolean isTokenExpired(Response response) {
        if(response.code() == 200){
            Gson gson = new Gson();
            BaseResponse baseResponse = null;
            try {
                assert response.body() != null;
                ResponseBody responseBody = response.body();
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // request the entire body.
                Buffer buffer = source.buffer();
                String responseBodyString = buffer.clone().readString(Charset.forName("UTF-8"));

                baseResponse = gson.fromJson(responseBodyString, BaseResponse.class);

                if(baseResponse.getCode() == 3001){
                    return true;
                }else{
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }
}
