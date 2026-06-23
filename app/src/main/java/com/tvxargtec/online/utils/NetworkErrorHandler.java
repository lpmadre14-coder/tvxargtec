package com.tvxargtec.online.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import retrofit2.Response;

/**
 * Gestor centralizado para manejar errores de red y conectividad
 */
public class NetworkErrorHandler {

    /**
     * Verifica si el dispositivo tiene conexión a internet
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    /**
     * Obtiene un mensaje de error amigable basado en el código HTTP
     */
    public static String getErrorMessage(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Solicitud inválida. Por favor verifica los datos.";
            case 401:
                return "No autorizado. Por favor inicia sesión nuevamente.";
            case 403:
                return "Acceso denegado. No tienes permiso para acceder a este recurso.";
            case 404:
                return "Recurso no encontrado.";
            case 500:
                return "Error del servidor. Por favor intenta más tarde.";
            case 502:
                return "Puerta de enlace incorrecta. El servidor está temporalmente no disponible.";
            case 503:
                return "Servicio no disponible. Por favor intenta más tarde.";
            case 504:
                return "Tiempo de espera agotado. Por favor intenta más tarde.";
            default:
                return "Error desconocido. Código: " + statusCode;
        }
    }

    /**
     * Obtiene un mensaje de error basado en la excepción
     */
    public static String getErrorMessage(Throwable throwable) {
        if (throwable instanceof java.net.UnknownHostException) {
            return "No se puede conectar al servidor. Verifica tu conexión a internet.";
        } else if (throwable instanceof java.net.SocketTimeoutException) {
            return "La solicitud tardó demasiado. Por favor intenta nuevamente.";
        } else if (throwable instanceof java.io.IOException) {
            return "Error de conexión. Por favor verifica tu internet.";
        } else {
            return "Error: " + (throwable.getMessage() != null ? throwable.getMessage() : "Desconocido");
        }
    }

    /**
     * Determina si el error es recuperable (reintentar)
     */
    public static boolean isRetryable(int statusCode) {
        return statusCode == 408 || statusCode == 429 || statusCode == 500 || statusCode == 502 || statusCode == 503 || statusCode == 504;
    }

    /**
     * Determina si el error es recuperable basado en la excepción
     */
    public static boolean isRetryable(Throwable throwable) {
        return throwable instanceof java.net.SocketTimeoutException ||
               throwable instanceof java.net.ConnectException ||
               throwable instanceof java.io.InterruptedIOException;
    }

    /**
     * Verifica si la respuesta fue exitosa
     */
    public static boolean isSuccessful(Response<?> response) {
        return response != null && response.isSuccessful();
    }

    /**
     * Obtiene el cuerpo del error de la respuesta
     */
    public static String getErrorBody(Response<?> response) {
        try {
            if (response != null && response.errorBody() != null) {
                return response.errorBody().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
