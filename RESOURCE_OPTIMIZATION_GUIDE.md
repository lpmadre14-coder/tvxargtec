# Guía de Optimización de Recursos - TVXargtec Online

## 1. Optimización de Imágenes

### 1.1 Convertir a WebP

WebP reduce el tamaño de las imágenes en ~25% sin perder calidad:

```bash
# Usando ImageMagick
convert imagen.png -quality 80 imagen.webp

# Usando cwebp
cwebp -q 80 imagen.png -o imagen.webp
```

En Android Studio:
1. Right-click en imagen PNG
2. Convert to WebP
3. Seleccionar calidad (80-85 recomendado)

### 1.2 Usar Vector Drawables

Para iconos simples, usar SVG/VectorDrawable en lugar de PNG:

```xml
<!-- res/drawable/ic_play.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M8,5v14l11,-7z"/>
</vector>
```

### 1.3 Comprimir Imágenes

```bash
# Usando ImageOptim (macOS)
imageoptim *.png

# Usando OptiPNG
optipng -o2 imagen.png

# Usando PNGQuant
pngquant --quality=80-90 imagen.png
```

### 1.4 Usar Imágenes Responsivas

Proporcionar múltiples resoluciones:

```
res/drawable-ldpi/    (120 dpi)
res/drawable-mdpi/    (160 dpi)
res/drawable-hdpi/    (240 dpi)
res/drawable-xhdpi/   (320 dpi)
res/drawable-xxhdpi/  (480 dpi)
res/drawable-xxxhdpi/ (640 dpi)
```

## 2. Optimización de Dependencias

### 2.1 Eliminar Dependencias No Utilizadas

```bash
# Analizar dependencias
./gradlew dependencies

# Buscar dependencias duplicadas
./gradlew dependencyInsight --dependency com.google.code.gson
```

### 2.2 Usar Versiones Más Ligeras

```kotlin
// En lugar de:
implementation("com.google.android.material:material:1.11.0")

// Considera versiones más ligeras o específicas:
implementation("androidx.appcompat:appcompat:1.6.1")
```

### 2.3 Configurar ProGuard para Eliminar Código No Usado

```
-dontshrink
-dontoptimize
-dontobfuscate
```

Cambiar a:

```
-dontshrink false
-dontoptimize false
-dontobfuscate false
```

## 3. Optimización de Código

### 3.1 Habilitar Minificación

En `app/build.gradle.kts`:

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### 3.2 Eliminar Strings No Utilizados

```
-dontshrink false
-dontoptimize false
```

### 3.3 Usar Lazy Initialization

```java
// En lugar de inicializar todo en onCreate()
private ContentRepository contentRepository;

private ContentRepository getContentRepository() {
    if (contentRepository == null) {
        contentRepository = ContentRepository.getInstance(context);
    }
    return contentRepository;
}
```

## 4. Optimización de Recursos

### 4.1 Eliminar Recursos No Utilizados

```gradle
android {
    packagingOptions {
        exclude 'META-INF/proguard/androidx-*.pro'
        exclude 'META-INF/DEPENDENCIES'
        exclude 'META-INF/LICENSE'
        exclude 'META-INF/LICENSE.txt'
        exclude 'META-INF/NOTICE'
    }
}
```

### 4.2 Usar Strings Dinámicos

```xml
<!-- En lugar de múltiples strings.xml -->
<!-- res/values/strings.xml -->
<string name="app_name">TVXargtec Online</string>
<string name="welcome">Bienvenido a %1$s</string>
```

### 4.3 Comprimir Recursos

```kotlin
android {
    buildTypes {
        release {
            isShrinkResources = true
        }
    }
}
```

## 5. Optimización de Audio/Video

### 5.1 Comprimir Audio

```bash
# Convertir a AAC con bitrate reducido
ffmpeg -i audio.mp3 -b:a 128k audio_compressed.aac

# Convertir a OGG Vorbis
ffmpeg -i audio.mp3 -q:a 6 audio.ogg
```

### 5.2 Comprimir Video

```bash
# Reducir resolución y bitrate
ffmpeg -i video.mp4 -s 1280x720 -b:v 2000k video_compressed.mp4

# Usar codec H.265 (HEVC)
ffmpeg -i video.mp4 -c:v libx265 -crf 28 video_hevc.mp4
```

## 6. Análisis de Tamaño

### 6.1 Analizar APK

```bash
# Generar reporte de tamaño
./gradlew analyzeReleaseBundle

# Usar bundletool
bundletool analyze-bundle --bundle=app-release.aab
```

### 6.2 Usar Android Studio Analyzer

1. Build > Analyze APK
2. Seleccionar APK generado
3. Ver desglose de tamaño por componente

### 6.3 Identificar Archivos Grandes

```bash
# Listar archivos dentro del APK
unzip -l app-release.apk | sort -k4 -n | tail -20
```

## 7. Configuración de Build

### 7.1 Habilitar Compresión

```kotlin
android {
    packagingOptions {
        // Comprimir todos los archivos
        compress 'assets/fonts/*'
        compress 'assets/images/*'
    }
}
```

### 7.2 Usar Split APKs

Para reducir el tamaño de descarga:

```kotlin
android {
    bundle {
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
        language {
            enableSplit = true
        }
    }
}
```

### 7.3 Configurar Arquitecturas

```kotlin
android {
    defaultConfig {
        ndk {
            abiFilters 'arm64-v8a', 'armeabi-v7a'
        }
    }
}
```

## 8. Monitoreo de Tamaño

### 8.1 Crear Baseline

```bash
# Guardar tamaño de APK actual
ls -lh app/release/app-release.apk > baseline.txt
```

### 8.2 Comparar Cambios

```bash
# Después de cambios
ls -lh app/release/app-release.apk > current.txt

# Comparar
diff baseline.txt current.txt
```

### 8.3 Automatizar en CI/CD

```yaml
# .github/workflows/build.yml
- name: Check APK Size
  run: |
    SIZE=$(stat -f%z app/release/app-release.apk)
    MAX_SIZE=$((100 * 1024 * 1024))  # 100 MB
    if [ $SIZE -gt $MAX_SIZE ]; then
      echo "APK size ($SIZE bytes) exceeds limit ($MAX_SIZE bytes)"
      exit 1
    fi
```

## 9. Tamaño Objetivo

| Componente | Tamaño Máximo |
|---|---|
| APK Total | 100 MB |
| Código (DEX) | 50 MB |
| Recursos | 30 MB |
| Librerías Nativas | 20 MB |

## 10. Checklist de Optimización

- [ ] Todas las imágenes están en WebP
- [ ] Iconos usan VectorDrawable
- [ ] Dependencias no utilizadas eliminadas
- [ ] ProGuard está habilitado
- [ ] Minificación está habilitada
- [ ] Shrinking de recursos está habilitado
- [ ] Split APKs configurado
- [ ] Tamaño final < 100 MB
- [ ] Pruebas en dispositivo real
- [ ] Análisis de tamaño completado

## Notas Importantes

- **Probar** después de cada optimización
- **Monitorear** tamaño en cada build
- **Documentar** cambios de optimización
- **Mantener** balance entre tamaño y funcionalidad
