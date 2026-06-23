package com.tvxargtec.online.api;

import com.tvxargtec.online.models.Content;
import com.tvxargtec.online.models.ContentList;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Servicio API para gestión de contenido (películas, series, etc.)
 */
public interface ContentService {

    /**
     * Obtiene lista de contenido destacado
     * @param token Token de autenticación
     * @return Lista de contenido destacado
     */
    @GET("api/content/featured")
    Call<ContentList> getFeaturedContent(@Header("Authorization") String token);

    /**
     * Busca contenido por término
     * @param query Término de búsqueda
     * @param token Token de autenticación
     * @return Lista de contenido encontrado
     */
    @GET("api/content/search")
    Call<ContentList> searchContent(@Query("q") String query, @Header("Authorization") String token);

    /**
     * Obtiene detalles de un contenido específico
     * @param contentId ID del contenido
     * @param token Token de autenticación
     * @return Detalles del contenido
     */
    @GET("api/content/{id}")
    Call<Content> getContentDetails(@Path("id") String contentId, @Header("Authorization") String token);

    /**
     * Obtiene contenido por categoría
     * @param category Categoría del contenido
     * @param token Token de autenticación
     * @return Lista de contenido de la categoría
     */
    @GET("api/content/category/{category}")
    Call<ContentList> getContentByCategory(@Path("category") String category, @Header("Authorization") String token);

    /**
     * Obtiene recomendaciones personalizadas
     * @param token Token de autenticación
     * @return Lista de contenido recomendado
     */
    @GET("api/content/recommendations")
    Call<ContentList> getRecommendations(@Header("Authorization") String token);
}
