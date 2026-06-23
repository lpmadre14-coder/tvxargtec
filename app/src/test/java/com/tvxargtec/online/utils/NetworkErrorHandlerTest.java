package com.tvxargtec.online.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Pruebas unitarias para NetworkErrorHandler
 */
public class NetworkErrorHandlerTest {

    @Test
    public void testGetErrorMessage_400() {
        String message = NetworkErrorHandler.getErrorMessage(400);
        assertEquals("Solicitud inválida. Por favor verifica los datos.", message);
    }

    @Test
    public void testGetErrorMessage_401() {
        String message = NetworkErrorHandler.getErrorMessage(401);
        assertEquals("No autorizado. Por favor inicia sesión nuevamente.", message);
    }

    @Test
    public void testGetErrorMessage_404() {
        String message = NetworkErrorHandler.getErrorMessage(404);
        assertEquals("Recurso no encontrado.", message);
    }

    @Test
    public void testGetErrorMessage_500() {
        String message = NetworkErrorHandler.getErrorMessage(500);
        assertEquals("Error del servidor. Por favor intenta más tarde.", message);
    }

    @Test
    public void testGetErrorMessage_503() {
        String message = NetworkErrorHandler.getErrorMessage(503);
        assertEquals("Servicio no disponible. Por favor intenta más tarde.", message);
    }

    @Test
    public void testIsRetryable_408() {
        assertTrue(NetworkErrorHandler.isRetryable(408));
    }

    @Test
    public void testIsRetryable_429() {
        assertTrue(NetworkErrorHandler.isRetryable(429));
    }

    @Test
    public void testIsRetryable_500() {
        assertTrue(NetworkErrorHandler.isRetryable(500));
    }

    @Test
    public void testIsRetryable_503() {
        assertTrue(NetworkErrorHandler.isRetryable(503));
    }

    @Test
    public void testIsRetryable_400() {
        assertFalse(NetworkErrorHandler.isRetryable(400));
    }

    @Test
    public void testIsRetryable_401() {
        assertFalse(NetworkErrorHandler.isRetryable(401));
    }

    @Test
    public void testIsRetryable_404() {
        assertFalse(NetworkErrorHandler.isRetryable(404));
    }

    @Test
    public void testGetErrorMessage_UnknownHostException() {
        Throwable throwable = new java.net.UnknownHostException("Host no encontrado");
        String message = NetworkErrorHandler.getErrorMessage(throwable);
        assertTrue(message.contains("No se puede conectar al servidor"));
    }

    @Test
    public void testGetErrorMessage_SocketTimeoutException() {
        Throwable throwable = new java.net.SocketTimeoutException("Timeout");
        String message = NetworkErrorHandler.getErrorMessage(throwable);
        assertTrue(message.contains("tardó demasiado"));
    }

    @Test
    public void testIsRetryable_SocketTimeoutException() {
        Throwable throwable = new java.net.SocketTimeoutException("Timeout");
        assertTrue(NetworkErrorHandler.isRetryable(throwable));
    }

    @Test
    public void testIsRetryable_ConnectException() {
        Throwable throwable = new java.net.ConnectException("Conexión rechazada");
        assertTrue(NetworkErrorHandler.isRetryable(throwable));
    }
}
