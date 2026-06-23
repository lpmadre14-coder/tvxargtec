package com.tvxargtec.online.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente API centralizado para manejar todas las llamadas a backend.
 * Configura Retrofit con OkHttp y proporciona acceso a los servicios API.
 */
public class ApiClient {
    
    private static final String BASE_URL = "https://apitvxargtec.duckdns.org/";
    private static ApiClient instance;
    private Retrofit retrofit;
    private OkHttpClient okHttpClient;

    private ApiClient() {
        CertificatePinner certificatePinner = new CertificatePinner.Builder()
            .add("apitvxargtec.duckdns.org", "sha256/bTywe7s4MWF/kC1h6zTaxlM/LtSjcrn4wHFtZ8G2q10=")
            .add("apitvxargtec.duckdns.org", "sha256/brzvtCELCIZUo4sD/qPX0ccRtPsd3DY6RfmxpOU9oB4=")
            .build();

        okHttpClient = new OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .addInterceptor(new RetryInterceptor())
            .addInterceptor(new ApiInterceptor())
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();

        // Configurar Gson
        Gson gson = new GsonBuilder()
            .setLenient()
            .create();

        // Configurar Retrofit
        retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public <T> T createService(Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }
}
