package com.tvxargtec.online.api;

import com.tvxargtec.online.models.ContentList;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Servicio API para gestión del historial de reproducción
 */
public interface HistoryService {

    /**
     * Obtiene historial de reproducción del usuario
     * @param token Token de autenticación
     * @return Lista de contenido visto
     */
    @GET("api/history")
    Call<ContentList> getHistory(@Header("Authorization") String token);

    /**
     * Registra reproducción de contenido
     * @param contentId ID del contenido
     * @param progress Progreso de reproducción en segundos
     * @param token Token de autenticación
     * @return Respuesta de éxito
     */
    @POST("api/history/{id}")
    Call<Void> recordWatchHistory(@Path("id") String contentId, 
                                  @Query("progress") int progress,
                                  @Header("Authorization") String token);

    /**
     * Elimina un elemento del historial
     * @param contentId ID del contenido
     * @param token Token de autenticación
     * @return Respuesta de éxito
     */
    @DELETE("api/history/{id}")
    Call<Void> removeFromHistory(@Path("id") String contentId, @Header("Authorization") String token);

    /**
     * Limpia todo el historial
     * @param token Token de autenticación
     * @return Respuesta de éxito
     */
    @DELETE("api/history/clear/all")
    Call<Void> clearHistory(@Header("Authorization") String token);
}
