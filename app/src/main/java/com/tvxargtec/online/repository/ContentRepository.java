package com.tvxargtec.online.repository;

import android.content.Context;

import com.tvxargtec.online.api.ContentService;
import com.tvxargtec.online.database.AppDatabase;
import com.tvxargtec.online.database.dao.ContentDao;
import com.tvxargtec.online.database.entity.ContentEntity;
import com.tvxargtec.online.models.Content;
import com.tvxargtec.online.models.ContentList;
import com.tvxargtec.online.utils.ApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio que implementa la estrategia Cache-First:
 * 1. Intenta obtener datos del backend
 * 2. Si hay error, usa los datos locales (cache)
 * 3. Siempre mantiene la base de datos local sincronizada
 */
public class ContentRepository {
    
    private final ContentService contentService;
    private final ContentDao contentDao;
    private final Executor executor;
    private static ContentRepository instance;

    private ContentRepository(Context context) {
        this.contentService = ApiClient.getInstance().createService(ContentService.class);
        this.contentDao = AppDatabase.getInstance(context).contentDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized ContentRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ContentRepository(context);
        }
        return instance;
    }

    /**
     * Obtiene contenido destacado con estrategia Cache-First
     */
    public void getFeaturedContent(String token, OnContentListListener listener) {
        // Primero, mostrar datos del cache
        executor.execute(() -> {
            List<ContentEntity> cachedContent = contentDao.getRecentContent(10);
            if (!cachedContent.isEmpty()) {
                listener.onSuccess(convertEntitiesToModels(cachedContent));
            }
        });

        // Luego, intentar obtener datos frescos del backend
        contentService.getFeaturedContent(token).enqueue(new Callback<ContentList>() {
            @Override
            public void onResponse(Call<ContentList> call, Response<ContentList> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ContentList contentList = response.body();
                    // Guardar en cache
                    executor.execute(() -> {
                        for (Content content : contentList.getData()) {
                            contentDao.insertContent(convertModelToEntity(content));
                        }
                    });
                    // Notificar con datos frescos
                    listener.onSuccess(contentList.getData());
                }
            }

            @Override
            public void onFailure(Call<ContentList> call, Throwable t) {
                // Si falla, usar datos del cache (ya se mostraron arriba)
                listener.onError("Error al cargar contenido: " + t.getMessage());
            }
        });
    }

    /**
     * Busca contenido con estrategia Cache-First
     */
    public void searchContent(String query, String token, OnContentListListener listener) {
        // Primero, buscar en cache local
        executor.execute(() -> {
            List<ContentEntity> cachedResults = contentDao.searchContent(query);
            if (!cachedResults.isEmpty()) {
                listener.onSuccess(convertEntitiesToModels(cachedResults));
            }
        });

        // Luego, intentar obtener resultados frescos del backend
        contentService.searchContent(query, token).enqueue(new Callback<ContentList>() {
            @Override
            public void onResponse(Call<ContentList> call, Response<ContentList> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ContentList contentList = response.body();
                    // Guardar en cache
                    executor.execute(() -> {
                        for (Content content : contentList.getData()) {
                            contentDao.insertContent(convertModelToEntity(content));
                        }
                    });
                    // Notificar con datos frescos
                    listener.onSuccess(contentList.getData());
                }
            }

            @Override
            public void onFailure(Call<ContentList> call, Throwable t) {
                listener.onError("Error en la búsqueda: " + t.getMessage());
            }
        });
    }

    /**
     * Obtiene detalles de un contenido específico
     */
    public void getContentDetails(String contentId, String token, OnContentListener listener) {
        // Primero, buscar en cache
        executor.execute(() -> {
            ContentEntity cachedContent = contentDao.getContentById(contentId);
            if (cachedContent != null) {
                listener.onSuccess(convertEntityToModel(cachedContent));
            }
        });

        // Luego, intentar obtener detalles frescos del backend
        contentService.getContentDetails(contentId, token).enqueue(new Callback<Content>() {
            @Override
            public void onResponse(Call<Content> call, Response<Content> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Content content = response.body();
                    // Guardar en cache
                    executor.execute(() -> contentDao.insertContent(convertModelToEntity(content)));
                    // Notificar con datos frescos
                    listener.onSuccess(content);
                }
            }

            @Override
            public void onFailure(Call<Content> call, Throwable t) {
                listener.onError("Error al cargar detalles: " + t.getMessage());
            }
        });
    }

    /**
     * Obtiene contenido por categoría
     */
    public void getContentByCategory(String category, String token, OnContentListListener listener) {
        // Primero, buscar en cache
        executor.execute(() -> {
            List<ContentEntity> cachedContent = contentDao.getContentByCategory(category);
            if (!cachedContent.isEmpty()) {
                listener.onSuccess(convertEntitiesToModels(cachedContent));
            }
        });

        // Luego, intentar obtener datos frescos del backend
        contentService.getContentByCategory(category, token).enqueue(new Callback<ContentList>() {
            @Override
            public void onResponse(Call<ContentList> call, Response<ContentList> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ContentList contentList = response.body();
                    executor.execute(() -> {
                        for (Content content : contentList.getData()) {
                            contentDao.insertContent(convertModelToEntity(content));
                        }
                    });
                    listener.onSuccess(contentList.getData());
                }
            }

            @Override
            public void onFailure(Call<ContentList> call, Throwable t) {
                listener.onError("Error al cargar categoría: " + t.getMessage());
            }
        });
    }

    // Métodos auxiliares de conversión
    private ContentEntity convertModelToEntity(Content content) {
        return new ContentEntity(
            content.getId(),
            content.getTitle(),
            content.getDescription(),
            content.getPosterUrl(),
            content.getCategory(),
            content.getRating(),
            content.getVideoUrl()
        );
    }

    private Content convertEntityToModel(ContentEntity entity) {
        Content content = new Content(
            entity.id,
            entity.title,
            entity.description,
            entity.posterUrl,
            entity.category,
            entity.rating,
            entity.videoUrl
        );
        content.setThumbnailUrl(entity.thumbnailUrl);
        content.setDuration(entity.duration);
        content.setReleaseDate(entity.releaseDate);
        return content;
    }

    private List<Content> convertEntitiesToModels(List<ContentEntity> entities) {
        List<Content> models = new ArrayList<>();
        for (ContentEntity entity : entities) {
            models.add(convertEntityToModel(entity));
        }
        return models;
    }

    // Interfaces de callback
    public interface OnContentListListener {
        void onSuccess(List<Content> contentList);
        void onError(String error);
    }

    public interface OnContentListener {
        void onSuccess(Content content);
        void onError(String error);
    }
}
