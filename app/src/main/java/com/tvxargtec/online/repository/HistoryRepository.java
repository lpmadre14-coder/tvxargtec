package com.tvxargtec.online.repository;

import android.content.Context;

import com.tvxargtec.online.api.HistoryService;
import com.tvxargtec.online.database.AppDatabase;
import com.tvxargtec.online.database.dao.HistoryDao;
import com.tvxargtec.online.database.entity.HistoryEntity;
import com.tvxargtec.online.utils.ApiClient;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio para gestionar el historial de reproducción
 */
public class HistoryRepository {
    
    private final HistoryService historyService;
    private final HistoryDao historyDao;
    private final Executor executor;
    private static HistoryRepository instance;

    private HistoryRepository(Context context) {
        this.historyService = ApiClient.getInstance().createService(HistoryService.class);
        this.historyDao = AppDatabase.getInstance(context).historyDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized HistoryRepository getInstance(Context context) {
        if (instance == null) {
            instance = new HistoryRepository(context);
        }
        return instance;
    }

    /**
     * Registra el progreso de reproducción de un contenido
     */
    public void recordWatchHistory(String contentId, int progress, String token, OnHistoryListener listener) {
        // Primero, guardar localmente
        executor.execute(() -> {
            HistoryEntity history = new HistoryEntity(contentId, progress);
            historyDao.insertHistory(history);
            listener.onSuccess("Historial actualizado");
        });

        // Luego, sincronizar con backend
        historyService.recordWatchHistory(contentId, progress, token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    listener.onError("Error al sincronizar historial");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // El historial se guardó localmente, se sincronizará después
                listener.onError("Error de conexión, historial guardado localmente");
            }
        });
    }

    /**
     * Obtiene el progreso de reproducción de un contenido
     */
    public void getWatchProgress(String contentId, OnProgressListener listener) {
        executor.execute(() -> {
            HistoryEntity history = historyDao.getHistoryByContentId(contentId);
            if (history != null) {
                listener.onResult(history.watchProgress);
            } else {
                listener.onResult(0);
            }
        });
    }

    /**
     * Elimina un elemento del historial
     */
    public void removeFromHistory(String contentId, String token, OnHistoryListener listener) {
        // Primero, eliminar localmente
        executor.execute(() -> {
            historyDao.deleteHistoryByContentId(contentId);
            listener.onSuccess("Eliminado del historial");
        });

        // Luego, sincronizar con backend
        historyService.removeFromHistory(contentId, token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
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
     * Limpia todo el historial
     */
    public void clearHistory(String token, OnHistoryListener listener) {
        // Primero, limpiar localmente
        executor.execute(() -> {
            historyDao.clearAllHistory();
            listener.onSuccess("Historial limpiado");
        });

        // Luego, sincronizar con backend
        historyService.clearHistory(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    listener.onError("Error al sincronizar cambios");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                listener.onError("Error de conexión, cambio guardado localmente");
            }
        });
    }

    // Interfaces de callback
    public interface OnHistoryListener {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface OnProgressListener {
        void onResult(int progress);
    }
}
