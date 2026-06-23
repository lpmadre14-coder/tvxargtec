# Guía de Construcción para Producción - TVXargtec Online

## 1. Preparación Previa

Antes de compilar la versión de producción, asegúrate de realizar los siguientes pasos:

### 1.1 Actualizar Versión de la App

Edita `app/build.gradle.kts` y actualiza los números de versión:

```kotlin
android {
    defaultConfig {
        versionCode = 2  // Incrementar para cada release
        versionName = "1.1.0"  // Seguir versionado semántico
    }
}
```

### 1.2 Configurar URLs de Producción

Actualiza `app/src/main/java/com/tvxargtec/online/utils/ApiClient.java`:

```java
private static final String BASE_URL = "https://api.tvxargtec.com/";  // URL real del servidor
```

### 1.3 Habilitar Ofuscación

La ofuscación ya está habilitada en `app/build.gradle.kts`:

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
```

## 2. Compilación de Release

### 2.1 Generar APK Firmado

```bash
# Desde Android Studio:
# 1. Build > Generate Signed Bundle/APK
# 2. Seleccionar "APK"
# 3. Seleccionar o crear un keystore
# 4. Configurar alias y contraseña
# 5. Seleccionar "release" como build variant
# 6. Completar el proceso

# Desde línea de comandos:
./gradlew assembleRelease
```

### 2.2 Generar Bundle (AAB) para Google Play

```bash
./gradlew bundleRelease
```

## 3. Verificación de Seguridad

### 3.1 Verificar Ofuscación

```bash
# Descomprimir el APK
unzip app/release/app-release.apk -d apk_contents

# Verificar que las clases estén ofuscadas
strings apk_contents/classes.dex | grep -i "tvxargtec"
# No debería mostrar nombres claros de clases
```

### 3.2 Verificar Firma

```bash
jarsigner -verify -verbose app/release/app-release.apk
```

### 3.3 Verificar Permisos

```bash
# Listar permisos en el APK
aapt dump permissions app/release/app-release.apk
```

## 4. Pruebas Finales

### 4.1 Pruebas en Dispositivo Real

Instala el APK en un dispositivo real y verifica:

- [ ] Login funciona correctamente
- [ ] Registro de nuevos usuarios
- [ ] Recuperación de contraseña
- [ ] Carga de contenido desde el servidor
- [ ] Funcionalidad de favoritos
- [ ] Historial de reproducción
- [ ] Cambio de avatar
- [ ] Logout y limpieza de sesión
- [ ] Manejo de errores de red (desconectar WiFi)
- [ ] Modo offline (datos en caché)

### 4.2 Pruebas de Rendimiento

```bash
# Monitorear memoria y CPU
adb shell dumpsys meminfo com.tvxargtec.online
adb shell top -n 1 | grep tvxargtec
```

### 4.3 Pruebas de Seguridad

- [ ] Verificar que los tokens se almacenan de forma segura
- [ ] Verificar que las contraseñas no se registran en logs
- [ ] Verificar que no hay datos sensibles en el caché
- [ ] Verificar que la comunicación con el servidor es HTTPS

## 5. Optimización de Recursos

### 5.1 Reducir Tamaño del APK

```bash
# Analizar tamaño del APK
./gradlew analyzeReleaseBundle

# Usar WebP para imágenes en lugar de PNG/JPG
# Comprimir recursos de audio/video
```

### 5.2 Optimizar Imágenes

- Convertir PNG a WebP (reduce ~25% de tamaño)
- Usar imágenes vectoriales (SVG/VectorDrawable) cuando sea posible
- Comprimir imágenes JPEG a máximo 85% de calidad

## 6. Distribución

### 6.1 Google Play Store

1. Acceder a [Google Play Console](https://play.google.com/console)
2. Crear nueva aplicación
3. Completar información de la app (descripción, screenshots, etc.)
4. Subir el Bundle (AAB) generado
5. Configurar precios y distribución
6. Enviar para revisión

### 6.2 Distribución Beta

Antes de lanzar a producción, distribuir a un grupo beta:

1. En Google Play Console, crear un track "Beta"
2. Subir la misma versión del Bundle
3. Invitar usuarios beta (mínimo 20-50 usuarios)
4. Recopilar feedback durante 1-2 semanas
5. Corregir bugs reportados
6. Promover a producción

## 7. Monitoreo Post-Lanzamiento

### 7.1 Firebase Crashlytics

Monitorear crashes en tiempo real:

```bash
# En Firebase Console:
# 1. Ir a Crashlytics
# 2. Revisar crashes por versión
# 3. Priorizar bugs críticos
```

### 7.2 Análisis de Usuarios

- Monitorear DAU (Daily Active Users)
- Monitorear retención (1 día, 7 días, 30 días)
- Monitorear sesiones promedio
- Monitorear eventos clave (login, reproducción, favoritos)

### 7.3 Actualizaciones

Para lanzar actualizaciones:

1. Incrementar versionCode y versionName
2. Actualizar CHANGELOG
3. Compilar nueva versión de release
4. Seguir el mismo proceso de pruebas
5. Subir a Google Play Console

## 8. Checklist Final

Antes de lanzar a producción:

- [ ] Todos los TODO comentarios han sido resueltos
- [ ] Pruebas unitarias pasan (./gradlew test)
- [ ] Pruebas instrumentadas pasan (./gradlew connectedAndroidTest)
- [ ] No hay warnings en la compilación
- [ ] Ofuscación está habilitada
- [ ] URLs de producción están configuradas
- [ ] Certificados SSL están actualizados
- [ ] Claves API están configuradas correctamente
- [ ] Logs de debug están deshabilitados
- [ ] Privacidad y términos están actualizados
- [ ] Screenshots y descripción están en Google Play Console
- [ ] Grupo beta ha validado la funcionalidad

## 9. Rollback Plan

En caso de problemas críticos después del lanzamiento:

1. Identificar el problema
2. Crear hotfix en rama separada
3. Compilar nueva versión con versionCode incrementado
4. Subir a Google Play Console
5. Marcar versión anterior como "deprecated"
6. Notificar a usuarios sobre actualización urgente

## Notas Importantes

- **Nunca** usar la misma contraseña para múltiples keystores
- **Siempre** hacer backup del keystore en lugar seguro
- **Nunca** perder el keystore - es necesario para futuras actualizaciones
- **Siempre** probar en dispositivos reales antes de lanzar
- **Siempre** mantener un changelog detallado de cambios
