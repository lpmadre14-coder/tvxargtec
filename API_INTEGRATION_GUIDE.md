# Guía de Integración de API - TVXargtec Online

## 1. Configuración de URL Base

### 1.1 Actualizar ApiClient.java

Reemplaza la URL base con la URL real de tu servidor:

```java
// app/src/main/java/com/tvxargtec/online/utils/ApiClient.java
private static final String BASE_URL = "https://api.tvxargtec.com/v1/";  // URL real del servidor
```

### 1.2 Usar BuildConfig para Diferentes Ambientes

Para mantener URLs diferentes según el ambiente (desarrollo, staging, producción):

```kotlin
// app/build.gradle.kts
android {
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"https://dev-api.tvxargtec.com/v1/\"")
        }
        create("staging") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"https://staging-api.tvxargtec.com/v1/\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"https://api.tvxargtec.com/v1/\"")
        }
    }
}
```

Luego en ApiClient:

```java
private static final String BASE_URL = BuildConfig.API_BASE_URL;
```

## 2. Integración de Servicios API

### 2.1 Servicio de Autenticación

Reemplaza los TODO en `LoginAty.java`:

```java
// Conectar con API para login real
ApiClient.getInstance().createService(AuthService.class)
    .login(new LoginRequest(email, password))
    .enqueue(new Callback<LoginResponse>() {
        @Override
        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
            hideLoading();
            if (response.isSuccessful() && response.body() != null) {
                LoginResponse loginResponse = response.body();
                
                // Guardar sesión
                SessionManager sessionManager = SessionManager.getInstance(LoginAty.this);
                sessionManager.saveSession(
                    loginResponse.getUserId(),
                    loginResponse.getUserName(),
                    loginResponse.getUserEmail(),
                    loginResponse.getAuthToken(),
                    loginResponse.getPlanStatus()
                );
                
                // Navegar a MainActivity
                navigateToMain();
            } else {
                String errorMessage = NetworkErrorHandler.getErrorMessage(response.code());
                showToast(errorMessage);
            }
        }
        
        @Override
        public void onFailure(Call<LoginResponse> call, Throwable t) {
            hideLoading();
            String errorMessage = NetworkErrorHandler.getErrorMessage(t);
            showToast(errorMessage);
        }
    });
```

### 2.2 Servicio de Contenido

En `ProfileFragment` o cualquier fragment que muestre contenido:

```java
// Obtener contenido destacado
String token = SessionManager.getInstance(getContext()).getAuthToken();
ContentRepository.getInstance(getContext())
    .getFeaturedContent(token, new ContentRepository.OnContentListListener() {
        @Override
        public void onSuccess(List<Content> contentList) {
            // Actualizar UI con contenido
            contentAdapter.updateList(contentList);
        }
        
        @Override
        public void onError(String error) {
            showToast("Error: " + error);
        }
    });
```

### 2.3 Servicio de Favoritos

```java
// Agregar a favoritos
FavoritesRepository.getInstance(getContext())
    .addFavorite(contentId, token, new FavoritesRepository.OnFavoriteListener() {
        @Override
        public void onSuccess(String message) {
            showToast("Agregado a favoritos");
        }
        
        @Override
        public void onError(String error) {
            showToast("Error: " + error);
        }
    });
```

### 2.4 Servicio de Historial

```java
// Registrar reproducción
HistoryRepository.getInstance(getContext())
    .recordWatchHistory(contentId, progress, token, new HistoryRepository.OnHistoryListener() {
        @Override
        public void onSuccess(String message) {
            // Historial actualizado
        }
        
        @Override
        public void onError(String error) {
            // Manejar error
        }
    });
```

## 3. Manejo de Errores

### 3.1 Errores Comunes

```java
// Usar NetworkErrorHandler para mensajes amigables
if (!NetworkErrorHandler.isNetworkAvailable(context)) {
    showToast("No hay conexión a internet");
    return;
}

// Reintentos automáticos (configurados en RetryInterceptor)
// Los reintentos se manejan automáticamente para errores 5xx y timeouts
```

### 3.2 Errores de Autenticación

