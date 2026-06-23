# Tvxargtec — Streaming Premium

## ✅ Estado actual — Transformación completa

### Diseño 2026 — Cinematográfico Premium

**Paleta**: Violeta eléctrico `#7C3AED` + Cian `#00D4FF` + Verde premium `#10B981`  
**Fondo**: Azul noche profundo `#0B1020`  
**Efectos**: Glassmorphism, gradientes, tarjetas con profundidad, sombras suaves

### Navegación — Bottom Navigation (5 tabs)

| Tab | Fragment | Contenido |
|---|---|---|
| 🏠 Home | `HomeFragment` | Banner cinematográfico, carruseles (Continúa viendo, En Vivo, Recomendados, Estrenos, Tendencias), Pull-to-refresh |
| 📡 Live TV | `LiveTvFragment` | Parrilla de canales en vivo con badge EN VIVO animado, categorías, programa actual |
| 📺 Series | `SeriesFragment` | Grid 2 columnas, posters con rating, año, badge HD/4K |
| 🎬 Movies | `MoviesFragment` | Banner destacado, grid con géneros, badges HD/4K |
| 👤 Profile | `ProfileFragment` | Avatar, plan VIP, menú (cuenta, favoritos, historial, settings, cerrar sesión) |

### Flujo de entrada

```
Splash (animación scale+fade 600ms) 
  → ¿Primera vez? 
    → Sí: Onboarding (3 pantallas con ViewPager2) 
    → No: Main (Bottom Navigation)
```

### Experiencia visual

| Elemento | Detalle |
|---|---|
| **Splash** | Logo escala 0.3→1.0 + fade in, tagline aparece después |
| **Onboarding** | 3 pantallas con indicadores de puntos, botón gradient "Comenzar" |
| **Home** | Banner hero con gradiente + botones "Ver Ahora" / "Más Info", 5 carruseles horizontales |
| **Tarjetas** | MaterialCardView 16dp radius, 8dp elevation, borde sutil, glow en selección |
| **Badges** | HD (violeta), 4K (cian), LIVE (rojo con animación) |
| **Skeleton** | Shimmer gradient loading mientras carga contenido |
| **Pull to refresh** | SwipeRefreshLayout en todas las secciones |
| **Modo oscuro** | `values-night/colors.xml` con tonos más profundos |

### Próximos pasos disponibles

- [ ] **Player con selector de calidad/subtítulos/audio** — overlay mejorado
- [ ] **Pagar en la app** — Stripe/MercadoPago
- [ ] **Chromecast funcionando** — conectar CastOptionsProvider
- [ ] **Notificaciones push** — conectar Firebase Cloud Messaging
- [ ] **Multi-idioma completo** — strings.xml traducidos
- [ ] **Descargas offline** — ExoPlayer descarga de contenido
