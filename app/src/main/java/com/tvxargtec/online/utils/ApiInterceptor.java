package com.tvxargtec.online.utils;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Interceptor de OkHttp que agrega headers comunes y maneja tokens de autenticación.
 */
public class ApiInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Agregar headers comunes
        Request.Builder requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("User-Agent", "TVXargtec-Android/1.0");

        // TODO: Agregar token de autenticación si está disponible
        // String token = getAuthToken();
        // if (token != null && !token.isEmpty()) {
        //     requestBuilder.header("Authorization", "Bearer " + token);
        // }

        Request newRequest = requestBuilder.build();
        return chain.proceed(newRequest);
    }

    // TODO: Implementar obtención del token desde SharedPreferences
    // private String getAuthToken() {
    //     // Obtener token guardado localmente
    //     return null;
    // }
}
