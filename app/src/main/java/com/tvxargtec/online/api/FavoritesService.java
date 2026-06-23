package com.tvxargtec.online.api;

import com.tvxargtec.online.models.Content;
import com.tvxargtec.online.models.ContentList;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Servicio API para gestión de favoritos del usuario
 */
public interface FavoritesService {

    /**
     * Obtiene lista de favoritos del usuario
     * @param token Token de autenticación
     * @return Lista de contenido favorito
     */
    @GET("api/favorites")
    Call<ContentList> getFavorites(@Header("Authorization") String token);

    /**
     * Agrega contenido a favoritos
     * @param contentId ID del contenido
     * @param token Token de autenticación
     * @return Respuesta de éxito
     */
    @POST("api/favorites/{id}")
    Call<Void> addFavorite(@Path("id") String contentId, @Header("Authorization") String token);

    /**
     * Elimina contenido de favoritos
     * @param contentId ID del contenido
     * @param token Token de autenticación
     * @return Respuesta de éxito
     */
    @DELETE("api/favorites/{id}")
    Call<Void> removeFavorite(@Path("id") String contentId, @Header("Authorization") String token);

    /**
     * Verifica si un contenido está en favoritos
     * @param contentId ID del contenido
     * @param token Token de autenticación
     * @return Respuesta con estado de favorito
     */
    @GET("api/favorites/{id}/check")
    Call<Void> isFavorite(@Path("id") String contentId, @Header("Authorization") String token);
}
