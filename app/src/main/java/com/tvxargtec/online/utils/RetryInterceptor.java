package com.tvxargtec.online.utils;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Interceptor que reintentar automáticamente las solicitudes en caso de error
 */
public class RetryInterceptor implements Interceptor {
    
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000; // 1 segundo

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        IOException lastException = null;

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                response = chain.proceed(request);
                
                // Si la respuesta es exitosa o no es recuperable, retornar
                if (response.isSuccessful() || !NetworkErrorHandler.isRetryable(response.code())) {
                    return response;
                }
                
                // Si es recuperable, cerrar la respuesta y reintentar
                response.close();
                
            } catch (IOException e) {
                lastException = e;
                
                // Si no es recuperable, lanzar la excepción
                if (!NetworkErrorHandler.isRetryable(e)) {
                    throw e;
                }
            }
            
            // Esperar antes de reintentar (excepto en el último intento)
            if (attempt < MAX_RETRIES - 1) {
                try {
                    Thread.sleep(RETRY_DELAY_MS * (attempt + 1)); // Backoff exponencial
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Reintento interrumpido", e);
                }
            }
        }

        // Si llegamos aquí, todos los reintentos fallaron
        if (response != null) {
            return response;
        }
        
        if (lastException != null) {
            throw lastException;
        }
        
        throw new IOException("Error desconocido después de " + MAX_RETRIES + " intentos");
    }
}
