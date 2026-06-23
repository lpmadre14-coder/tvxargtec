package com.tvxargtec.online.utils;

import android.content.Context;

import com.tvxargtec.online.manager.SessionManager;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Interceptor de autenticación que maneja tokens y refrescos automáticos
 */
public class AuthInterceptor implements Interceptor {
    
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // Obtener token de sesión
        SessionManager sessionManager = SessionManager.getInstance(context);
        String token = sessionManager.getAuthToken();

        // Agregar token al header si existe
        Request.Builder requestBuilder = originalRequest.newBuilder();
        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        Request newRequest = requestBuilder.build();
        Response response = chain.proceed(newRequest);

        // Si recibimos un 401 (no autorizado), intentar refrescar el token
        if (response.code() == 401) {
            synchronized (this) {
                // Verificar si el token fue actualizado por otro thread
                String newToken = sessionManager.getAuthToken();
                if (!newToken.equals(token)) {
                    // El token fue refrescado, reintentar con el nuevo token
                    Request retryRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + newToken)
                        .build();
                    response.close();
                    return chain.proceed(retryRequest);
                }

                // TODO: Intentar refrescar el token
                // Si el refresco falla, limpiar sesión y redirigir a login
                sessionManager.clearSession();
                // Aquí se podría lanzar una excepción o notificar a la UI
            }
        }

        return response;
    }
}
