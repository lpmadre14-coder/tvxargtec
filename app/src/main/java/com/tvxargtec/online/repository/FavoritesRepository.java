package com.tvxargtec.online.repository;

import android.content.Context;

import com.tvxargtec.online.api.FavoritesService;
import com.tvxargtec.online.database.AppDatabase;
import com.tvxargtec.online.database.dao.FavoriteDao;
import com.tvxargtec.online.database.entity.FavoriteEntity;
import com.tvxargtec.online.utils.ApiClient;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio para gestionar favoritos con sincronización entre local y backend
 */
public class FavoritesRepository {
    
    private final FavoritesService favoritesService;
    private final FavoriteDao favoriteDao;
    private final Executor executor;
    private static FavoritesRepository instance;

    private FavoritesRepository(Context context) {
        this.favoritesService = ApiClient.getInstance().createService(FavoritesService.class);
        this.favoriteDao = AppDatabase.getInstance(context).favoriteDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized FavoritesRepository getInstance(Context context) {
        if (instance == null) {
            instance = new FavoritesRepository(context);
        }
        return instance;
    }

    /**
     * Agrega un contenido a favoritos
     */
    public void addFavorite(String contentId, String token, OnFavoriteListener listener) {
        // Primero, guardar localmente
        executor.execute(() -> {
            favoriteDao.addFavorite(new FavoriteEntity(contentId));
            listener.onSuccess("Agregado a favoritos");
        });

        // Luego, sincronizar con backend
        favoritesService.addFavorite(contentId, token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    // Si falla, remover del cache local
                    executor.execute(() -> favoriteDao.deleteFavoriteByContentId(contentId));
                    listener.onError("Error al sincronizar favorito");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // El favorito se guardó localmente, se sincronizará después
                listener.onError("Error de conexión, favorito guardado localmente");
            }
        });
    }

    /**
     * Elimina un contenido de favoritos
     */
    public void removeFavorite(String contentId, String token, OnFavoriteListener listener) {
        // Primero, eliminar localmente
        executor.execute(() -> {
            favoriteDao.deleteFavoriteByContentId(contentId);
            listener.onSuccess("Eliminado de favoritos");
        });

        // Luego, sincronizar con backend
        favoritesService.removeFavorite(contentId, token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    // Si falla, restaurar en cache local
                    executor.execute(() -> favoriteDao.addFavorite(new FavoriteEntity(contentId)));
                    listener.onError("Error al sincronizar cambios");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                listener.onError("Error de conexión, cambio guardado localmente");
            }
        });
    }

    /**
     * Verifica si un contenido está en favoritos
     */
    public void isFavorite(String contentId, OnBooleanListener listener) {
        executor.execute(() -> {
            int count = favoriteDao.isFavorite(contentId);
            listener.onResult(count > 0);
        });
    }

    /**
     * Obtiene el conteo de favoritos
     */
    public void getFavoritesCount(OnCountListener listener) {
        executor.execute(() -> {
            // TODO: Implementar conteo en la base de datos
            listener.onResult(0);
        });
    }

    // Interfaces de callback
    public interface OnFavoriteListener {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface OnBooleanListener {
        void onResult(boolean result);
    }

    public interface OnCountListener {
        void onResult(int count);
    }
}
