package com.tvxargtec.online.api;

import com.tvxargtec.online.models.LoginRequest;
import com.tvxargtec.online.models.LoginResponse;
import com.tvxargtec.online.models.UserProfile;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Servicio API para autenticación y gestión de usuarios.
 */
public interface AuthService {

    /**
     * Realiza login del usuario
     * @param loginRequest Email y contraseña del usuario
     * @return LoginResponse con token y datos del usuario
     */
    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    /**
     * Obtiene el perfil del usuario autenticado
     * @param token Token de autenticación
     * @return UserProfile con datos del usuario
     */
    @GET("api/profile")
    Call<UserProfile> getUserProfile(@Header("Authorization") String token);

    /**
     * Realiza logout del usuario
     * @param token Token de autenticación
     * @return Respuesta genérica
     */
    @POST("api/logout")
    Call<Void> logout(@Header("Authorization") String token);

    /**
     * Registra un nuevo usuario
     * @param loginRequest Email y contraseña del nuevo usuario
     * @return LoginResponse con token y datos del usuario creado
     */
    @POST("api/register")
    Call<LoginResponse> register(@Body LoginRequest loginRequest);
}