```java
// Si recibimos 401 (no autorizado), el AuthInterceptor intenta refrescar el token
// Si falla, limpia la sesión y redirige a login
if (response.code() == 401) {
    SessionManager.getInstance(context).clearSession();
    // Redirigir a LoginAty
    Intent intent = new Intent(context, LoginAty.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
}
```

## 4. Endpoints Esperados

Tu servidor debe proporcionar los siguientes endpoints:

### 4.1 Autenticación

```
POST /auth/login
Content-Type: application/json
{
    "email": "user@example.com",
    "password": "password123"
}

Response:
{
    "success": true,
    "data": {
        "user_id": "123",
        "user_name": "Juan Pérez",
        "user_email": "juan@example.com",
        "auth_token": "eyJhbGc...",
        "plan_status": "Premium"
    }
}
```

### 4.2 Contenido

```
GET /content/featured
Authorization: Bearer {token}

Response:
{
    "success": true,
    "data": [
        {
            "id": "1",
            "title": "Película 1",
            "description": "Descripción",
            "poster_url": "https://...",
            "category": "Acción",
            "rating": 8.5,
            "video_url": "https://..."
        }
    ]
}
```

### 4.3 Búsqueda

```
GET /content/search?q=matrix
Authorization: Bearer {token}

Response: (mismo formato que /content/featured)
```

### 4.4 Favoritos

```
POST /favorites/{contentId}
Authorization: Bearer {token}

Response:
{
    "success": true,
    "message": "Added to favorites"
}

DELETE /favorites/{contentId}
Authorization: Bearer {token}
```

### 4.5 Historial

```
POST /history/{contentId}?progress=3600
Authorization: Bearer {token}

Response:
{
    "success": true,
    "message": "Watch history recorded"
}
```

## 5. Certificados SSL

### 5.1 Certificate Pinning (Recomendado)

Para mayor seguridad, implementa Certificate Pinning:

```java
// En ApiClient.java
OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
    .certificatePinner(new CertificatePinner.Builder()
        .add("api.tvxargtec.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .build())
    .build();
```

### 5.2 Validación de Certificados

Asegúrate de que tu servidor tenga un certificado SSL válido de una autoridad certificadora confiable.

## 6. Testing de Integración

### 6.1 Pruebas con Postman

Usa Postman para probar los endpoints antes de integrar:

1. Crear colección "TVXargtec API"
2. Agregar requests para cada endpoint
3. Usar variables de entorno para token y base URL
4. Guardar respuestas como ejemplos

### 6.2 Pruebas en Emulador

```bash
# Redirigir puerto del servidor local
adb reverse tcp:8080 tcp:8080

# Usar http://10.0.2.2:8080 en lugar de localhost
```

### 6.3 Logs de Red

Habilita logging de OkHttp para debugging:

```java
// En ApiClient.java (solo en debug)
if (BuildConfig.DEBUG) {
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    clientBuilder.addInterceptor(loggingInterceptor);
}
```

## 7. Manejo de Respuestas Paginadas

### 7.1 Implementar Paginación en API

El servidor debe soportar parámetros de paginación:

```
GET /content/featured?page=1&per_page=20
Authorization: Bearer {token}

Response:
{
    "success": true,
    "data": [...],
    "pagination": {
        "current_page": 1,
        "total_pages": 5,
        "per_page": 20,
        "total_items": 100,
        "has_next": true,
        "has_previous": false
    }
}
```

### 7.2 Usar PaginatedContentRepository

```java
PaginatedContentRepository.getInstance(context)
    .searchContentPaginated("matrix", 1, token, new PaginatedContentRepository.OnPaginatedContentListener() {
        @Override
        public void onSuccess(List<Content> contentList, int page, boolean hasMore) {
            contentAdapter.updateList(contentList);
            // Mostrar botón "Cargar más" si hasMore es true
        }
        
        @Override
        public void onError(String error) {
            showToast("Error: " + error);
        }
    });
```

## 8. Notas Importantes

- **Siempre** valida respuestas del servidor
- **Nunca** confíes en datos del cliente para autorización
- **Siempre** usa HTTPS en producción
- **Implementa** rate limiting en el servidor
- **Mantén** logs de todas las operaciones críticas
- **Prueba** con datos reales antes de lanzar a producción
