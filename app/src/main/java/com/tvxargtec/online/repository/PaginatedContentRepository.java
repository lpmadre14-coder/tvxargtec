package com.tvxargtec.online.repository;

import android.content.Context;

import com.tvxargtec.online.api.ContentService;
import com.tvxargtec.online.database.AppDatabase;
import com.tvxargtec.online.database.dao.ContentDao;
import com.tvxargtec.online.models.Content;
import com.tvxargtec.online.models.ContentList;
import com.tvxargtec.online.models.PaginatedResponse;
import com.tvxargtec.online.utils.ApiClient;
import com.tvxargtec.online.utils.NetworkErrorHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio con soporte para paginación de contenido
 */
public class PaginatedContentRepository {
    
    private final ContentService contentService;
    private final ContentDao contentDao;
    private final Executor executor;
    private static PaginatedContentRepository instance;
    private int currentPage = 1;
    private static final int ITEMS_PER_PAGE = 20;

    private PaginatedContentRepository(Context context) {
        this.contentService = ApiClient.getInstance().createService(ContentService.class);
        this.contentDao = AppDatabase.getInstance(context).contentDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized PaginatedContentRepository getInstance(Context context) {
        if (instance == null) {
            instance = new PaginatedContentRepository(context);
        }
        return instance;
    }

    /**
     * Obtiene contenido destacado con paginación
     */
    public void getFeaturedContentPaginated(int page, String token, OnPaginatedContentListener listener) {
        // Mostrar datos del cache primero
        executor.execute(() -> {
            List<Content> cachedContent = new ArrayList<>();
            int offset = (page - 1) * ITEMS_PER_PAGE;
            // TODO: Implementar offset en DAO
            if (!cachedContent.isEmpty()) {
                listener.onSuccess(cachedContent, page, true);
            }
        });

        // Luego obtener datos frescos del backend
        // TODO: Conectar con endpoint paginado
        // contentService.getFeaturedContentPaginated(page, ITEMS_PER_PAGE, token)
        //     .enqueue(new Callback<PaginatedResponse<Content>>() { ... });
    }

    /**
     * Busca contenido con paginación
     */
    public void searchContentPaginated(String query, int page, String token, OnPaginatedContentListener listener) {
        if (!NetworkErrorHandler.isNetworkAvailable(null)) {
            listener.onError("No hay conexión a internet");
            return;
        }

        // TODO: Conectar con endpoint de búsqueda paginada
        // contentService.searchContentPaginated(query, page, ITEMS_PER_PAGE, token)
        //     .enqueue(new Callback<PaginatedResponse<Content>>() {
        //         @Override
        //         public void onResponse(Call<PaginatedResponse<Content>> call, Response<PaginatedResponse<Content>> response) {
        //             if (response.isSuccessful() && response.body() != null) {
        //                 PaginatedResponse<Content> paginatedResponse = response.body();
        //                 List<Content> contentList = paginatedResponse.getData();
        //                 
        //                 // Guardar en cache
        //                 executor.execute(() -> {
        //                     for (Content content : contentList) {
        //                         contentDao.insertContent(convertModelToEntity(content));
        //                     }
        //                 });
        //                 
        //                 boolean hasMore = paginatedResponse.getPagination().hasNext();
        //                 listener.onSuccess(contentList, page, hasMore);
        //             } else {
        //                 listener.onError(NetworkErrorHandler.getErrorMessage(response.code()));
        //             }
        //         }
        //         
        //         @Override
        //         public void onFailure(Call<PaginatedResponse<Content>> call, Throwable t) {
        //             listener.onError(NetworkErrorHandler.getErrorMessage(t));
        //         }
        //     });
    }

    /**
     * Obtiene contenido por categoría con paginación
     */
    public void getContentByCategoryPaginated(String category, int page, String token, OnPaginatedContentListener listener) {
        if (!NetworkErrorHandler.isNetworkAvailable(null)) {
            listener.onError("No hay conexión a internet");
            return;
        }

        // TODO: Conectar con endpoint de categoría paginada
    }

    /**
     * Reinicia la paginación
     */
    public void resetPagination() {
        this.currentPage = 1;
    }

    /**
     * Obtiene la página actual
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Establece la página actual
     */
    public void setCurrentPage(int page) {
        this.currentPage = page;
    }

    /**
     * Obtiene el número de items por página
     */
    public int getItemsPerPage() {
        return ITEMS_PER_PAGE;
    }

    // Interfaces de callback
    public interface OnPaginatedContentListener {
        void onSuccess(List<Content> contentList, int page, boolean hasMore);
        void onError(String error);
    }
}
