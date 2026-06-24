package com.tvxargtec.online.api;

import com.tvxargtec.online.utils.ApiResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface VipService {

    @GET("api/vip/plans")
    Call<ApiResponse<List<Map<String, Object>>>> getPlans(@Header("Authorization") String token);

    @POST("api/upgrade")
    Call<ApiResponse<Map<String, Object>>> upgrade(@Header("Authorization") String token, @Body Map<String, String> body);
}
