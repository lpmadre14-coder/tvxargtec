package com.tvxargtec.online.utils;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class ApiInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        Request.Builder requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("User-Agent", "TVXargtec-Android/1.0");

        Request newRequest = requestBuilder.build();
        return chain.proceed(newRequest);
    }
}
