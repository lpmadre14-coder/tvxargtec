package com.tvxargtec.online.manager;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Pruebas instrumentadas para SessionManager
 */
@RunWith(AndroidJUnit4.class)
public class SessionManagerTest {

    private Context context;
    private SessionManager sessionManager;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        sessionManager = SessionManager.getInstance(context);
        // Limpiar sesión antes de cada prueba
        sessionManager.clearSession();
    }

    @Test
    public void testSaveSession() {
        // Arrange
        String userId = "user123";
        String name = "Juan Pérez";
        String email = "juan@example.com";
        String token = "token_abc123";
        String planStatus = "Premium";

        // Act
        sessionManager.saveSession(userId, name, email, token, planStatus);

        // Assert
        assertTrue(sessionManager.isLoggedIn());
        assertEquals(userId, sessionManager.getUserId());
        assertEquals(name, sessionManager.getUserName());
        assertEquals(email, sessionManager.getUserEmail());
        assertEquals(token, sessionManager.getAuthToken());
        assertEquals(planStatus, sessionManager.getPlanStatus());
    }

    @Test
    public void testClearSession() {
        // Arrange
        sessionManager.saveSession("user123", "Juan", "juan@example.com", "token", "Premium");
        assertTrue(sessionManager.isLoggedIn());

        // Act
        sessionManager.clearSession();

        // Assert
        assertFalse(sessionManager.isLoggedIn());
        assertEquals("", sessionManager.getUserId());
    }

    @Test
    public void testSetUserName() {
        // Arrange
        sessionManager.saveSession("user123", "Juan", "juan@example.com", "token", "Premium");

        // Act
        sessionManager.setUserName("Carlos");

        // Assert
        assertEquals("Carlos", sessionManager.getUserName());
    }

    @Test
    public void testSetUserAvatar() {
        // Arrange
        sessionManager.saveSession("user123", "Juan", "juan@example.com", "token", "Premium");

        // Act
        sessionManager.setUserAvatar("https://example.com/avatar.jpg");

        // Assert
        assertEquals("https://example.com/avatar.jpg", sessionManager.getUserAvatar());
    }

    @Test
    public void testSetPlanStatus() {
        // Arrange
        sessionManager.saveSession("user123", "Juan", "juan@example.com", "token", "Free");

        // Act
        sessionManager.setPlanStatus("Premium");

        // Assert
        assertEquals("Premium", sessionManager.getPlanStatus());
    }

    @Test
    public void testTokenExpiry() {
        // Arrange
        sessionManager.saveSession("user123", "Juan", "juan@example.com", "token", "Premium");

        // Act - Establecer expiración en el pasado
        sessionManager.setTokenExpiry(System.currentTimeMillis() - 1000);

        // Assert
        assertTrue(sessionManager.isTokenExpired());
    }

    @Test
    public void testTokenNotExpired() {
        // Arrange
        sessionManager.saveSession("user123", "Juan", "juan@example.com", "token", "Premium");

        // Act - Establecer expiración en el futuro
        sessionManager.setTokenExpiry(System.currentTimeMillis() + 24 * 60 * 60 * 1000);

        // Assert
        assertFalse(sessionManager.isTokenExpired());
    }
}
